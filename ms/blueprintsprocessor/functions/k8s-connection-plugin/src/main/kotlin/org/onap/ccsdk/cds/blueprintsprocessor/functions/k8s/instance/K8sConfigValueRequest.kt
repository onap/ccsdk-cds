package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance

import com.fasterxml.jackson.annotation.JsonProperty

class K8sConfigValueRequest {

    @get:JsonProperty("template-name")
    var templateName: String? = null

    @get:JsonProperty("config-name")
    var configName: String? = null

    @get:JsonProperty("description")
    var description: String? = null

    @get:JsonProperty("values")
    var values: Any? = null

    override fun toString(): String {
        return "$templateName:$configName:$description:$values"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
