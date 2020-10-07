/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.chtijbug.kieserver.services.drools;

import org.chtijbug.drools.kieserver.extension.KieServerAddOnElement;
import org.chtijbug.drools.kieserver.extension.KieServerGlobalVariableDefinition;
import org.chtijbug.drools.kieserver.extension.KieServerListenerDefinition;
import org.chtijbug.drools.kieserver.extension.KieServerLoggingDefinition;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.kie.api.remote.Remotable;
import org.kie.scanner.KieModuleMetaData;
import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.*;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.KieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.lang.annotation.Annotation;
import java.util.*;

public class DroolsChtijbugKieServerExtension implements KieServerExtension {



    public static final String EXTENSION_NAME = "DroolsFramework";
    private static final Logger logger = LoggerFactory.getLogger(DroolsChtijbugKieServerExtension.class);
    private static final Boolean DISABLED = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_DROOLS_SERVER_EXT_DISABLED, "false"));
    private static final Boolean FILTER_REMOTEABLE = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_DROOLS_FILTER_REMOTEABLE_CLASSES, "false"));



    private DroolsChtijbugRulesExecutionService rulesExecutionService;

    private KieServerRegistry registry;

    private List<Object> services = new ArrayList<>();
    private KieServerAddOnElement kieServerAddOnElement = null;

    private boolean initialized = false;



    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
        return !DISABLED;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        initExtensionsList();
        this.rulesExecutionService = new DroolsChtijbugRulesExecutionService(registry, this.kieServerAddOnElement);
        this.registry = registry;
        services.add(rulesExecutionService);
        initialized = true;
    }

    private void initExtensionsList() {
        List<KieServerGlobalVariableDefinition> kieServerGlobalVariableDefinitions = new ArrayList<>();
        ServiceLoader<KieServerGlobalVariableDefinition> serverExtensions1 = ServiceLoader.load(KieServerGlobalVariableDefinition.class);
        for (KieServerGlobalVariableDefinition loadedImpl : serverExtensions1) {
            kieServerGlobalVariableDefinitions.add(loadedImpl);
        }
        List<KieServerLoggingDefinition> kieServerLoggingDefinitions = new ArrayList<>();
        ServiceLoader<KieServerLoggingDefinition> serverExtensions2 = ServiceLoader.load(KieServerLoggingDefinition.class);
        for (KieServerLoggingDefinition loadedImpl : serverExtensions2) {
            kieServerLoggingDefinitions.add(loadedImpl);
        }
        List<KieServerListenerDefinition> kieServerListenerDefinitions = new ArrayList<>();
        ServiceLoader<KieServerListenerDefinition> serverExtensions3 = ServiceLoader.load(KieServerListenerDefinition.class);
        for (KieServerListenerDefinition loadedImpl : serverExtensions3) {
            kieServerListenerDefinitions.add(loadedImpl);
        }

        this.kieServerAddOnElement = new KieServerAddOnElement(kieServerGlobalVariableDefinitions, kieServerLoggingDefinitions, kieServerListenerDefinitions);


    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        // no-op?
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // do any other bootstrapping rule service requires
        Set<Class<?>> extraClasses = new HashSet<>();

        // create kbases so declared types can be created
        Collection<String> kbases = kieContainerInstance.getKieContainer().getKieBaseNames();
        for (String kbase : kbases) {
            kieContainerInstance.getKieContainer().getKieBase(kbase);
        }

        KieModuleMetaData metaData = (KieModuleMetaData) parameters.get(KieServerConstants.KIE_SERVER_PARAM_MODULE_METADATA);
        Collection<String> packages = metaData.getPackages();

        for (String p : packages) {
            Collection<String> classes = metaData.getClasses(p);

            for (String c : classes) {
                String type = p + "." + c;
                try {
                    logger.debug("Adding {} type into extra jaxb classes set", type);
                    Class<?> clazz = Class.forName(type, true, kieContainerInstance.getKieContainer().getClassLoader());

                    addExtraClass(extraClasses, clazz, FILTER_REMOTEABLE);
                    logger.debug("Added {} type into extra jaxb classes set", type);

                } catch (ClassNotFoundException e) {
                    logger.warn("Unable to create instance of type {} due to {}", type, e.getMessage());
                    logger.debug("Complete stack trace for exception while creating type {}", type, e);
                } catch (Exception e) {
                    logger.warn("Unexpected error while create instance of type {} due to {}", type, e.getMessage());
                    logger.debug("Complete stack trace for unknown error while creating type {}", type, e);
                }
            }
        }

        kieContainerInstance.addExtraClasses(extraClasses);
        this.rulesExecutionService.createRuleBasePackage(id, kieContainerInstance);

    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> map) {
            //Nop
    }

    @Override
    public boolean isUpdateContainerAllowed(String s, KieContainerInstance kieContainerInstance, Map<String, Object> map) {
        logger.info("Container {} isUpdateContainerAllowed",s);
        return true;
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        logger.info("Container {} dispose",id);
        if (kieServerAddOnElement != null) {

            for (KieServerGlobalVariableDefinition kieServerGlobalVariableDefinition : kieServerAddOnElement.getKieServerGlobalVariableDefinitions()) {
                kieServerGlobalVariableDefinition.OnDisposeKieBase();
            }
            for (KieServerListenerDefinition kieServerListenerDefinition : kieServerAddOnElement.getKieServerListenerDefinitions()) {
                kieServerListenerDefinition.OnDisposeKieBase();
            }
            for (KieServerLoggingDefinition kieServerLoggingDefinition : kieServerAddOnElement.getKieServerLoggingDefinitions()) {
                kieServerLoggingDefinition.OnDisposeKieBase();
            }
        }
        kieContainerInstance.clearExtraClasses();
        rulesExecutionService.removeRuleBasePackage(id);
        if (kieContainerInstance instanceof KieContainerInstanceImpl) {
            KieContainerInstanceImpl internalContainer = (KieContainerInstanceImpl) kieContainerInstance;


             if (internalContainer.getKieContainer().getMainKieModule() instanceof InternalKieModule) {
                InternalKieModule kie = (InternalKieModule) internalContainer.getKieContainer().getMainKieModule();
                kie.getFile().delete();
            }
        }
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
                = ServiceLoader.load(KieServerApplicationComponentsService.class);
        List<Object> appComponentsList = new ArrayList<>();
        Object[] serviceList = {
                rulesExecutionService,
                registry

        };
        for (KieServerApplicationComponentsService appComponentsService : appComponentsServices) {
            appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, serviceList));
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if (serviceType.isAssignableFrom(rulesExecutionService.getClass())) {
            return (T) rulesExecutionService;
        }

        return null;
    }

    @Override
    public String getImplementedCapability() {
        return "BRules";
    }

    @Override
    public List<Object> getServices() {
        return services;
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Integer getStartOrder() {
        return 0;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }

    public DroolsChtijbugRulesExecutionService getRulesExecutionService() {
        return rulesExecutionService;
    }

    protected void addExtraClass(Set<Class<?>> extraClasses, Class classToAdd, boolean filtered) {

        if (classToAdd.isInterface()
                || classToAdd.isAnnotation()
                || classToAdd.isLocalClass()
                || classToAdd.isMemberClass()) {
            return;
        }

        if (filtered) {
            boolean jaxbClass = false;
            boolean remoteableClass = false;
            // @XmlRootElement and @XmlType may be used with inheritance
            for (Annotation anno : classToAdd.getAnnotations()) {
                if (XmlRootElement.class.equals(anno.annotationType())) {
                    jaxbClass = true;
                    break;
                }
                if (XmlType.class.equals(anno.annotationType())) {
                    jaxbClass = true;
                    break;
                }
            }
            // @Remotable is not inheritable, and may not be used as such
            for (Annotation anno : classToAdd.getDeclaredAnnotations()) {
                if (Remotable.class.equals(anno.annotationType())) {
                    remoteableClass = true;
                    break;
                }
            }

            if (jaxbClass || remoteableClass) {
                extraClasses.add(classToAdd);
            }
        } else {
            extraClasses.add(classToAdd);
        }

    }
}
