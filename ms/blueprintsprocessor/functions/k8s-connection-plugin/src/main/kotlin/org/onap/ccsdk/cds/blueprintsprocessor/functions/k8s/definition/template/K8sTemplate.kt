package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.definition.template

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class K8sTemplate {

    @get:JsonProperty("template-name")
    var templateName: String? = null

    @get:JsonProperty("description")
    var description: String? = null

    override fun toString(): String {
        return "$templateName:$description"
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
