/*
 * Copyright © 2017-2019 AT&T, Bell Canada
 * Modifications Copyright © 2019 Huawei.
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
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.ssl.SSLContextBuilder
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestClientProperties
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

open class SSLRestClientService(private val restClientProperties: SSLRestClientProperties) :
    BaseBlueprintWebClientService<SSLRestClientProperties>() {

    var auth: BlueprintWebClientService? = null

    init {
        auth = getAuthService()
    }

    override fun getRestClientProperties(): SSLRestClientProperties {
        return restClientProperties
    }

    private fun getAuthService(): BaseBlueprintWebClientService<RestClientProperties>? {
        // type,url and additional headers don't get carried over to TokenAuthRestClientProperties from SSLTokenAuthRestClientProperties
        // set them in auth obj to be consistent. TODO: refactor
        return when (restClientProperties) {
            is SSLBasicAuthRestClientProperties -> {
                val basicAuthProps = BasicAuthRestClientProperties()
                basicAuthProps.username = restClientProperties.username
                basicAuthProps.password = restClientProperties.password
                basicAuthProps.additionalHeaders = restClientProperties.additionalHeaders
                basicAuthProps.url = restClientProperties.url
                basicAuthProps.type = restClientProperties.type
                BasicAuthRestClientService(basicAuthProps)
            }
            is SSLTokenAuthRestClientProperties -> {
                val token = restClientProperties.tokenAuth!!
                token.additionalHeaders = restClientProperties.additionalHeaders
                token.url = restClientProperties.url
                token.type = restClientProperties.type
                TokenAuthRestClientService(token)
            }
            else -> {
                // Returns null for No auth
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
            HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE
        )
    }

    override fun httpClient(): CloseableHttpClient {

        val keystoreInstance = restClientProperties.keyStoreInstance
        val sslKey = restClientProperties.sslKey
        val sslKeyPwd = restClientProperties.sslKeyPassword
        val sslTrust = restClientProperties.sslTrust
        val sslTrustPwd = restClientProperties.sslTrustPassword
        val sslTrustIgnoreHostname = restClientProperties.sslTrustIgnoreHostname

        val acceptingTrustStrategy = { _: Array<X509Certificate>, _: String ->
            true
        }
        val sslContext = SSLContextBuilder.create()

        if (sslKey != null && sslKeyPwd != null) {
            FileInputStream(sslKey).use { keyInput ->
                val keyStore = KeyStore.getInstance(keystoreInstance)
                keyStore.load(keyInput, sslKeyPwd.toCharArray())
                sslContext.loadKeyMaterial(keyStore, sslKeyPwd.toCharArray())
            }
        }

        sslContext.loadTrustMaterial(File(sslTrust), sslTrustPwd.toCharArray(), acceptingTrustStrategy)
        var csf: SSLConnectionSocketFactory
        if (sslTrustIgnoreHostname) {
            csf = SSLConnectionSocketFactory(sslContext.build(), NoopHostnameVerifier())
        } else {
            csf = SSLConnectionSocketFactory(sslContext.build())
        }
        return HttpClients.custom()
            .addInterceptorFirst(WebClientUtils.logRequest())
            .addInterceptorLast(WebClientUtils.logResponse())
            .setDefaultRequestConfig(getRequestConfig())
            .setSSLSocketFactory(csf).build()
    }

    override fun convertToBasicHeaders(headers: Map<String, String>): Array<BasicHeader> {
        val mergedDefaultAndSuppliedHeaders = defaultHeaders().plus(headers)
        // During the initialization, getAuthService() sets the auth variable.
        // If it's not null, then we have an authentication mechanism.
        // If null - indicates no-auth used
        if (auth != null) {
            return auth!!.convertToBasicHeaders(mergedDefaultAndSuppliedHeaders)
        }
        // inject additionalHeaders
        return super.convertToBasicHeaders(
            mergedDefaultAndSuppliedHeaders
                .plus(verifyAdditionalHeaders(restClientProperties))
        )
    }
}
