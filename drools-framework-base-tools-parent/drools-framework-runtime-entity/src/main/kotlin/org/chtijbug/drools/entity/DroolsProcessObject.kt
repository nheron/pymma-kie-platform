package org.chtijbug.drools.entity

class DroolsProcessObject {

    val serialVersionUID: Long = 4718079763911002405L
    var id: String? = null
    var name: String? = null
    var packageName: String? = null
    var type: String? = null
    var version: String? = null
    var nodeLists: MutableMap<String?, DroolsNodeObject>? = null

    constructor() {
    }

    constructor(id: String?, name: String?, packageName: String?, type: String?, version: String?) {
        this.id = id
        this.name = name
        this.packageName = packageName
        this.type = type
        this.version = version
        this.nodeLists = HashMap()
    }

    companion object {

    fun createDroolsProcessObject(
        id: String?,
        processInstanceID: String?,
        packageName: String?,
        type: String?,
        version: String?
    ): DroolsProcessObject {
        return org.chtijbug.drools.entity.DroolsProcessObject(id, processInstanceID, packageName, type, version)
    }
}

    fun addDroolsNodeObject(droolsNodeObject: DroolsNodeObject) {
        nodeLists!![droolsNodeObject.id] = droolsNodeObject
    }

    fun getDroolsNodeObjet(id: String?): DroolsNodeObject? {
        return nodeLists!![id]
    }



    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as DroolsProcessObject
        if (if (this.id == null) (other.id != null) else (this.id != other.id)) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var hash = 3
        hash = 41 * hash + (if (this.name != null) name.hashCode() else 0)
        hash = 41 * hash + (if (this.packageName != null) packageName.hashCode() else 0)
        return hash
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append("DroolsProcessObject")
        sb.append("{id='").append(id).append('\'')
        sb.append(", name='").append(name).append('\'')
        sb.append(", packageName='").append(packageName).append('\'')
        sb.append(", type='").append(type).append('\'')
        sb.append(", version='").append(version).append('\'')
        sb.append(", nodeLists=").append(nodeLists)
        sb.append('}')
        return sb.toString()
    }



}