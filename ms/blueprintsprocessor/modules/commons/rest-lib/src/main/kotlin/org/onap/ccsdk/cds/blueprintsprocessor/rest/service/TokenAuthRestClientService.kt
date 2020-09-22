/*
 * Copyright © 2019 Bell Canada, Nordix Foundation
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
import org.onap.ccsdk.cds.blueprintsprocessor.rest.TokenAuthRestClientProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

class TokenAuthRestClientService(
    private val restClientProperties:
        TokenAuthRestClientProperties
) :
    BlueprintWebClientService {

    override fun defaultHeaders(): Map<String, String> {
        return mapOf(
            HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.AUTHORIZATION to restClientProperties.token!!
        )
    }

    override fun convertToBasicHeaders(headers: Map<String, String>):
        Array<BasicHeader> {
            val customHeaders: MutableMap<String, String> = headers.toMutableMap()
            // inject additionalHeaders
            customHeaders.putAll(verifyAdditionalHeaders(restClientProperties))
            if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                customHeaders[HttpHeaders.AUTHORIZATION] = restClientProperties.token!!
            }
            return super.convertToBasicHeaders(customHeaders)
        }

    override fun host(uri: String): String {
        return restClientProperties.url + uri
    }
}
