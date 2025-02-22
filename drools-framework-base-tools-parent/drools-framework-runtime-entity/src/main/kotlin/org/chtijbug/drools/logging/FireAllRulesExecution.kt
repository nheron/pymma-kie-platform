package org.chtijbug.drools.logging

import java.util.*

class FireAllRulesExecution {

    var startDate: Date? = null
    var endDate: Date? = null

    var nbreRulesFired: Long? = null
    var maxNbreRulesDefinedForSession: Long? = null

    var executionTime: Long? = null


    var startEventID: Long? = null


    var fireAllRulesExecutionStatus: FireAllRulesExecutionStatus? = null


    var stopEventID: Long? = null

    var maxRulesEventID: Long? = null

}