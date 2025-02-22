package org.chtijbug.drools.entity

import java.io.Serializable

class DroolsFactObjectAttribute : Serializable{
    val serialVersionUID: Long = 2251337648100424168L
    var attributeName: String? = null
    var attributeValue: String? = null
    var attributeType: String? = null

    /**
     *
     */
    constructor() {
    }

    constructor(attributeName: String?, attributeValue: String?, attributeType: String?) {
        this.attributeName = attributeName
        this.attributeValue = attributeValue
        this.attributeType = attributeType
    }
}