package org.chtijbug.drools.logging

import java.util.*

class SessionExecution {
    var id: Long? = null
    var sessionId: Long? = null
    var startDate: Date? = null
    var endDate: Date? = null
    var startEventID: Long? = null
    var stopEventID: Long? = null

    var platformRuntimeMode = PlatformRuntimeMode.Debug


    var sessionExecutionStatus: SessionExecutionStatus? = null

    var ruleExecutions: List<RuleExecution> = ArrayList()

    var processExecutions: List<ProcessExecution> = ArrayList()

    var fireAllRulesExecutions: List<FireAllRulesExecution> = ArrayList()

    var facts: List<Fact> = ArrayList()
    var processingStartDate: Date? = null
    var processingStopDate: Date? = null

}