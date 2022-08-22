/*
 * Copyright © 2017-2019 AT&T, Bell Canada, Nordix Foundation
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
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.nio.charset.Charset
import java.util.Base64

open class BasicAuthRestClientService(
    private val restClientProperties:
        BasicAuthRestClientProperties
) :
    BaseBlueprintWebClientService<BasicAuthRestClientProperties>() {

    override fun getRestClientProperties(): BasicAuthRestClientProperties {
        return restClientProperties
    }

    override fun defaultHeaders(): Map<String, String> {

        val encodedCredentials = setBasicAuth(
            getRestClientProperties().username,
            getRestClientProperties().password
        )
        return mapOf(
            HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.AUTHORIZATION to "Basic $encodedCredentials"
        )
    }

    override fun convertToBasicHeaders(headers: Map<String, String>):
        Array<BasicHeader> {
            val customHeaders: MutableMap<String, String> = headers.toMutableMap()
            // inject additionalHeaders
            customHeaders.putAll(verifyAdditionalHeaders())

            if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                val encodedCredentials = setBasicAuth(
                    getRestClientProperties().username,
                    getRestClientProperties().password
                )
                customHeaders[HttpHeaders.AUTHORIZATION] =
                    "Basic $encodedCredentials"
            }
            return super.convertToBasicHeaders(customHeaders)
        }

    private fun setBasicAuth(username: String, password: String): String {
        val credentialsString = "$username:$password"
        return Base64.getEncoder().encodeToString(
            credentialsString.toByteArray(Charset.defaultCharset())
        )
    }
}
