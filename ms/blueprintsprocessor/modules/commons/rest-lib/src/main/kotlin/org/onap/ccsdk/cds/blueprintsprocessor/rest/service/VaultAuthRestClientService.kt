/*
 * Copyright © 2019 IBM, Bell Canada
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

package org.onap.ccsdk.cds.blueprintsprocessor.rest.service

import org.apache.http.message.BasicHeader
import org.onap.ccsdk.cds.blueprintsprocessor.rest.VaultAuthRestClientProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

class VaultAuthRestClientService(private val restClientProperties:
                                 VaultAuthRestClientProperties) :
        BlueprintWebClientService {
    val AUTHORIZATION = "X-Vault-Token"

    override fun defaultHeaders(): Map<String, String> {

        return mapOf(
                HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE,
                AUTHORIZATION to restClientProperties.vaultToken!!)
    }

    override fun convertToBasicHeaders(headers: Map<String, String>):
            Array<BasicHeader> {

        val customHeaders: MutableMap<String, String> = headers.toMutableMap()
        if (!headers.containsKey(AUTHORIZATION)) {
            customHeaders[AUTHORIZATION] = restClientProperties.vaultToken!!
        }
        return super.convertToBasicHeaders(customHeaders)
    }

    override fun host(uri: String): String {
        return restClientProperties.url + uri
    }
}
