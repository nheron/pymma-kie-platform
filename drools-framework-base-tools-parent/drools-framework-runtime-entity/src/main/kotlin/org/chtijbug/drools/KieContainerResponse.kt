package org.chtijbug.drools

class KieContainerResponse {
    enum class STATUS {
        ERROR,
        SUCCESS
    }

    var kieContainerUpdate: KieContainerUpdate? = null

    var messageError: String? = null

    var status: STATUS? = null

    var errorMessages: List<String> = ArrayList()

    override fun toString(): String {
        val sb = StringBuffer("KieContainerResponse{")
        sb.append("kieContainerUpdate=").append(kieContainerUpdate)
        sb.append(", messageError='").append(messageError).append('\'')
        sb.append(", status=").append(status)
        sb.append('}')
        return sb.toString()
    }
}