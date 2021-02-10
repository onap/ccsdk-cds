/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
 * Modifications Copyright © 2021 Orange.
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

class K8sRbInstance {

    @get:JsonProperty("id")
    var id: String? = null

    @get:JsonProperty("namespace")
    var namespace: String? = "default"

    @get:JsonProperty("request")
    var request: K8sRbInstanceRequest? = null

    @get:JsonProperty("release-name")
    var releaseName: String? = null

    @get:JsonProperty("resources")
    var resources: List<K8sRbInstanceResource>? = null

    override fun toString(): String {
        return "$id:$releaseName:$namespace"
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

class K8sRbInstanceResource {

    @get:JsonProperty("Name")
    var name: String? = null

    @get:JsonProperty("GVK")
    var gvk: K8sRbInstanceGvk? = null

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
