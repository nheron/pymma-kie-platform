package org.chtijbug.drools

import java.time.LocalDateTime

class ChtijbugObjectRequest {
    var transactionID: String? = null
    var transactionStartTimeStamp: LocalDateTime? = null
    var transactionEndTimeStamp: LocalDateTime? = null

    var processID: String? = null
    var containerID: String? = null

    var artifactID: String? = null
    var groupID: String? = null
    var version: String? = null

    var objectRequest: Any? = null

    var sessionLogging: SessionContext? = null

    var disableLogging = false


}