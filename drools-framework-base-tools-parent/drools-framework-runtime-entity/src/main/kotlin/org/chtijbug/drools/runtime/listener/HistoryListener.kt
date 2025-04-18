package org.chtijbug.drools.runtime.listener

import org.chtijbug.drools.entity.history.HistoryEvent
import org.chtijbug.drools.runtime.DroolsChtijbugException
import java.io.Serializable

interface HistoryListener : Serializable {
    @Throws(DroolsChtijbugException::class)
     fun fireEvent(newHistoryEvent: HistoryEvent?)

     fun withDetails(): Boolean

     fun setDetails(details: Boolean?)
}
