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

package org.onap.ccsdk.apps.blueprintsprocessor.rest.service

import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.ssl.SSLContextBuilder
import org.onap.ccsdk.apps.blueprintsprocessor.rest.SSLBasicAuthRestClientProperties
import org.onap.ccsdk.apps.blueprintsprocessor.rest.utils.WebClientUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.cert.X509Certificate

class SSLBasicAuthRestClientService(private val restClientProperties: SSLBasicAuthRestClientProperties) :
    BlueprintWebClientService {

    override fun headers(): Array<BasicHeader> {
        val params = arrayListOf<BasicHeader>()
        params.add(BasicHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        params.add(BasicHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
        return params.toTypedArray()
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

        val acceptingTrustStrategy = { chain: Array<X509Certificate>, authType: String -> true }

        FileInputStream(sslKey).use { keyInput ->
            val keyStore = KeyStore.getInstance(keystoreInstance)
            keyStore.load(keyInput, sslKeyPwd.toCharArray())

            val sslContext =
                SSLContextBuilder.create()
                    .loadKeyMaterial(keyStore, sslKeyPwd.toCharArray())
                    .loadTrustMaterial(File(sslTrust), sslTrustPwd.toCharArray(), acceptingTrustStrategy).build()

            val csf = SSLConnectionSocketFactory(sslContext!!)

            return HttpClients.custom()
                .addInterceptorFirst(WebClientUtils.logRequest())
                .addInterceptorLast(WebClientUtils.logResponse())
                .setSSLSocketFactory(csf).build()
        }
    }
}