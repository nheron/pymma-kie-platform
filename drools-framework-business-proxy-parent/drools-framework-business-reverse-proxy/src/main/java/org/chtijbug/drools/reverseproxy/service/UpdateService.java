package org.chtijbug.drools.reverseproxy.service;

import com.github.mkopylec.charon.configuration.MappingProperties;
import org.chtijbug.drools.ReverseProxyUpdate;
import org.chtijbug.drools.common.KafkaTopicConstants;
import org.chtijbug.drools.proxy.persistence.model.ProjectPersist;
import org.chtijbug.drools.proxy.persistence.model.RuntimePersist;
import org.chtijbug.drools.proxy.persistence.repository.ProjectRepository;
import org.chtijbug.drools.proxy.persistence.repository.RuntimeRepository;
import org.chtijbug.drools.reverseproxy.mappings.CustomMappingsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service("updateService")
public class UpdateService {
    private static final Logger logger = LoggerFactory.getLogger(UpdateService.class);

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private RuntimeRepository runtimeRepository;

    private Map<String, RuntimePersist> runtimes = new HashMap<>();
    private Map<String, ProjectPersist> projects = new HashMap<>();

    private Boolean toUpdate = true;



    private Map<String, MappingProperties> mappingPropertiesMap = new HashMap<>();
    private Map<String, MappingProperties> mappingJWTPropertiesMap = new HashMap<>();
    @Autowired
    private CustomMappingsProvider customMappingsProvider;

    public Boolean getToUpdate() {
        return toUpdate;
    }


    @KafkaListener(
            topics = KafkaTopicConstants.REVERSE_PROXY,
            containerFactory = "mappingKafkaListenerContainerFactory")
    public void store(ReverseProxyUpdate update) {
        boolean found = false;
        MappingProperties mappingProperties = null;
        if (update.getTokenUUID() != null && update.getTokenUUID().length() > 0) {
            mappingProperties = mappingJWTPropertiesMap.get(update.getTokenUUID());
            if (mappingProperties != null) {
                found = true;
            }
        } else {
            mappingProperties = mappingPropertiesMap.get(UpdateService.removeSlach(update.getPath()));
            if (mappingProperties != null) {
                found = true;
            }
        }
        if (found) {
            mappingProperties.getDestinations().clear();
            logger.info("Updating path {}", update.getPath());
            for (String destination : update.getServerNames()) {
                mappingProperties.getDestinations().add(destination);
                logger.info("for path {} adding server {} ", update.getPath(), destination);
            }
        } else {
            MappingProperties newMappingProperties = new MappingProperties();

            if (update.getTokenUUID() != null && update.getTokenUUID().length() > 0) {
                mappingJWTPropertiesMap.put(update.getTokenUUID(), newMappingProperties);
            } else {
                newMappingProperties.setPath(UpdateService.removeSlach(update.getPath()));
                logger.info("Creating path {}", update.getPath());
                mappingPropertiesMap.put(UpdateService.removeSlach(update.getPath()), newMappingProperties);
            }
            newMappingProperties.setName(update.getContainerID());

            newMappingProperties.getCustomConfiguration().put("connect", 2000);
            newMappingProperties.getCustomConfiguration().put("read", 2000);
            newMappingProperties.setStripPath(true);
            for (String destination : update.getServerNames()) {
                newMappingProperties.getDestinations().add(destination);
                logger.info("for path {} adding server {} ", update.getPath(), destination);
            }
        }

        this.toUpdate = true;
    }



    public static String removeSlach(String target) {
        if (target != null) {
            return target.replace("/", "").replace(" ", "");
        }
        return null;
    }

    private void generateMappings() {
        projects.clear();
        mappingPropertiesMap.clear();
        List<MappingProperties> paths = new ArrayList<>();
        Collection<ProjectPersist> projectPersists = projectRepository.findAll();
        Map<String, String> urlMap = new HashMap<>();
        List<RuntimePersist> runtimePersists = runtimeRepository.findAll();
        for (RuntimePersist runtimePersist : runtimePersists) {
            if (urlMap.containsKey(runtimePersist.getServerName()) == false) {
                urlMap.put(runtimePersist.getServerName(), runtimePersist.getServerUrl());
                runtimes.put(runtimePersist.getServerName(), runtimePersist.duplicate());
            }
        }
        for (ProjectPersist projectPersist : projectPersists) {
            if (projectPersist.getServerNames().size() > 0) {
                projects.put(projectPersist.getContainerID(), projectPersist.duplicate());
                MappingProperties mappingProperties2 = new MappingProperties();
                String servList = null;
                for (String serverName : projectPersist.getServerNames()) {
                    RuntimePersist runtimePersist = runtimes.get(serverName);
                    if (runtimePersist != null) {
                        String hostName = runtimePersist.getServerUrl() + "/api/" + projectPersist.getContainerID();
                        mappingProperties2.getDestinations().add(hostName);
                        if (servList == null) {
                            servList = serverName;
                        } else {
                            servList = servList + ":" + serverName;
                        }

                    }
                }
                mappingProperties2.setName(projectPersist.getContainerID());
                mappingProperties2.setPath("/" + projectPersist.getContainerID());
                mappingProperties2.getCustomConfiguration().put("connect", 2000);
                mappingProperties2.getCustomConfiguration().put("read", 2000);
                mappingProperties2.setStripPath(true);
                if (mappingProperties2.getDestinations().size() > 0) {
                    if (projectPersist.isUseJWTToConnect()) {
                        mappingJWTPropertiesMap.put(projectPersist.getUuid(), mappingProperties2);
                        paths.add(mappingProperties2);
                        logger.info("Startup creating path / and for token uuid {}", projectPersist.getUuid());
                        for (String serverName : mappingProperties2.getDestinations()) {
                            logger.info("---------for uuid {} adding server {} ", projectPersist.getUuid(), serverName);
                        }
                        logger.info("---------Project " + projectPersist.getContainerID() + " defined on servers - " + mappingProperties2.getDestinations().toString());

                    } else {
                        mappingPropertiesMap.put(UpdateService.removeSlach(mappingProperties2.getPath()), mappingProperties2);
                        paths.add(mappingProperties2);
                        logger.info("Startup creating path {}", mappingProperties2.getPath());
                        for (String serverName : mappingProperties2.getDestinations()) {
                            logger.info("---------for path {} adding server {} ", mappingProperties2.getPath(), serverName);
                        }
                        logger.info("---------Project " + projectPersist.getContainerID() + " defined on servers - " + mappingProperties2.getDestinations().toString());
                    }
                } else {
                    logger.error("Project " + projectPersist.getContainerID() + " defined on non existing server");
                }


            }

        }
        this.customMappingsProvider.setMappingPropertiesMap(mappingPropertiesMap);
        this.customMappingsProvider.setMappingJWTPropertiesMap(mappingJWTPropertiesMap);
    }

    @PostConstruct
    public void initConfig() {
        generateMappings();
    }

}
