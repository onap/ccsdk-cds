package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance

import com.fasterxml.jackson.annotation.JsonProperty

class K8sConfigValueResponse {

    @get:JsonProperty("rb-name")
    var rbName: String? = null

    @get:JsonProperty("rb-version")
    var rbVersion: String? = null

    @get:JsonProperty("profile-name")
    var profileName: String? = null

    @get:JsonProperty("template-name")
    var templateName: String? = null

    @get:JsonProperty("config-name")
    var configName: String? = null

    @get:JsonProperty("config-name")
    var configVersion: String? = null

    override fun toString(): String {
        return "$rbName:$rbVersion:$profileName:$templateName:$configName:$configVersion"
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
