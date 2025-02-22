package org.chtijbug.drools.entity

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.StringWriter
import java.io.Writer

class DroolsFactObject {
    val logger: Logger = LoggerFactory.getLogger(DroolsFactObject::class.java)

    val serialVersionUID: Long = 8185674445343213645L

    @Transient
    private var realObject: Any? = null
    protected var version: Int = 0
    private var fullClassName: String? = null
    private var hashCode = 0
    private var listfactObjectAttributes: List<DroolsFactObjectAttribute> = ArrayList()
    private var realObject_JSON: String? = null
    val mapper: ObjectMapper = ObjectMapper()
    private var classLoader: ClassLoader? = null

    /**
     *
     */
    constructor() {
        realObject = null
    }

    @Throws(IOException::class)
    constructor(realObject: Any?, version: Int) {
        this.realObject = realObject
        this.version = version
        val strWriter: Writer = StringWriter()
        mapper.writeValue(strWriter, realObject)
        this.realObject_JSON = strWriter.toString()
        if (realObject != null) {
            this.classLoader = realObject.javaClass.classLoader
        }
    }

    @Throws(ClassNotFoundException::class, IOException::class)
    fun updateRealObjectFromJSON() {
        if (this.realObject_JSON != null) {
            var localClassLoader: ClassLoader? = null
            try {
                localClassLoader = Thread.currentThread()
                    .contextClassLoader
            } catch (e: ClassCastException) {
                logger.info("DroolsFactObject.updateRealObjectFromJSON", e)
            }
            try {
                var targetClass: Class<*>? = null
                if (this.classLoader != null) {
                    Thread.currentThread().contextClassLoader = classLoader
                    targetClass = classLoader!!.loadClass(this.fullClassName)
                }
                if (targetClass == null) {
                    targetClass = Class.forName(this.fullClassName)
                }
                var result: Any? = null

                result = mapper.readValue(this.realObject_JSON, targetClass)
                this.realObject = result
            } catch (e: ClassNotFoundException) {
                logger.error("getRealObjectFromJSON")
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if (localClassLoader != null) {
                    Thread.currentThread().contextClassLoader = localClassLoader
                }
            }
        }
    }

    fun getRealObjectFromJSON(): Any? {
        try {
            this.updateRealObjectFromJSON()
        } catch (e: ClassNotFoundException) {
            logger.error("getRealObjectFromJSON")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return realObject
    }

    ;


    fun getRealObject_JSON(): String? {
        return realObject_JSON
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append("DroolsFactObject")
        sb.append("{fullClassName='").append(fullClassName).append('\'')
        sb.append(", hashCode=").append(hashCode)
        sb.append(", version=").append(version)
        sb.append(", listfactObjectAttributes=").append(listfactObjectAttributes)
        sb.append("attributes :\n")
        for (foa in this.getListfactObjectAttributes()) {
            sb.append("- " + foa.attributeType + " " + foa.attributeName + "=" + foa.attributeValue + "\n")
        }
        sb.append(", realObject=").append(realObject)
        sb.append('}')
        return sb.toString()
    }

    fun getObjectVersion(): Int {
        return version
    }

    fun getNextObjectVersion(): Int {
        return version + 1
    }

    fun getListfactObjectAttributes(): List<DroolsFactObjectAttribute> {
        return listfactObjectAttributes
    }

    fun setListfactObjectAttributes(listfactObjectAttributes: List<DroolsFactObjectAttribute>) {
        this.listfactObjectAttributes = listfactObjectAttributes
    }

    fun getFullClassName(): String? {
        return fullClassName
    }

    fun setFullClassName(fullClassName: String?) {
        this.fullClassName = fullClassName
    }

    fun getHashCode(): Int {
        return hashCode
    }

    fun setHashCode(hashCode: Int) {
        this.hashCode = hashCode
    }

    fun getRealObject(): Any? {
        return realObject
    }




}