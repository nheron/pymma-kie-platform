package org.chtijbug.drools.entity

class DroolsRuleFlowGroupObject {

    var ruleFlowInstanceID = 0
    var name: String? = null

    constructor() {
    }

    constructor(ruleFlowInstanceID: Int, name: String?) {
        this.ruleFlowInstanceID = ruleFlowInstanceID
        this.name = name
    }


    override fun toString(): String {
        val sb = StringBuffer()
        sb.append("DroolsRuleFlowGroupObject")
        sb.append("{ruleFlowInstanceID=").append(ruleFlowInstanceID)
        sb.append(", name='").append(name).append('\'')
        sb.append('}')
        return sb.toString()
    }
}