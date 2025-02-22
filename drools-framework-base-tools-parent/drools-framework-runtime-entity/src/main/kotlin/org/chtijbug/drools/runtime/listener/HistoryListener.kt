package org.chtijbug.drools.runtime.listener

import org.chtijbug.drools.entity.history.HistoryEvent
import org.chtijbug.drools.runtime.DroolsChtijbugException
import java.io.Serializable

interface HistoryListener : Serializable {
    @Throws(DroolsChtijbugException::class)
    open fun fireEvent(newHistoryEvent: HistoryEvent?)

    open fun withDetails(): Boolean

    open fun setDetails(details: Boolean?)
}