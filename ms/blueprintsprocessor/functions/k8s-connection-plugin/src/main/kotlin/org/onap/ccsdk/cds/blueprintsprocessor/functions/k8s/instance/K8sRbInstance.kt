/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
 * Modifications Copyright © 2022 Orange.
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
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

@JsonIgnoreProperties(ignoreUnknown = true)
class K8sRbInstanceFull {

    @get:JsonProperty("id")
    var id: String? = null

    @get:JsonProperty("namespace")
    var namespace: String? = "default"

    @get:JsonProperty("status")
    var status: String? = null

    @get:JsonProperty("hook-progress")
    var hookProgress: String? = null

    @get:JsonProperty("request")
    var request: K8sRbInstanceRequest? = null

    @get:JsonProperty("release-name")
    var releaseName: String? = null

    @get:JsonProperty("resources")
    var resources: List<K8sRbInstanceResource>? = null

    @get:JsonProperty("hooks")
    var hooks: List<K8sRbInstanceHookDefinition>? = null

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

@JsonIgnoreProperties(ignoreUnknown = true)
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

@JsonIgnoreProperties(ignoreUnknown = true)
class K8sRbInstanceHookDefinition {

    @get:JsonProperty("Hook")
    var hook: K8sRbInstanceHook? = null

    @get:JsonProperty("KRT")
    var krt: K8sRbInstanceKrt? = null

    override fun toString(): String {
        return "$hook"
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
class K8sRbInstanceHook {

    @get:JsonProperty("name")
    var name: String? = null

    @get:JsonProperty("kind")
    var kind: String? = null

    @get:JsonProperty("path")
    var path: String? = null

    @get:JsonProperty("manifest")
    var manifest: String? = null

    @get:JsonProperty("weight")
    var weight: Int? = 0

    @get:JsonProperty("last_run")
    var lastRun: K8sRbInstanceHookExecution? = null

    @get:JsonProperty("events")
    var events: List<String>? = null

    @get:JsonProperty("delete_policies")
    var deletePolicies: List<String>? = null

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

@JsonIgnoreProperties(ignoreUnknown = true)
class K8sRbInstanceHookExecution {

    @get:JsonProperty("started_at")
    var startedAt: String? = null

    @get:JsonProperty("completed_at")
    var completedAt: String? = null

    @get:JsonProperty("phase")
    var phase: String? = null

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
class K8sRbInstanceKrt {

    @get:JsonProperty("FilePath")
    var filePath: String? = null

    @get:JsonProperty("GVK")
    var gvk: K8sRbInstanceGvk? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
