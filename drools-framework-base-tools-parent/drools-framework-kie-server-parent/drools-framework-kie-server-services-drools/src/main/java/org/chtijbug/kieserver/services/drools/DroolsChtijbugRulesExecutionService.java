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

import org.chtijbug.drools.ChtijbugObjectRequest;
import org.chtijbug.drools.SessionContext;
import org.chtijbug.drools.kieserver.extension.KieServerAddOnElement;
import org.chtijbug.drools.kieserver.extension.KieServerGlobalVariableDefinition;
import org.chtijbug.drools.kieserver.extension.KieServerListenerDefinition;
import org.chtijbug.drools.kieserver.extension.KieServerLoggingDefinition;
import org.chtijbug.drools.runtime.DroolsChtijbugException;
import org.chtijbug.drools.runtime.RuleBasePackage;
import org.chtijbug.drools.runtime.RuleBaseSession;
import org.chtijbug.drools.runtime.impl.RuleBaseSingleton;
import org.chtijbug.drools.runtimeevent.MessageHandlerResolver;
import org.kie.api.runtime.KieContainer;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Direct rules execution service that allow use of typed objects instead of string only
 */
public class DroolsChtijbugRulesExecutionService {
    private static final Logger logger = LoggerFactory.getLogger(DroolsChtijbugRulesExecutionService.class);

    private KieServerRegistry context;
    private Map<String, RuleBasePackage> ruleBasePackages = new HashMap<>();
    private Map<String, RuleBasePackage> ruleBasePackagesClass = new HashMap<>();
    private MessageHandlerResolver messageHandlerResolver;
    private int MaxNumberRulesToExecute = 20000;
    private KieServerAddOnElement kieServerAddOnElement = null;

    public DroolsChtijbugRulesExecutionService(KieServerRegistry context, KieServerAddOnElement kieServerAddOnElement) {
        this.context = context;
        this.messageHandlerResolver = new MessageHandlerResolver();
        this.kieServerAddOnElement = kieServerAddOnElement;
    }


    public KieServerRegistry getContext() {
        return context;
    }


    public ChtijbugObjectRequest FireAllRulesAndStartProcess(KieContainerInstance kci, ChtijbugObjectRequest chtijbugObjectRequest, String processID, int sessionMaxNumberRulesToExecute) {
        return this.FireAllRulesAndStartProcess(kci, chtijbugObjectRequest, processID, null, sessionMaxNumberRulesToExecute);
    }

    public ChtijbugObjectRequest FireAllRulesAndStartProcess(KieContainerInstance kci, ChtijbugObjectRequest chtijbugObjectRequest, String processID) {
        return this.FireAllRulesAndStartProcess(kci, chtijbugObjectRequest, processID, null);
    }

    public KieServerAddOnElement getKieServerAddOnElement() {
        return kieServerAddOnElement;
    }

    public void removeRuleBasePackage(String id) {
        if (this.ruleBasePackages.containsKey(id) == true) {
            RuleBasePackage ruleBasePackage = this.ruleBasePackages.get(id);
            ruleBasePackage.dispose();
            this.ruleBasePackages.remove(id);
        }
        if (this.ruleBasePackagesClass.containsKey(id) == true) {
            this.ruleBasePackagesClass.remove(id);
        }


    }

    public void createRuleBasePackage(String id, KieContainerInstance kci) {
        if (kci != null
                && kci.getKieContainer() != null) {
            if (this.ruleBasePackages.containsKey(id) == false) {

                KieContainer kieContainer = kci.getKieContainer();
                RuleBasePackage ruleBasePackage = new RuleBaseSingleton(kieContainer, this.MaxNumberRulesToExecute, kieServerAddOnElement);
                if (kieServerAddOnElement != null) {
                    for (KieServerGlobalVariableDefinition kieServerGlobalVariableDefinition : kieServerAddOnElement.getKieServerGlobalVariableDefinitions()) {
                        kieServerGlobalVariableDefinition.OnCreateKieBase(kieServerAddOnElement.getGlobals());
                    }
                    for (KieServerListenerDefinition kieServerListenerDefinition : kieServerAddOnElement.getKieServerListenerDefinitions()) {
                        kieServerListenerDefinition.OnCreateKieBase();
                    }
                    for (KieServerLoggingDefinition kieServerLoggingDefinition : kieServerAddOnElement.getKieServerLoggingDefinitions()) {
                        kieServerLoggingDefinition.OnCreateKieBase();
                    }
                }
                this.ruleBasePackages.put(id, ruleBasePackage);

            }
        }
    }

    public ChtijbugObjectRequest FireAllRulesAndStartProcess(KieContainerInstance kci, ChtijbugObjectRequest chtijbugObjectRequest, String processID, String sessionName, int sessionMaxNumberRulesToExecute) {

        Object result = null;

        try {
            if (messageHandlerResolver==null){
                logger.error("DroolsChtijbugRulesExecutionService.FireAllRulesAndStartProcess.messageHandlerResolver is null");
            }
            if (chtijbugObjectRequest==null){
                logger.error("DroolsChtijbugRulesExecutionService.FireAllRulesAndStartProcess.chtijbugObjectRequest is null");
            }else if( chtijbugObjectRequest.getObjectRequest()==null){
                logger.error("DroolsChtijbugRulesExecutionService.FireAllRulesAndStartProcess.chtijbugObjectRequest.getObjectRequest is null");
            }
            messageHandlerResolver.setClassLoader(chtijbugObjectRequest.getObjectRequest().getClass().getClassLoader());
            RuleBasePackage ruleBasePackage = this.ruleBasePackages.get(kci.getResource().getContainerId());
            if (ruleBasePackage != null) {
                Date startTime = new Date();
                ChtijbugHistoryListener chtijbugHistoryListener = new ChtijbugHistoryListener();
                RuleBaseSession session = ruleBasePackage.createRuleBaseSession(sessionMaxNumberRulesToExecute, chtijbugHistoryListener, sessionName);
                if (kieServerAddOnElement != null) {

                    for (KieServerListenerDefinition kieServerListenerDefinition : kieServerAddOnElement.getKieServerListenerDefinitions()) {
                        kieServerListenerDefinition.OnCreateKieSession(session.getKnowledgeSession());
                    }
                    for (String globalVariableName : kieServerAddOnElement.getGlobals().keySet()) {
                        session.setGlobal(globalVariableName, kieServerAddOnElement.getGlobals().get(globalVariableName));
                    }

                }
                result = session.fireAllRulesAndStartProcess(chtijbugObjectRequest.getObjectRequest(), processID);
                session.dispose();
                Date stopTime = new Date();
                SessionContext sessionContext = this.messageHandlerResolver.getSessionFromHistoryEvent(chtijbugHistoryListener.getHistoryEventLinkedList());
                sessionContext.setGroupID(kci.getResource().getReleaseId().getGroupId());
                sessionContext.setArtefactID(kci.getResource().getReleaseId().getArtifactId());
                sessionContext.setVersion(kci.getResource().getReleaseId().getVersion());
                sessionContext.setContainerId(kci.getContainerId());
                String serverName = System.getProperty("org.kie.server.id");
                if (serverName!= null){
                    sessionContext.setServerName(serverName);
                }
                sessionContext.setStartTime(startTime);
                sessionContext.setStopTime(stopTime);
                chtijbugObjectRequest.setSessionLogging(sessionContext);
                chtijbugObjectRequest.setObjectRequest(result);
                logger.debug("Returning OK response with content '{}'", chtijbugObjectRequest.getObjectRequest());

            }
            return chtijbugObjectRequest;

        } catch (DroolsChtijbugException e) {
           logger.error("FireAllRulesAndStartProcess",e);
        }

        throw new IllegalStateException("Unable to execute command " + chtijbugObjectRequest.getObjectRequest());

    }

    public ChtijbugObjectRequest FireAllRulesAndStartProcess(KieContainerInstance kci, ChtijbugObjectRequest chtijbugObjectRequest, String processID, String sessionName) {

        return this.FireAllRulesAndStartProcess(kci, chtijbugObjectRequest, processID, sessionName, this.MaxNumberRulesToExecute);
    }
}
