package org.chtijbug.drools.entity

import java.io.Serializable

class DroolsNodeObject : Serializable {




    val serialVersionUID: Long = 2149698078767524188L
    var id: String? = null
    var nodeType = DroolsNodeType.Other

    private var ruleflowGroupName: String? = null

    /**
     *
     */
    constructor() {
    }

    constructor(id: String?, nodeType: DroolsNodeType) {
        this.id = id
        this.nodeType = nodeType
    }

    constructor(id: String?) {
        this.id = id
        this.nodeType = DroolsNodeType.Other
    }
    companion object{
        fun createDroolsNodeObject(id: String?, nodeType: DroolsNodeType): DroolsNodeObject {
            return org.chtijbug.drools.entity.DroolsNodeObject(id, nodeType)
        }
    }




    fun getRuleflowGroupName(): String? {
        return ruleflowGroupName
    }

    fun setRuleflowGroupName(ruleflowGroupName: String?) {
        if (ruleflowGroupName != null && ruleflowGroupName.length > 0) {
            this.nodeType = DroolsNodeType.RuleNode
        }
        this.ruleflowGroupName = ruleflowGroupName
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as DroolsNodeObject
        if (if (this.id == null) (other.id != null) else (this.id != other.id)) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 53 * hash + (if (this.id != null) id.hashCode() else 0)
        return hash
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append("DroolsNodeObject")
        sb.append("{id='").append(id).append('\'')
        sb.append("nodeType='").append(nodeType).append('\'')
        sb.append('}')
        return sb.toString()
    }



}