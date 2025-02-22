package org.chtijbug.drools.jms

import org.chtijbug.drools.ReverseProxyUpdate
import org.springframework.jms.core.MessageCreator
import javax.jms.JMSException
import javax.jms.Message
import javax.jms.Session

class ReverseProxyMessageCreator : MessageCreator{

    private var reverseProxyUpdate: ReverseProxyUpdate? = null

    fun ReverseProxyMessageCreator(reverseProxyUpdate: ReverseProxyUpdate?) {
        this.reverseProxyUpdate = reverseProxyUpdate
    }

    @Throws(JMSException::class)
    override fun createMessage(session: Session): Message {
        val objectMessage = session.createObjectMessage()

        objectMessage.setObject(reverseProxyUpdate)
        return objectMessage
    }

}