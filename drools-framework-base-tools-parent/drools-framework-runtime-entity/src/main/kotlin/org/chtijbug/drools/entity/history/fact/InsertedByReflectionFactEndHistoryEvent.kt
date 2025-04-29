package org.chtijbug.drools.entity.history.fact

import java.util.*

class InsertedByReflectionFactEndHistoryEvent : FactHistoryEvent {
   constructor() {
    }

    constructor(eventID: Long?, ruleBaseId: Long?, sessionId: Long?) :super(eventID, Date(), ruleBaseId, sessionId){

    }


    /*
      * (non-Javadoc)
      *
      * @see org.chtijbug.drools.entity.history.HistoryEvent#toString()
      */
    override fun toString(): String {
        val str = StringBuilder()
        str.append(super.toString() + "\n")
        str.append("End InsertBuReflectionEndHistoryEvent\n")


        return str.toString()
    }
}
