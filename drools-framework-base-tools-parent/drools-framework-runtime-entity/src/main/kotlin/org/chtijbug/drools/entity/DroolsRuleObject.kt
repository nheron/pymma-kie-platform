package org.chtijbug.drools.entity

class DroolsRuleObject {

    val serialVersionUID: Long = -716077698281963299L

    var ruleName: String? = null
    var rulePackageName: String? = null
    var ruleFlowGroup: String? = null


    companion object{
        fun createDroolRuleObject(ruleName: String?, rulePackageName: String?): DroolsRuleObject {
            return org.chtijbug.drools.entity.DroolsRuleObject(ruleName, rulePackageName)
        }
    }

    /**
     *
     */
    constructor() {
    }

    /**
     * @param ruleName        - the rule name added to the knowledge base
     * @param rulePackageName - the package of the rule
     */
    protected constructor(ruleName: String?, rulePackageName: String?) {
        this.ruleName = ruleName
        this.rulePackageName = rulePackageName
    }





    /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#toString()
         */
    override fun toString(): String {
        return "Rule object with name :" + ruleName
    }
}