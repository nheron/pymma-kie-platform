package org.chtijbug.drools.runtimeevent

import com.rits.cloning.Cloner
import com.rits.cloning.ObjenesisInstantiationStrategy
import org.chtijbug.drools.SessionContext
import org.chtijbug.drools.entity.history.HistoryEvent
import org.chtijbug.drools.runtimeevent.impl.fact.*
import org.chtijbug.drools.runtimeevent.impl.knowledgeSession.*
import org.chtijbug.drools.runtimeevent.impl.process.*
import org.chtijbug.drools.runtimeevent.impl.rule.AfterRuleFiredEventStrategy
import org.chtijbug.drools.runtimeevent.impl.rule.AfterRuleflowGroupActivatedEventStrategy
import org.chtijbug.drools.runtimeevent.impl.rule.AfterRuleflowGroupDeactivatedEventStrategy
import org.chtijbug.drools.runtimeevent.impl.rule.BeforeRuleFiredEventStrategy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.annotation.Resource

class MessageHandlerResolver {


    val logger: Logger = LoggerFactory.getLogger(MessageHandlerResolver::class.java)

    @Resource
    private val allMemoryStrategies: MutableList<AbstractMemoryEventHandlerStrategy> = ArrayList()

    private var classLoader: ClassLoader? = null

    fun MessageHandlerResolver() {
        allMemoryStrategies.add(DeleteFactEventStrategy())
        allMemoryStrategies.add(InsertedByRelectionFactEndEventStrategy())
        allMemoryStrategies.add(InsertedByRelectionFactStartEventStrategy())
        allMemoryStrategies.add(InsertedFactEventStrategy())
        allMemoryStrategies.add(UpdatedFactEventStrategy())
        allMemoryStrategies.add(KnowledgeSessionCreateEventStrategy())
        allMemoryStrategies.add(KnowledgeSessionDisposeEventStrategy())
        allMemoryStrategies.add(KnowledgeSessionFireAllRulesAndStartProcessEventStrategy())
        allMemoryStrategies.add(KnowledgeSessionFireAllRulesBeginEventStrategy())
        allMemoryStrategies.add(KnowledgeSessionFireAllRulesEndEventStrategy())
        allMemoryStrategies.add(KnowledgeSessionFireAllRulesMaxRulesEventStrategy())
        allMemoryStrategies.add(KnowledgeSessionProcessBeginEventStrategy())
        allMemoryStrategies.add(KnowledgeSessionProcessEndEventStrategy())
        allMemoryStrategies.add(AfterNodeInstanceTriggeredEventStrategy())
        allMemoryStrategies.add(AfterNodeLeftEventStrategy())
        allMemoryStrategies.add(AfterProcessEndHistoryEventStrategy())
        allMemoryStrategies.add(AfterProcessStartEventStrategy())
        allMemoryStrategies.add(AfterVariableChangeEventStrategy())
        allMemoryStrategies.add(BeforeNodeInstanceTriggeredEventStrategy())
        allMemoryStrategies.add(BeforeNodeLeftEventStrategy())
        allMemoryStrategies.add(BeforeProcessEndEventStrategy())
        allMemoryStrategies.add(BeforeProcessStartEventStrategy())
        allMemoryStrategies.add(BeforeVariableChangeEventStrategy())
        allMemoryStrategies.add(AfterRuleFiredEventStrategy())
        allMemoryStrategies.add(AfterRuleflowGroupActivatedEventStrategy())
        allMemoryStrategies.add(AfterRuleflowGroupDeactivatedEventStrategy())
        allMemoryStrategies.add(BeforeRuleFiredEventStrategy())
    }

    fun getSessionFromHistoryEvent(historyEvents: List<HistoryEvent>): SessionContext {
        val currentThread = Thread.currentThread()
        val old = currentThread.contextClassLoader
        currentThread.contextClassLoader = classLoader
        val sessionContext = SessionContext()
        val cloner = Cloner(ObjenesisInstantiationStrategy())
        for (historyEvent in historyEvents) {
            val strategy = this.resolveMessageHandlerMemory(historyEvent)
            if (strategy != null) {
                try {
                    strategy.handleMessageInternally(historyEvent, sessionContext, cloner)
                } catch (e: Exception) {
                    logger.error("MessageHandle for class" + historyEvent.javaClass.toString(), historyEvent, e)
                }
            }
        }

        sessionContext.ruleflowGroups.clear();
        currentThread.contextClassLoader = old
        return sessionContext
    }

    fun resolveMessageHandlerMemory(historyEvent: HistoryEvent?): AbstractMemoryEventHandlerStrategy? {
        for (strategy in allMemoryStrategies) {
            if (strategy.isEventSupported(historyEvent)) return strategy
        }
        return null
    }

    fun setClassLoader(classLoader: ClassLoader?) {
        this.classLoader = classLoader
    }


}