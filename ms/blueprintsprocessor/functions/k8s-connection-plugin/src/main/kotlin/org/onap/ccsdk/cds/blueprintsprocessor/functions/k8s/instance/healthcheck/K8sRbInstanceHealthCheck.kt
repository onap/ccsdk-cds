package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance.healthcheck

import com.fasterxml.jackson.annotation.JsonProperty

class K8sRbInstanceHealthCheck {

    @get:JsonProperty("Id")
    var id: String? = null

    @get:JsonProperty("StartedAt")
    var startedAt: String? = null

    @get:JsonProperty("CompletedAt")
    var completedAt: String? = null

    @get:JsonProperty("Status")
    var status: String? = null

    @get:JsonProperty("Tests")
    var tests: List<K8sHealthCheckTest>? = null

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

class K8sHealthCheckTest {

    @get:JsonProperty("Name")
    var name: String? = null

    @get:JsonProperty("StartedAt")
    var startedAt: String? = null

    @get:JsonProperty("CompletedAt")
    var completedAt: String? = null

    @get:JsonProperty("Status")
    var status: String? = null

    @get:JsonProperty("Info")
    var info: String? = null

    override fun toString(): String {
        return "$name:$status"
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
