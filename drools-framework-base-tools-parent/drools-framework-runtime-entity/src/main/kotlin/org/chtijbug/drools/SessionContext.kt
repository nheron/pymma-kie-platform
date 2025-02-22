package org.chtijbug.drools

import org.chtijbug.drools.logging.*
import java.util.*


class SessionContext {
    var sessionExecution: SessionExecution? = null

    var processExecution: ProcessExecution? = null


    var ruleflowGroups: ArrayList<RuleflowGroup> = ArrayList()

    var ruleExecution: RuleExecution? = null

    var fireAllRulesExecution: FireAllRulesExecution? = null

    var fact: Fact? = null

    var groupID: String? = null

    var artefactID: String? = null

    var version: String? = null

    var containerId: String? = null

    var serverName: String? = null

    var startTime: Date? = null

    var stopTime: Date? = null


    fun findRuleFlowGroup(name: String): RuleflowGroup? {
        var result: RuleflowGroup? = null
        for (r in ruleflowGroups) {
            if (r.ruleflowGroup != null && r.ruleflowGroup == name) {
                result = r
            }
        }

        return result
    }

}