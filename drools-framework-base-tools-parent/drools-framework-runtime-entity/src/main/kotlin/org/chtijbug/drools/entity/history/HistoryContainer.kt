package org.chtijbug.drools.entity.history

import org.chtijbug.drools.runtime.DroolsChtijbugException
import org.chtijbug.drools.runtime.listener.HistoryListener
import java.io.Serializable
import java.util.*

class HistoryContainer: Serializable {
     val serialVersionUID: Long = 5645452451089006572L
     var listHistoryEvent: MutableList<HistoryEvent> = LinkedList()
     var sessionID: Long? = null
     var historylistener: HistoryListener? = null

    constructor() {
    }

    /**
     *
     */
    constructor(sessionID: Long?, historylistener: HistoryListener?) {
        this.sessionID = sessionID
        this.historylistener = historylistener
    }


    fun addHistoryElement(ruleBaseID: Long?, sessionID: Long?, newHistoryElement: HistoryEvent) {
        var error: DroolsChtijbugException? = null
        newHistoryElement.ruleBaseID = ruleBaseID
        newHistoryElement.sessionId = sessionID
        try {
            if (historylistener != null) {
                historylistener!!.fireEvent(newHistoryElement)
            }
        } catch (e: DroolsChtijbugException) {
            error = e
        } finally {
            if (error != null) {
                newHistoryElement.droolsChtijbugException = error
            }
            listHistoryEvent.add(newHistoryElement)
        }
    }
}