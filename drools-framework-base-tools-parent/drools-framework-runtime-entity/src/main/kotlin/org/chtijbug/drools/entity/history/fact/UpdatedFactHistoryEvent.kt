package org.chtijbug.drools.entity.history.fact

import org.chtijbug.drools.entity.DroolsFactObject
import java.util.*

class UpdatedFactHistoryEvent : FactHistoryEvent {

    private var objectOldValue: DroolsFactObject? = null
    private var objectNewValue: DroolsFactObject? = null

    /**
     *
     */
   constructor() {
    }

   constructor(
        eventID: Long?,
        objectOldValue: DroolsFactObject,
        objectNewValue: DroolsFactObject,
        ruleBaseId: Long?,
        sessionId: Long?
    ) :super(eventID, Date(), ruleBaseId, sessionId){

        this.objectOldValue = objectOldValue
        this.objectNewValue = objectNewValue
    }

    fun getObjectNewValue(): DroolsFactObject {
        return objectNewValue!!
    }

    fun setObjectNewValue(objectNewValue: DroolsFactObject) {
        this.objectNewValue = objectNewValue
    }

    fun getObjectOldValue(): DroolsFactObject {
        return objectOldValue!!
    }

    fun setObjectOldValue(objectOldValue: DroolsFactObject) {
        this.objectOldValue = objectOldValue
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append(super.toString() + "\n")
        sb.append("UpdatedFactHistoryEvent")
        sb.append("Update Object : " + objectNewValue!!.getFullClassName() + "\n")
        sb.append("{objectOldValue=").append("\n")
        sb.append("version Object : " + objectOldValue!!.getObjectVersion() + "\n")
        sb.append("attributes :\n")
        for (foa in objectOldValue!!.getListfactObjectAttributes()) {
            sb.append("- " + foa.attributeType + " " + foa.attributeName + "=" + foa.attributeValue + "\n")
        }
        sb.append(", objectNewValue=").append(objectNewValue)
        sb.append("version Object : " + objectNewValue!!.getObjectVersion() + "\n")
        sb.append("attributes :\n")
        for (foa in objectNewValue!!.getListfactObjectAttributes()) {
            sb.append("- " + foa.attributeType + " " + foa.attributeName + "=" + foa.attributeValue + "\n")
        }
        sb.append('}')
        return sb.toString()
    }




}
