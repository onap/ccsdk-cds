package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance

import com.fasterxml.jackson.annotation.JsonProperty

class K8sTopic {

    @get:JsonProperty("name")
    var name: String? = null

    @get:JsonProperty("cluster")
    var cluster: String? = null

    @get:JsonProperty("partitions")
    var partitions: Number? = null

    @get:JsonProperty("replicas")
    var replicas: Number? = null

    override fun toString(): String {
        return "$name:$cluster:$partitions:$replicas"
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
