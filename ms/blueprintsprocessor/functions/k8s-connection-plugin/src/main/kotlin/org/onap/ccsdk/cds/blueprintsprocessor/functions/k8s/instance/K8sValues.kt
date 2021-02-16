package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance

import com.fasterxml.jackson.annotation.JsonProperty

class K8sValues {

    @get:JsonProperty("namespace")
    var namespace: String? = null

    @get:JsonProperty("topic")
    var topic: K8sTopic? = null

    override fun toString(): String {
        return "$namespace:$topic"
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
