package org.chtijbug.drools.entity.history

import java.util.concurrent.atomic.AtomicLong

class EventCounter {

    private val nextEventId = AtomicLong(1L)

    constructor() {
    }
    companion object{
        fun newCounter(): EventCounter {
            return org.chtijbug.drools.entity.history.EventCounter()
        }
    }

    fun next(): Long {
        return nextEventId.getAndIncrement()
    }

    fun current(): Long {
        return nextEventId.get()
    }

}