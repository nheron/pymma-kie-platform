package org.chtijbug.drools.proxy.service;

import org.chtijbug.drools.ChtijbugObjectRequest;
import org.chtijbug.drools.common.KafkaTopicConstants;
import org.chtijbug.drools.kieserver.extension.KieServerAddOnElement;
import org.chtijbug.drools.kieserver.extension.KieServerLoggingDefinition;
import org.kie.server.services.api.KieContainerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;

@Service("ruleService")
public class RuleService {

    private static final Logger logger = LoggerFactory.getLogger(RuleService.class);
    @Inject
    private KieServiceCommon kieServiceCommon;

    @Inject
    private KafkaTemplate<String, ChtijbugObjectRequest> kafkaTemplateLogging;

    public RuleService() {
        logger.info("Rule Service created");
    }

    public Object runSessionObject(String transactionID, String id, String processID, Object input) {

        ChtijbugObjectRequest chtijbugObjectRequest = new ChtijbugObjectRequest();
        chtijbugObjectRequest.setTransactionID(transactionID);
        chtijbugObjectRequest.setProcessID(processID);
        chtijbugObjectRequest.setContainerID(id);
        chtijbugObjectRequest.setTransactionStartTimeStamp(LocalDateTime.now());
        KieContainerInstance kci = kieServiceCommon.getRegistry().getContainer(id);
        chtijbugObjectRequest.setArtifactID(kci.getKieContainer().getReleaseId().getArtifactId());
        chtijbugObjectRequest.setGroupID(kci.getKieContainer().getReleaseId().getGroupId());
        chtijbugObjectRequest.setVersion(kci.getKieContainer().getReleaseId().getVersion());
        chtijbugObjectRequest.setObjectRequest(input);
        KieServerAddOnElement kieServerAddOnElement = kieServiceCommon.getDroolsChtijbugRulesExecutionService().getKieServerAddOnElement();
        if (kieServerAddOnElement != null) {
            for (KieServerLoggingDefinition kieServerLoggingDefinition : kieServerAddOnElement.getKieServerLoggingDefinitions()) {
                kieServerLoggingDefinition.OnFireAllrulesStart(kci.getKieContainer().getReleaseId().getGroupId(), kci.getKieContainer().getReleaseId().getArtifactId(), kci.getKieContainer().getReleaseId().getVersion(), input);
            }
        }
        ChtijbugObjectRequest chtijbutObjectResponse = kieServiceCommon.getDroolsChtijbugRulesExecutionService().FireAllRulesAndStartProcess(kci, chtijbugObjectRequest, processID);
        /**
         * remove facts from logging to avoid infinite loop when marshalling to json and size of logging
         */
        if (kieServerAddOnElement != null) {

            for (KieServerLoggingDefinition kieServerLoggingDefinition : kieServerAddOnElement.getKieServerLoggingDefinitions()) {
                kieServerLoggingDefinition.OnFireAllrulesEnd(kci.getKieContainer().getReleaseId().getGroupId(), kci.getKieContainer().getReleaseId().getArtifactId(), kci.getKieContainer().getReleaseId().getVersion(), chtijbutObjectResponse.getObjectRequest(), chtijbutObjectResponse.getSessionLogging());
            }
        }
        chtijbugObjectRequest.setTransactionEndTimeStamp(LocalDateTime.now());
        try {
            kafkaTemplateLogging.send(KafkaTopicConstants.LOGING_TOPIC,chtijbugObjectRequest);
        }catch (Exception e){
            logger.error(" kafkaTemplateLogging.send",e);
        }finally {
            return  chtijbutObjectResponse.getObjectRequest();
        }

    }

}
