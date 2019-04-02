/*
 * Copyright © 2017-2019 AT&T, Bell Canada
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

import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.ssl.SSLContextBuilder
import org.onap.ccsdk.cds.blueprintsprocessor.rest.SSLBasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.SSLRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.SSLTokenAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.utils.WebClientUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.cert.X509Certificate

class SSLRestClientService(private val restClientProperties:
                           SSLRestClientProperties) :
        BlueprintWebClientService {

    var auth: BlueprintWebClientService? = null

    init {
         auth = getAuthService()
    }

    private fun getAuthService() : BlueprintWebClientService? {

        return when(restClientProperties) {
            is SSLBasicAuthRestClientProperties -> {
                val basic =  restClientProperties.basicAuth!!
                BasicAuthRestClientService(basic)
            }
            is SSLTokenAuthRestClientProperties -> {
                val token =  restClientProperties.tokenAuth!!
                TokenAuthRestClientService(token)
            }
            else -> {
                null
            }
        }
    }


    override fun defaultHeaders(): Map<String, String> {

        if (auth != null) {
            return auth!!.defaultHeaders()
        }
        return mapOf(
                HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE)
    }

    override fun host(uri: String): String {

        return restClientProperties.url + uri
    }

    override fun httpClient(): CloseableHttpClient {

        val keystoreInstance = restClientProperties.keyStoreInstance
        val sslKey = restClientProperties.sslKey
        val sslKeyPwd = restClientProperties.sslKeyPassword
        val sslTrust = restClientProperties.sslTrust
        val sslTrustPwd = restClientProperties.sslTrustPassword

        val acceptingTrustStrategy = { chain: Array<X509Certificate>,
                                       authType: String -> true }
        val sslContext = SSLContextBuilder.create()

        if (sslKey != null && sslKeyPwd != null) {
            FileInputStream(sslKey).use { keyInput ->
                val keyStore = KeyStore.getInstance(keystoreInstance)
                keyStore.load(keyInput, sslKeyPwd.toCharArray())
                sslContext.loadKeyMaterial(keyStore, sslKeyPwd.toCharArray())
            }
        }

        sslContext.loadTrustMaterial(File(sslTrust), sslTrustPwd.toCharArray(),
                acceptingTrustStrategy)
        val csf = SSLConnectionSocketFactory(sslContext.build())
        return HttpClients.custom()
                .addInterceptorFirst(WebClientUtils.logRequest())
                .addInterceptorLast(WebClientUtils.logResponse())
                .setSSLSocketFactory(csf).build()

    }

    // Non Blocking Rest Implementation
    override suspend fun httpClientNB(): CloseableHttpClient {
        return httpClient()
    }

    override fun convertToBasicHeaders(headers: Map<String, String>): Array<BasicHeader> {
        var head1: Map<String, String> = defaultHeaders()
        var head2: MutableMap<String, String> = head1.toMutableMap()
        head2.putAll(headers)
        if (auth != null) {
            return auth!!.convertToBasicHeaders(head2)
        }
        return super.convertToBasicHeaders(head2)
    }

}