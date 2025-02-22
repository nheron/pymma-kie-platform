package org.chtijbug.drools.entity

class DroolsProcessInstanceObject {

val serialVersionUID: Long = 5436434746711988139L
    var id: String? = null
    var process: DroolsProcessObject? = null
    var nodeInstances: MutableMap<String?, DroolsNodeInstanceObject>? = null
    var name: String? = null
    var packageName: String? = null
    var type: String? = null
    var version: String? = null

    /**
     *
     */
    constructor() {
    }

    constructor(id: String?, process: DroolsProcessObject) {
        this.id = id
        this.name = process.name
        this.packageName = process.packageName
        this.type = process.type
        this.version = process.version
        this.process = process
        nodeInstances = HashMap()
    }
    companion object {
        fun createDroolsProcessInstanceObject(
            processInstanceID: String?,
            process: DroolsProcessObject
        ): DroolsProcessInstanceObject {
            return org.chtijbug.drools.entity.DroolsProcessInstanceObject(processInstanceID, process)
        }
    }



    fun addDroolsNodeInstanceObject(droolsNodeInstanceObject: DroolsNodeInstanceObject) {
        nodeInstances!![droolsNodeInstanceObject.id] = droolsNodeInstanceObject
    }

    fun getDroolsNodeInstanceObjet(id: String?): DroolsNodeInstanceObject? {
        return nodeInstances!![id]
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as DroolsProcessInstanceObject
        if (if (this.id == null) (other.id != null) else (this.id != other.id)) {
            return false
        }
        if (this.process !== other.process && (this.process == null || this.process != other.process)) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 17 * hash + (if (this.id != null) id.hashCode() else 0)
        hash = 17 * hash + (if (this.process != null) process.hashCode() else 0)
        return hash
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append("DroolsProcessInstanceObject")
        sb.append("{id='").append(id).append('\'')
        sb.append(", name='").append(name).append('\'')
        sb.append(", packageName='").append(packageName).append('\'')
        sb.append(", type='").append(type).append('\'')
        sb.append(", version='").append(version).append('\'')
        sb.append(", process=").append(process)
        sb.append(", nodeInstances=").append(nodeInstances)
        sb.append('}')
        return sb.toString()
    }


}