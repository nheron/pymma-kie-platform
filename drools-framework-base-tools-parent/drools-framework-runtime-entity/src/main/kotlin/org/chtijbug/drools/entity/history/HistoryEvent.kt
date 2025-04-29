package org.chtijbug.drools.entity.history

import org.chtijbug.drools.runtime.DroolsChtijbugException
import java.io.Serializable
import java.util.*

open class HistoryEvent : Serializable{

    val serialVersionUID: Long = -6640538290066213804L
     var dateEvent: Date? = null
     var typeEvent: TypeEvent? = null
     var eventID: Long? = null
     var ruleBaseID: Long? = null
     var sessionId: Long? = null
     var droolsChtijbugException: DroolsChtijbugException? = null
     var knowledgeResources = ArrayList<KnowledgeResource>()
     var businessClassLoader: ClassLoader? = null

    /**
     * Mandatory for GWT Serialization
     */
    constructor() {
    }

    constructor(eventID: Long?, dateEvent: Date?, typeEvent: TypeEvent?) {
        this.eventID = eventID
        this.dateEvent = dateEvent
        this.typeEvent = typeEvent
    }

    enum class TypeEvent {
        Fact, Rule, BPMN, RuleFlowGroup, KnowledgeBaseSingleton, Session
    }
}
