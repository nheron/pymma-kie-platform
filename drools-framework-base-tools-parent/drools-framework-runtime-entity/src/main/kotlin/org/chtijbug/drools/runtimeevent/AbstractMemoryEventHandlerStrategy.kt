package org.chtijbug.drools.runtimeevent

import com.rits.cloning.Cloner
import org.chtijbug.drools.SessionContext
import org.chtijbug.drools.entity.history.HistoryEvent

interface AbstractMemoryEventHandlerStrategy {
    abstract fun handleMessageInternally(historyEvent: HistoryEvent?, sessionContext: SessionContext?, cloner: Cloner?)

    abstract fun isEventSupported(historyEvent: HistoryEvent?): Boolean

}