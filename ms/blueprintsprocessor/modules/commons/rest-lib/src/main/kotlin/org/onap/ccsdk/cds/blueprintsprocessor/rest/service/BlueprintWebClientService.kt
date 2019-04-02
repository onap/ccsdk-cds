/*
 * Copyright © 2017-2019 AT&T, Bell Canada, Nordix Foundation
 * Modifications Copyright © 2018-2019 IBM.
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

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.*
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.onap.ccsdk.cds.blueprintsprocessor.rest.utils.WebClientUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.http.HttpMethod
import java.io.InputStream
import java.nio.charset.Charset

interface BlueprintWebClientService {

    fun defaultHeaders(): Map<String, String>

    fun host(uri: String): String

    fun httpClient(): CloseableHttpClient {
        return HttpClients.custom()
                .addInterceptorFirst(WebClientUtils.logRequest())
                .addInterceptorLast(WebClientUtils.logResponse())
                .build()
    }

    fun exchangeResource(methodType: String, path: String, request: String):
            String {
        return this.exchangeResource(methodType, path, request, defaultHeaders())
    }

    fun exchangeResource(methodType: String, path: String, request: String,
                         headers: Map<String, String>): String {
        val convertedHeaders: Array<BasicHeader> = convertToBasicHeaders(
                headers)
        return when (HttpMethod.resolve(methodType)) {
            HttpMethod.DELETE -> delete(path, convertedHeaders)
            HttpMethod.GET -> get(path, convertedHeaders)
            HttpMethod.POST -> post(path, request, convertedHeaders)
            HttpMethod.PUT -> put(path, request, convertedHeaders)
            HttpMethod.PATCH -> patch(path, request, convertedHeaders)
            else -> throw BluePrintProcessorException("Unsupported met" +
                    "hodType($methodType)")
        }
    }

    fun convertToBasicHeaders(headers: Map<String, String>): Array<BasicHeader> {
        return headers.map{ BasicHeader(it.key, it.value)}.toTypedArray()
    }

    fun delete(path: String, headers: Array<BasicHeader>): String {
        val httpDelete = HttpDelete(host(path))
        httpDelete.setHeaders(headers)
        httpClient().execute(httpDelete).entity.content.use {
            return IOUtils.toString(it, Charset.defaultCharset())
        }
    }

    fun get(path: String, headers: Array<BasicHeader>): String {
        val httpGet = HttpGet(host(path))
        httpGet.setHeaders(headers)
        httpClient().execute(httpGet).entity.content.use {
            return IOUtils.toString(it, Charset.defaultCharset())
        }
    }

    fun post(path: String, request: String, headers: Array<BasicHeader>):
            String {
        val httpPost = HttpPost(host(path))
        val entity = StringEntity(request)
        httpPost.entity = entity
        httpPost.setHeaders(headers)
        httpClient().execute(httpPost).entity.content.use {
            return IOUtils.toString(it, Charset.defaultCharset())
        }
    }

    fun put(path: String, request: String, headers: Array<BasicHeader>):
            String {
        val httpPut = HttpPut(host(path))
        val entity = StringEntity(request)
        httpPut.entity = entity
        httpPut.setHeaders(headers)
        httpClient().execute(httpPut).entity.content.use {
            return IOUtils.toString(it, Charset.defaultCharset())
        }
    }

    fun patch(path: String, request: String, headers: Array<BasicHeader>):
            String {
        val httpPatch = HttpPatch(host(path))
        val entity = StringEntity(request)
        httpPatch.entity = entity
        httpPatch.setHeaders(headers)
        httpClient().execute(httpPatch).entity.content.use {
            return IOUtils.toString(it, Charset.defaultCharset())
        }
    }


    suspend fun getNB(path: String): String {
        return getNB(path, null, String::class.java)
    }

    suspend fun getNB(path: String, additionalHeaders: Map<String, String>?):
            String {
        return getNB(path, additionalHeaders, String::class.java)
    }

    suspend fun <T> getNB(path: String, additionalHeaders: Map<String, String>?,
                          responseType: Class<T>): T =
            withContext(Dispatchers.IO) {
        val httpGet = HttpGet(host(path))
        httpGet.setHeaders(basicHeaders(additionalHeaders))
        httpClientNB().execute(httpGet).entity.content.use {
            getResponse(it, responseType)
        }
    }

    suspend fun postNB(path: String, request: Any): String {
        return postNB(path, request, null, String::class.java)
    }

    suspend fun postNB(path: String, request: Any,
                       additionalHeaders: Map<String, String>?): String {
        return postNB(path, request, additionalHeaders, String::class.java)
    }

    suspend fun <T> postNB(path: String, request: Any,
                           additionalHeaders: Map<String, String>?,
                           responseType: Class<T>): T =
            withContext(Dispatchers.IO) {
                val httpPost = HttpPost(host(path))
                httpPost.entity = StringEntity(strRequest(request))
                httpPost.setHeaders(basicHeaders(additionalHeaders))
                httpClientNB().execute(httpPost).entity.content.use {
                    getResponse(it, responseType)
                }
            }

    suspend fun putNB(path: String, request: Any): String {
        return putNB(path, request, null, String::class.java)
    }

    suspend fun putNB(path: String, request: Any,
                      additionalHeaders: Map<String, String>?): String {
        return putNB(path, request, additionalHeaders, String::class.java)
    }

    suspend fun <T> putNB(path: String, request: Any,
                          additionalHeaders: Map<String, String>?,
                          responseType: Class<T>): T =
            withContext(Dispatchers.IO) {
        val httpPut = HttpPut(host(path))
        httpPut.entity = StringEntity(strRequest(request))
        httpPut.setHeaders(basicHeaders(additionalHeaders))
        httpClientNB().execute(httpPut).entity.content.use {
            getResponse(it, responseType)
        }
    }

    suspend fun <T> deleteNB(path: String): String {
        return deleteNB(path, null, String::class.java)
    }

    suspend fun <T> deleteNB(path: String,
                             additionalHeaders: Map<String, String>?): String {
        return deleteNB(path, additionalHeaders, String::class.java)
    }

    suspend fun <T> deleteNB(path: String,
                             additionalHeaders: Map<String, String>?,
                             responseType: Class<T>): T =
            withContext(Dispatchers.IO) {
        val httpDelete = HttpDelete(host(path))
        httpDelete.setHeaders(basicHeaders(additionalHeaders))
        httpClient().execute(httpDelete).entity.content.use {
            getResponse(it, responseType)
        }
    }

    suspend fun <T> patchNB(path: String, request: Any,
                            additionalHeaders: Map<String, String>?,
                            responseType: Class<T>): T =
            withContext(Dispatchers.IO) {
        val httpPatch = HttpPatch(host(path))
        httpPatch.entity = StringEntity(strRequest(request))
        httpPatch.setHeaders(basicHeaders(additionalHeaders))
        httpClient().execute(httpPatch).entity.content.use {
            getResponse(it, responseType)
        }
    }

    suspend fun exchangeNB(methodType: String, path: String, request: Any):
            String {
        return exchangeNB(methodType, path, request, hashMapOf(),
                String::class.java)
    }

    suspend fun exchangeNB(methodType: String, path: String, request: Any,
                           additionalHeaders: Map<String, String>?): String {
        return exchangeNB(methodType, path, request, additionalHeaders,
                String::class.java)
    }

    suspend fun <T> exchangeNB(methodType: String, path: String, request: Any,
                               additionalHeaders: Map<String, String>?,
                               responseType: Class<T>): T {

        return when (HttpMethod.resolve(methodType)) {
            HttpMethod.GET -> getNB(path, additionalHeaders, responseType)
            HttpMethod.POST -> postNB(path, request, additionalHeaders,
                    responseType)
            HttpMethod.DELETE -> deleteNB(path, additionalHeaders, responseType)
            HttpMethod.PUT -> putNB(path, request, additionalHeaders,
                    responseType)
            HttpMethod.PATCH -> patchNB(path, request, additionalHeaders,
                    responseType)
            else -> throw BluePrintProcessorException("Unsupported method" +
                    "Type($methodType)")
        }
    }

    private fun strRequest(request: Any): String {
        return when (request) {
            is String -> request.toString()
            is JsonNode -> request.toString()
            else -> JacksonUtils.getJson(request)
        }
    }

    private fun <T> getResponse(it: InputStream, responseType: Class<T>): T {
        return if (responseType == String::class.java) {
            IOUtils.toString(it, Charset.defaultCharset()) as T
        } else {
            JacksonUtils.readValue(it, responseType)!!
        }
    }

    private fun basicHeaders(headers: Map<String, String>?):
            Array<BasicHeader> {

        val basicHeaders = mutableListOf<BasicHeader>()
        defaultHeaders().forEach { name, value ->
            basicHeaders.add(BasicHeader(name, value))
        }
        headers?.forEach { name, value ->
            basicHeaders.add(BasicHeader(name, value))
        }
        return basicHeaders.toTypedArray()
    }

    // Non Blocking Rest Implementation
    suspend fun httpClientNB(): CloseableHttpClient {
        return HttpClients.custom()
                .addInterceptorFirst(WebClientUtils.logRequest())
                .addInterceptorLast(WebClientUtils.logResponse())
                .build()
    }
}