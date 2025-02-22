package org.chtijbug.drools.entity

class DroolsJbpmVariableObject {

    @Transient
    private var variableValue: Any? = null
    private var variableId: String? = null
    private var variableInstanceId: String? = null

    constructor() {
        this.variableValue = null
    }

    constructor(variableId: String?, variableInstanceId: String?, variableValue: Any?) {
        this.variableId = variableId
        this.variableInstanceId = variableInstanceId
        this.variableValue = variableValue
    }


    override fun toString(): String {
        val sb = StringBuffer("DroolsJbpmVariableObject{")
        sb.append("variableId='").append(variableId).append('\'')
        sb.append(", variableValue=").append(variableValue)
        sb.append('}')
        return sb.toString()
    }


}