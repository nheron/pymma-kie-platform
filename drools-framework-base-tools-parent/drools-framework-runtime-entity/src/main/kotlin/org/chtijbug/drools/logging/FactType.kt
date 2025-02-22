package org.chtijbug.drools.logging

enum class FactType {
    WHEN, INSERTED, UPDATED_OLDVALUE, UPDATED_NEWVALUE, DELETED, INPUTDATA, OUTPUTDATA;

    fun getEnum(factType: String): FactType? {
        for (type in entries) {
            if (type.name == factType) return type
        }
        return null
    }


}