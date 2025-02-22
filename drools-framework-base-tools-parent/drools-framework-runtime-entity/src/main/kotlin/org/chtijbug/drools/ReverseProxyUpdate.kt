package org.chtijbug.drools

import java.io.Serializable

class ReverseProxyUpdate : Serializable{

    var path: String? = null

    var tokenUUID: String? = null

    var containerID: String? = null

    var serverNames: List<String> = ArrayList()

}