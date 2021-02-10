/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
 * Modifications Copyright © 2020 Orange.
 * Modifications Copyright © 2020 Deutsche Telekom AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance

import com.fasterxml.jackson.annotation.JsonProperty

class K8sRbInstanceStatus {

    @get:JsonProperty("request")
    var request: K8sRbInstanceRequest? = null

    @get:JsonProperty("resourceCount")
    var resourceCount: Int = 0

    @get:JsonProperty("ready")
    var ready: Boolean = false

    @get:JsonProperty("resourcesStatus")
    var resourcesStatus: List<K8sRbInstanceResourceStatus>? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class K8sRbInstanceResourceStatus {

    @get:JsonProperty("name")
    var name: String? = null

    @get:JsonProperty("GVK")
    var gvk: K8sRbInstanceGvk? = null

    @get:JsonProperty("status")
    var status: Map<String, Object>? = null

    override fun toString(): String {
        return "$name"
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
