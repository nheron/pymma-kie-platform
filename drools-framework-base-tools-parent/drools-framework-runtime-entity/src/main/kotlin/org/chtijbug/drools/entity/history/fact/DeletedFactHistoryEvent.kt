package org.chtijbug.drools.entity.history.fact

import org.chtijbug.drools.entity.DroolsFactObject
import java.util.*

class DeletedFactHistoryEvent : FactHistoryEvent {



    private var deletedObject: DroolsFactObject? = null

    /**
     *
     */
    constructor()  {
    }

    constructor (eventID: Long?, deletedObject: DroolsFactObject, ruleBaseId: Long?, sessionId: Long?) :super(eventID, Date(), ruleBaseId, sessionId){

        this.deletedObject = deletedObject
    }

    fun getDeletedObject(): DroolsFactObject {
        return deletedObject!!
    }

    fun setDeletedObject(deletedObject: DroolsFactObject) {
        this.deletedObject = deletedObject
    }

    override fun toString(): String {
        val str = StringBuilder()
        str.append(super.toString() + "\n")
        str.append("retracted Object : " + deletedObject!!.getFullClassName() + "\n")
        str.append("version Object : " + deletedObject!!.getObjectVersion() + "\n")
        str.append("attributes :\n")
        for (foa in deletedObject!!.getListfactObjectAttributes()) {
            str.append("- " + foa.attributeType + " " + foa.attributeName + "=" + foa.attributeValue + "\n")
        }
        return str.toString()
    }


}
