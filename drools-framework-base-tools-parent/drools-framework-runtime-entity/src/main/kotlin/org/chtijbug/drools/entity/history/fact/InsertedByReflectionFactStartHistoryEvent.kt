package org.chtijbug.drools.entity.history.fact

import org.chtijbug.drools.entity.DroolsFactObject
import java.util.*

class InsertedByReflectionFactStartHistoryEvent : FactHistoryEvent {
    private var topObject: DroolsFactObject? = null

    constructor() {
    }

    constructor(
        eventID: Long?,
        topObject: DroolsFactObject?,
        ruleBaseId: Long?,
        sessionId: Long?
    ) : super(eventID, Date(), ruleBaseId, sessionId){

        this.topObject = topObject
    }

    fun getTopObject(): DroolsFactObject? {
        return topObject
    }

    fun setTopObject(topObject: DroolsFactObject?) {
        this.topObject = topObject
    }

    /*
      * (non-Javadoc)
      *
      * @see org.chtijbug.drools.entity.history.HistoryEvent#toString()
      */
    override fun toString(): String {
        val str = StringBuilder()
        str.append(super.toString() + "\n")

        if (topObject != null) {
            str.append("inserted Top Object : " + topObject!!.getFullClassName() + "\n")
            str.append("version Object : " + topObject!!.getObjectVersion() + "\n")
            str.append("attributes :\n")
            for (foa in topObject!!.getListfactObjectAttributes()) {
                str.append("- " + foa.attributeType + " " + foa.attributeName + "=" + foa.attributeValue + "\n")
            }
        }

        return str.toString()
    }


}
