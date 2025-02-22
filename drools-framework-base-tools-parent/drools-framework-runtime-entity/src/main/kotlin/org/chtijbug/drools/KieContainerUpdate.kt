package org.chtijbug.drools

class KieContainerUpdate {
    enum class STATUS {
        TODEPLOY,
        TODELETE
    }
    var mainClass: String? = null

    var groupID: String? = null

    var artifactID: String? = null

    var processID: String? = null

    var projectVersion: String? = null

    var containerID: String? = null

    var action: STATUS? = null


}