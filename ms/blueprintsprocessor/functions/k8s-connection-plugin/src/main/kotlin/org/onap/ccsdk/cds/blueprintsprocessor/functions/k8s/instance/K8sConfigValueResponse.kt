package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class K8sConfigValueResponse {
    @get:JsonProperty("rb-name")
    var rbName: String? = null

    @get:JsonProperty("rb-version")
    var rbVersion: String? = null

    @get:JsonProperty("instance-id")
    var instanceId: String? = null

    @get:JsonProperty("profile-name")
    var profileName: String? = null

    @get:JsonProperty("description")
    var description: String? = null

    @get:JsonProperty("template-name")
    var templateName: String? = null

    @get:JsonProperty("config-name")
    var configName: String? = null

    @get:JsonProperty("config-version")
    @get:JsonAlias("config-verion")
    var configVersion: Integer? = null

    @get:JsonProperty("values")
    var values: Map<String, Object>? = null

    override fun toString(): String {
        return "$templateName:$configName"
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
