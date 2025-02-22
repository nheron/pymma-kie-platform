package org.chtijbug.drools.logging

import java.util.*

class ProcessExecution {

    var processName: String? = null


    var startDate: Date? = null
    var endDate: Date? = null


    var processExecutionStatus: ProcessExecutionStatus? = null

    var startEventID: Long? = null

    var stopEventID: Long? = null


    var ProcessInstanceId: String? = null

    var processPackageName: String? = null

    var processVersion: String? = null

    var processType: String? = null

    var processId: String? = null


    var ruleflowGroups = LinkedList<RuleflowGroup>()

}