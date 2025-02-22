package org.chtijbug.drools.logging

enum class PlatformRuntimeInstanceStatus {
    INITMODE, STARTED, NOT_JOINGNABLE, STOPPED, CRASHED;

    fun getEnum(assetStatus: String): PlatformRuntimeInstanceStatus? {
        for (status in entries) {
            if (status.name == assetStatus) return status
        }
        return null
    }
}