/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

import io.netty.handler.ssl.SslContextBuilder
import org.onap.ccsdk.apps.blueprintsprocessor.rest.SSLBasicAuthRestClientProperties
import org.onap.ccsdk.apps.blueprintsprocessor.rest.utils.WebClientUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.io.File
import java.security.KeyStore
import java.security.cert.X509Certificate


class SSLBasicAuthRestClientService(private val restClientProperties: SSLBasicAuthRestClientProperties) : BlueprintWebClientService {

    override fun webClient(): WebClient {

        // Load the Keystore Information
        val ketInputStream = File(restClientProperties.sslKey).inputStream()
        val ks = KeyStore.getInstance(restClientProperties.keyStoreInstance)
        ks.load(ketInputStream, restClientProperties.sslKeyPasswd.toCharArray())

        // Manage Trust Store
        val trustCertCollection = ks.aliases().toList().map { alias ->
            ks.getCertificate(alias) as X509Certificate
        }.toTypedArray()
        val sslContext = SslContextBuilder
                .forClient()
                .trustManager(*trustCertCollection)
                .build()

        // Create Http Client
        val httpClient = HttpClient.create().secure { t -> t.sslContext(sslContext) }

        return WebClient.builder()
                .baseUrl(restClientProperties.url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(WebClientUtils.logRequest())
                .clientConnector(ReactorClientHttpConnector(httpClient)).build()
    }

    override fun <T> getResource(path: String, responseType: Class<T>): T {
        return getResource(path, null, responseType)
    }

    override fun <T> getResource(path: String, headers: Map<String, String>?, responseType: Class<T>): T {
        return webClient().get()
                .uri(path)
                .headers { httpHeaders ->
                    headers?.forEach {
                        httpHeaders.set(it.key, it.value)
                    }
                }
                .retrieve()
                .bodyToMono(responseType).block()!!
    }

    override fun <T> postResource(path: String, request: Any, responseType: Class<T>): T {
        return postResource(path, null, request, responseType)
    }

    override fun <T> postResource(path: String, headers: Map<String, String>?, request: Any, responseType: Class<T>): T {
        return webClient().post()
                .uri(path)
                .headers { httpHeaders ->
                    headers?.forEach {
                        httpHeaders.set(it.key, it.value)
                    }
                }
                .body(BodyInserters.fromObject(request))
                .retrieve().bodyToMono(responseType).block()!!
    }

    override fun <T> exchangeResource(methodType: String, path: String, request: Any, responseType: Class<T>): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}