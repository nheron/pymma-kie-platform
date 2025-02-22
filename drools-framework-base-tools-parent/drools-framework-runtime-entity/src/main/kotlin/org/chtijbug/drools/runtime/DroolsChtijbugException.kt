package org.chtijbug.drools.runtime

import java.io.Serializable

class DroolsChtijbugException : Exception, Serializable {

    companion object{
        val insertByReflection: String = "insertByReflection"
        val MaxNumberRuleExecutionReached: String = "MaxNumberRuleExecutionReached"
        val fireAllRules: String = "fireAllRules"
        val KbaseAcquire: String = "KbaseAcquire"
        val KbaseNotInitialised: String = "KbaseNotInitialised"
        val ErrorToLoadAgent: String = "ErrorToLoadAgent"
        val UnknowFileExtension: String = "UnknowFileExtension"
        val ErrorRegisteringMBeans: String = ""
        val RessourceAlreadyAdded: String = "RessourceAlreadyAdded"
        val RessourceDoesNotExist: String = "RessourceNotExisting"
    }

    var key: String? = null
    var Description: String? = null
    var originException: java.lang.Exception? = null

    constructor() {
    }

    constructor(key: String, description: String, originException: java.lang.Exception?) : super(
        key + description,
        originException
    ) {
        this.key = key
        Description = description
        this.originException = originException
    }

}