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

class K8sRbInstanceRequest {

    @get:JsonProperty("labels")
    var labels: Map<String, String>? = null

    @get:JsonProperty("cloud-region")
    var cloudRegion: String? = null

    @get:JsonProperty("override-values")
    var overrideValues: Map<String, String>? = null

    @get:JsonProperty("release-name")
    var releaseName: String? = null

    @get:JsonProperty("rb-name")
    var rbName: String? = null

    @get:JsonProperty("rb-version")
    var rbVersion: String? = null

    @get:JsonProperty("profile-name")
    var profileName: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
