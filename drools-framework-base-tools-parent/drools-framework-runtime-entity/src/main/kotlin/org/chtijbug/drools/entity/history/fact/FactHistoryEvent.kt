package org.chtijbug.drools.entity.history.fact

import org.chtijbug.drools.entity.history.HistoryEvent
import java.util.*

open class FactHistoryEvent : HistoryEvent {


    private var ruleName: String? = null
    private var rulePackageName: String? = null
    private var ruleflowGroup: String? = null

    /**
     * Mandatory for GWT Serialization
     */
    constructor() {
    }

    constructor(eventID: Long?, dateEvent: Date?, ruleBaseId: Long?, sessionId: Long?) :  super(eventID, dateEvent, TypeEvent.Fact){

        this.ruleBaseID = ruleBaseId
        this.sessionId = sessionId
    }

    fun getRuleName(): String? {
        return ruleName
    }

    fun setRuleName(ruleName: String?) {
        this.ruleName = ruleName
    }

    fun getRulePackageName(): String? {
        return rulePackageName
    }

    fun setRulePackageName(rulePackageName: String?) {
        this.rulePackageName = rulePackageName
    }

    fun getRuleflowGroup(): String? {
        return ruleflowGroup
    }

    fun setRuleflowGroup(ruleflowGroup: String?) {
        this.ruleflowGroup = ruleflowGroup
    }



}
