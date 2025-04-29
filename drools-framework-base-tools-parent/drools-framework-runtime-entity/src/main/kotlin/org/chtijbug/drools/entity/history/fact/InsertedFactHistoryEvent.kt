package org.chtijbug.drools.entity.history.fact

import org.chtijbug.drools.entity.DroolsFactObject
import java.util.*

class InsertedFactHistoryEvent : FactHistoryEvent {

    private var insertedObject: DroolsFactObject? = null

    constructor() {
    }

    constructor(
        eventID: Long?,
        insertedObject: DroolsFactObject,
        ruleBaseId: Long?,
        sessionId: Long?
    ) :super(eventID, Date(), ruleBaseId, sessionId){

        this.insertedObject = insertedObject
    }

    fun getInsertedObject(): DroolsFactObject {
        return insertedObject!!
    }

    fun setInsertedObject(insertedObject: DroolsFactObject) {
        this.insertedObject = insertedObject
    }

    /*
      * (non-Javadoc)
      *
      * @see org.chtijbug.drools.entity.history.HistoryEvent#toString()
      */
    override fun toString(): String {
        val str = StringBuilder()
        str.append(super.toString() + "\n")

        str.append("inserted Object : " + insertedObject!!.getFullClassName() + "\n")
        str.append("version Object : " + insertedObject!!.getObjectVersion() + "\n")
        str.append("attributes :\n")
        for (foa in insertedObject!!.getListfactObjectAttributes()) {
            str.append("- " + foa.attributeType + " " + foa.attributeName + "=" + foa.attributeValue + "\n")
        }

        return str.toString()
    }
}
