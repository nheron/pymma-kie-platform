package org.chtijbug.drools.logging

import java.util.*

class RuleExecution {
    var ruleName: String? = null
    var packageName: String? = null
    var startDate: Date? = null
    var endDate: Date? = null

    var startEventID: Long? = null

    var stopEventID: Long? = null


    var whenFacts: List<Fact> = ArrayList()


    var thenFacts: List<Fact> = ArrayList()
}