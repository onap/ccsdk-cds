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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s

import org.apache.http.message.BasicHeader
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.nio.charset.Charset
import java.util.Base64

abstract class K8sAbstractRestClientService(val username: String, val password: String) : BlueprintWebClientService {

    private val restClientProperties: BasicAuthRestClientProperties = getBasicAuthRestClientProperties()

    private fun getBasicAuthRestClientProperties(): BasicAuthRestClientProperties {
        val basicAuthRestClientProperties = BasicAuthRestClientProperties()
        basicAuthRestClientProperties.username = username
        basicAuthRestClientProperties.password = password
        basicAuthRestClientProperties.url = apiUrl()
        basicAuthRestClientProperties.additionalHeaders = getHeaders()
        return basicAuthRestClientProperties
    }

    private fun getHeaders(): HashMap<String, String> {
        val mapOfHeaders = hashMapOf<String, String>()
        mapOfHeaders["Accept"] = "application/json"
        mapOfHeaders["Content-Type"] = "application/json"
        mapOfHeaders["cache-control"] = " no-cache"
        mapOfHeaders["Accept"] = "application/json"
        return mapOfHeaders
    }

    private fun setBasicAuth(username: String, password: String): String {
        val credentialsString = "$username:$password"
        return Base64.getEncoder().encodeToString(credentialsString.toByteArray(Charset.defaultCharset()))
    }

    override fun defaultHeaders(): Map<String, String> {
        val encodedCredentials = setBasicAuth(
                restClientProperties.username,
                restClientProperties.password
        )
        return mapOf(
                CONTENT_TYPE to APPLICATION_JSON_VALUE,
                ACCEPT to APPLICATION_JSON_VALUE,
                AUTHORIZATION to "Basic $encodedCredentials"
        )
    }

    override fun host(uri: String): String {
        return restClientProperties.url + uri
    }

    override fun convertToBasicHeaders(headers: Map<String, String>): Array<BasicHeader> {
        val customHeaders: MutableMap<String, String> = headers.toMutableMap()
        // inject additionalHeaders
        customHeaders.putAll(verifyAdditionalHeaders(restClientProperties))

        if (!headers.containsKey(AUTHORIZATION)) {
            val encodedCredentials = setBasicAuth(
                    restClientProperties.username,
                    restClientProperties.password
            )
            customHeaders[AUTHORIZATION] = "Basic $encodedCredentials"
        }
        return super.convertToBasicHeaders(customHeaders)
    }

    abstract fun apiUrl(): String

}
