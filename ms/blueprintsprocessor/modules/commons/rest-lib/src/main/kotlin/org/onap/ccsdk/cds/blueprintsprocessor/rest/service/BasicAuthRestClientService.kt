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

import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustAllStrategy
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.ssl.SSLContextBuilder
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.utils.WebClientUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.net.URI
import java.nio.charset.Charset
import java.util.Base64

class BasicAuthRestClientService(
    private val restClientProperties:
    BasicAuthRestClientProperties
) :
    BlueprintWebClientService {

    override fun defaultHeaders(): Map<String, String> {

        val encodedCredentials = setBasicAuth(
            restClientProperties.username,
            restClientProperties.password
        )
        return mapOf(
            HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.AUTHORIZATION to "Basic $encodedCredentials"
        )
    }

    override fun host(uri: String): String {
        val uri: URI = URI.create(restClientProperties.url + uri)
        return uri.resolve(uri).toString()
    }

    override fun httpClient(): CloseableHttpClient {
        val sslContext = SSLContextBuilder.create()

        sslContext.loadTrustMaterial(TrustAllStrategy.INSTANCE)
        val csf = SSLConnectionSocketFactory(sslContext.build(), NoopHostnameVerifier())
        return HttpClients.custom()
            .addInterceptorFirst(WebClientUtils.logRequest())
            .addInterceptorLast(WebClientUtils.logResponse())
            .setSSLSocketFactory(csf).build()
    }

    override fun convertToBasicHeaders(headers: Map<String, String>):
            Array<BasicHeader> {
        val customHeaders: MutableMap<String, String> = headers.toMutableMap()
        // inject additionalHeaders
        customHeaders.putAll(verifyAdditionalHeaders(restClientProperties))

        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            val encodedCredentials = setBasicAuth(
                restClientProperties.username,
                restClientProperties.password
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
