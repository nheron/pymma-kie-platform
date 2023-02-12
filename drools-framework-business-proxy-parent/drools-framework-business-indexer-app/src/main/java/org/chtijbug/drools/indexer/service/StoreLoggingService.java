package org.chtijbug.drools.indexer.service;

import org.chtijbug.drools.ChtijbugObjectRequest;
import org.chtijbug.drools.SessionContext;
import org.chtijbug.drools.common.KafkaTopicConstants;
import org.chtijbug.drools.logging.*;
import org.chtijbug.drools.proxy.persistence.model.BusinessTransactionAction;
import org.chtijbug.drools.proxy.persistence.model.BusinessTransactionPersistence;
import org.chtijbug.drools.proxy.persistence.model.EventType;
import org.chtijbug.drools.proxy.persistence.repository.BusinessTransactionActionRepository;
import org.chtijbug.drools.proxy.persistence.repository.BusinessTransactionPersistenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("storeService")
public class StoreLoggingService {

    private static final Logger logger = LoggerFactory.getLogger(StoreLoggingService.class);

    @Autowired
    private BusinessTransactionPersistenceRepository transactionRepository;

    @Autowired
    private BusinessTransactionActionRepository actionRepository;


    @KafkaListener(
            topics = KafkaTopicConstants.LOGING_TOPIC, groupId = "${kafka.index.groupid}",
            containerFactory = "ruleKafkaListenerContainerFactory")
    public void store(ChtijbugObjectRequest result) {
        if (result != null) {

            Integer year = result.getTransactionStartTimeStamp().getYear();
            Integer month = result.getTransactionStartTimeStamp().getMonthValue();
            Integer day = result.getTransactionStartTimeStamp().getDayOfMonth();
            Integer hour = result.getTransactionStartTimeStamp().getHour();
            Integer minute = result.getTransactionStartTimeStamp().getMinute();
            Integer second = result.getTransactionStartTimeStamp().getSecond();
            Integer millis = result.getTransactionStartTimeStamp().getNano();
            String id = result.getTransactionID();
            BusinessTransactionPersistence item = new BusinessTransactionPersistence();
            item.setYear(year);
            item.setMonth(month);
            item.setDay(day);
            item.setHour(hour);
            item.setMinute(minute);
            item.setSecond(second);
            item.setMillis(millis);
            item.setTransactionId(id);

            long ii = 1;

            SessionContext sessionContext = result.getSessionLogging();
            item.setContainerId(sessionContext.getContainerId());
            item.setGroupID(sessionContext.getGroupID());
            item.setArtefactID(sessionContext.getArtefactID());
            item.setVersion(sessionContext.getVersion());

            item.setServerName(sessionContext.getServerName());
            Map<Long, BusinessTransactionAction> actions = new HashMap<>();
            SessionExecution sessionExecution = sessionContext.getSessionExecution();
            if (sessionExecution != null) {
                BusinessTransactionAction businessTransactionoutput = null;
                for (Fact fact : sessionExecution.getFacts()) {
                    BusinessTransactionAction businessTransactionAction = new BusinessTransactionAction();

                    businessTransactionAction.setBusinessTransactionId(item.getTransactionId());
                    if (fact.getFactType().equals(FactType.INPUTDATA)) {
                        businessTransactionAction.setEventType(EventType.INPUT);
                        businessTransactionAction.setInputData(fact);
                        businessTransactionAction.setEventNumber(0);
                        actions.put(businessTransactionAction.getEventNumber(), businessTransactionAction);
                    } else if (fact.getFactType().equals(FactType.OUTPUTDATA)) {
                        businessTransactionAction.setEventType(EventType.OUPUT);
                        businessTransactionAction.setOutputData(fact);
                        businessTransactionoutput = businessTransactionAction;

                    } else if (fact.getFactType().equals(FactType.INSERTED)) {
                        businessTransactionAction.setEventType(EventType.INSERTFACT);
                        businessTransactionAction.setFact(fact);
                        businessTransactionAction.setEventNumber(ii++);
                        actions.put(businessTransactionAction.getEventNumber(), businessTransactionAction);
                    } else if (fact.getFactType().equals(FactType.UPDATED_NEWVALUE)) {
                        businessTransactionAction.setEventType(EventType.UPDATEFACTNEWVALUE);
                        businessTransactionAction.setFact(fact);
                        businessTransactionAction.setEventNumber(ii++);
                        actions.put(businessTransactionAction.getEventNumber(), businessTransactionAction);
                    } else if (fact.getFactType().equals(FactType.UPDATED_OLDVALUE)) {
                        businessTransactionAction.setEventType(EventType.UPDATEFACTOLDVALUE);
                        businessTransactionAction.setFact(fact);
                        businessTransactionAction.setEventNumber(ii++);
                        actions.put(businessTransactionAction.getEventNumber(), businessTransactionAction);
                    } else if (fact.getFactType().equals(FactType.DELETED)) {
                        businessTransactionAction.setEventType(EventType.RETRACTFACT);
                        businessTransactionAction.setFact(fact);
                        businessTransactionAction.setEventNumber(ii++);
                        actions.put(businessTransactionAction.getEventNumber(), businessTransactionAction);
                    }
                }
                for (ProcessExecution processExecution : sessionExecution.getProcessExecutions()) {
                    BusinessTransactionAction businessTransactionActionStart = new BusinessTransactionAction();

                    businessTransactionActionStart.setEventType(EventType.STARTPROCESS);
                    businessTransactionActionStart.setProcessID(processExecution.getProcessId());

                    businessTransactionActionStart.setBusinessTransactionId(item.getTransactionId());
                    businessTransactionActionStart.setEventNumber(ii++);

                    actions.put(businessTransactionActionStart.getEventNumber(), businessTransactionActionStart);
                    for (RuleflowGroup rfg : processExecution.getRuleflowGroups()) {
                        BusinessTransactionAction businessTransactionActionStartRFG = new BusinessTransactionAction();

                        businessTransactionActionStartRFG.setBusinessTransactionId(item.getTransactionId());
                        businessTransactionActionStartRFG.setEventType(EventType.STARTRULEFLOWGROUP);
                        businessTransactionActionStartRFG.setRuleflowGroupName(rfg.getRuleflowGroup());
                        businessTransactionActionStartRFG.setEventNumber(ii++);

                        actions.put(businessTransactionActionStartRFG.getEventNumber(), businessTransactionActionStartRFG);
                        for (RuleExecution ruleExecution : rfg.getRuleExecutionList()) {
                            BusinessTransactionAction businessTransactionActionRule = new BusinessTransactionAction();

                            businessTransactionActionRule.setEventType(EventType.RULE);
                            businessTransactionActionRule.setRuleflowGroupName(rfg.getRuleflowGroup());
                            businessTransactionActionRule.setRuleExecution(ruleExecution);
                            businessTransactionActionRule.setBusinessTransactionId(item.getTransactionId());
                            businessTransactionActionRule.setProcessID(processExecution.getProcessId());
                            businessTransactionActionRule.setEventNumber(ii++);

                            actions.put(businessTransactionActionRule.getEventNumber(), businessTransactionActionRule);
                        }
                        BusinessTransactionAction businessTransactionActionSTOPRFG = new BusinessTransactionAction();

                        businessTransactionActionSTOPRFG.setEventType(EventType.STOPTRULEFLOWGROUP);
                        businessTransactionActionSTOPRFG.setRuleflowGroupName(rfg.getRuleflowGroup());
                        businessTransactionActionSTOPRFG.setBusinessTransactionId(item.getTransactionId());
                        businessTransactionActionSTOPRFG.setEventNumber(ii++);

                        actions.put(businessTransactionActionSTOPRFG.getEventNumber(), businessTransactionActionSTOPRFG);
                    }
                    BusinessTransactionAction businessTransactionActionEnd = new BusinessTransactionAction();
                    businessTransactionActionEnd.setEventType(EventType.STOPPROCESS);
                    businessTransactionActionEnd.setProcessID(processExecution.getProcessId());
                    businessTransactionActionEnd.setBusinessTransactionId(item.getTransactionId());
                    businessTransactionActionEnd.setEventNumber(ii++);


                    actions.put(businessTransactionActionEnd.getEventNumber(), businessTransactionActionEnd);
                }

                if (businessTransactionoutput != null) {
                    businessTransactionoutput.setEventNumber(ii++);
                    actions.put(businessTransactionoutput.getEventNumber(), businessTransactionoutput);
                }
            }
            List<Long> keys = new ArrayList<>(actions.keySet());
            Collections.sort(keys);
            List<BusinessTransactionAction> sortedList = new LinkedList<>();
            for (Long i : keys) {
                sortedList.add(actions.get(i));
            }
            transactionRepository.save(item);
            actionRepository.saveAll(sortedList);
            logger.info("Logging transaction {}", result.getTransactionID());
        }
    }
}
