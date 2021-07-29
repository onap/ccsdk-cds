package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance.healthcheck

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class K8sRbInstanceHealthCheckSimple {

    @get:JsonProperty("healthcheck-id")
    var id: String? = null

    @get:JsonProperty("status")
    var status: String? = null

    override fun toString(): String {
        return "$id:$status"
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

@JsonIgnoreProperties(ignoreUnknown = true)
class K8sRbInstanceHealthCheckList {

    @get:JsonProperty("instance-id")
    var instanceId: String? = null

    @get:JsonProperty("healthcheck-summary")
    var healthcheckSummary: List<K8sRbInstanceHealthCheckSimple>? = null

    @get:JsonProperty("hooks")
    var hooks: List<K8sRbInstanceHealthCheckHook>? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class K8sRbInstanceHealthCheck {

    @get:JsonProperty("healthcheck-id")
    var id: String? = null

    @get:JsonProperty("instance-id")
    var instanceId: String? = null

    @get:JsonProperty("info")
    var info: String? = null

    @get:JsonProperty("status")
    var status: String? = null

    @get:JsonProperty("test-suite")
    var testSuite: K8sHealthCheckTest? = null

    override fun toString(): String {
        return "$id:$status"
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

@JsonIgnoreProperties(ignoreUnknown = true)
class K8sHealthCheckTest {

    @get:JsonProperty("StartedAt")
    var startedAt: String? = null

    @get:JsonProperty("CompletedAt")
    var completedAt: String? = null

    @get:JsonProperty("Status")
    var status: String? = null

    @get:JsonProperty("TestManifests")
    var testManifests: List<String>? = null

    @get:JsonProperty("Results")
    var results: List<Any>? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class K8sRbInstanceHealthCheckHook {

    @get:JsonProperty("name")
    var name: String? = null

    @get:JsonProperty("kind")
    var kind: String? = null

    @get:JsonProperty("path")
    var path: String? = null

    @get:JsonProperty("manifest")
    var manifest: String? = null

    @get:JsonProperty("events")
    var events: List<Any>? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
