package org.chtijbug.drools.logging

import java.util.*

class RuleflowGroup {
    var ruleflowGroup: String? = null


    var ruleExecutionList = LinkedList<RuleExecution>()


    var startDate: Date? = null

    var endDate: Date? = null

    var startEventID: Long? = null

    var stopEventID: Long? = null


    var ruleflowGroupStatus: RuleflowGroupStatus? = null


}