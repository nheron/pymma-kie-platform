package org.chtijbug.drools.common.reflection

import java.lang.reflect.Method

class ReflectionUtils {
    companion object {
        fun IsGetter(method: Method): Boolean {
            if (!method.getName().startsWith("get")) return false
            if (method.getParameterTypes().size != 0) return false
            if (Void.TYPE == method.getReturnType()) return false
            return true
        }
    }
}
