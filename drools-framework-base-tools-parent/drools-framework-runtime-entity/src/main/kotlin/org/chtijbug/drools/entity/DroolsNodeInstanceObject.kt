package org.chtijbug.drools.entity

import java.io.Serializable

class DroolsNodeInstanceObject : Serializable {

    val serialVersionUID: Long = 9121172218519979577L
    var id: String? = null
    var node: DroolsNodeObject? = null

    constructor() {
    }

    constructor(id: String?, node: DroolsNodeObject?) {
        this.id = id
        this.node = node
    }

    companion object {

        fun createDroolsNodeInstanceObject(
            nodeInstanceId: String?,
            nodeObject: DroolsNodeObject?
        ): DroolsNodeInstanceObject {
            return org.chtijbug.drools.entity.DroolsNodeInstanceObject(nodeInstanceId, nodeObject)
        }
    }


    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as DroolsNodeInstanceObject
        if (if (this.id == null) (other.id != null) else (this.id != other.id)) {
            return false
        }
        if (this.node !== other.node && (this.node == null || this.node != other.node)) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var hash = 5
        hash = 71 * hash + (if (this.id != null) id.hashCode() else 0)
        hash = 71 * hash + (if (this.node != null) node.hashCode() else 0)
        return hash
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append("DroolsNodeInstanceObject")
        sb.append("{id='").append(id).append('\'')
        sb.append(", node=").append(node.toString())
        sb.append('}')
        return sb.toString()
    }


}