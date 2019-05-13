/*
 * Copyright © 2017-2019 AT&T, Bell Canada, Nordix Foundation
 * Modifications Copyright © 2018-2019 IBM.
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

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.onap.ccsdk.cds.blueprintsprocessor.rest.utils.WebClientUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.http.HttpMethod
import java.io.IOException
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

    fun exchangeResource(methodType: String, path: String, request: String): TypedWebClientResponse<String> {
        return this.exchangeResource(methodType, path, request, defaultHeaders())
    }

    fun exchangeResource(methodType: String, path: String, request: String,
                         headers: Map<String, String>): TypedWebClientResponse<String> {
        /**
         * TODO: Basic headers in the implementations of this client do not get added
         * in blocking version, whereas in NB version defaultHeaders get added.
         * the difference is in convertToBasicHeaders vs basicHeaders
         */
        val convertedHeaders: Array<BasicHeader> = convertToBasicHeaders(headers)
        return when (HttpMethod.resolve(methodType)) {
            HttpMethod.DELETE -> delete(path, convertedHeaders, String::class.java)
            HttpMethod.GET -> get(path, convertedHeaders, String::class.java)
            HttpMethod.POST -> post(path, request, convertedHeaders, String::class.java)
            HttpMethod.PUT -> put(path, request, convertedHeaders, String::class.java)
            HttpMethod.PATCH -> patch(path, request, convertedHeaders, String::class.java)
            else -> throw BluePrintProcessorException("Unsupported met" +
                "hodType($methodType)")
        }
    }

    fun convertToBasicHeaders(headers: Map<String, String>): Array<BasicHeader> {
        return headers.map { BasicHeader(it.key, it.value) }.toTypedArray()
    }

    fun <T> delete(path: String, headers: Array<BasicHeader>, responseType: Class<T>): TypedWebClientResponse<T> {
        val httpDelete = HttpDelete(host(path))
        httpDelete.setHeaders(headers)
        return performCallAndExtractTypedWebClientResponse(httpDelete, responseType)
    }

    fun <T> get(path: String, headers: Array<BasicHeader>, responseType: Class<T>): TypedWebClientResponse<T> {
        val httpGet = HttpGet(host(path))
        httpGet.setHeaders(headers)
        return performCallAndExtractTypedWebClientResponse(httpGet, responseType)
    }

    fun <T> post(path: String, request: Any, headers: Array<BasicHeader>, responseType: Class<T>): TypedWebClientResponse<T> {
        val httpPost = HttpPost(host(path))
        val entity = StringEntity(strRequest(request))
        httpPost.entity = entity
        httpPost.setHeaders(headers)
        return performCallAndExtractTypedWebClientResponse(httpPost, responseType)
    }

    fun <T> put(path: String, request: Any, headers: Array<BasicHeader>, responseType: Class<T>): TypedWebClientResponse<T> {
        val httpPut = HttpPut(host(path))
        val entity = StringEntity(strRequest(request))
        httpPut.entity = entity
        httpPut.setHeaders(headers)
        return performCallAndExtractTypedWebClientResponse(httpPut, responseType)
    }

    fun <T> patch(path: String, request: Any, headers: Array<BasicHeader>, responseType: Class<T>): TypedWebClientResponse<T> {
        val httpPatch = HttpPatch(host(path))
        val entity = StringEntity(strRequest(request))
        httpPatch.entity = entity
        httpPatch.setHeaders(headers)
        return performCallAndExtractTypedWebClientResponse(httpPatch, responseType)
    }

    /**
     * Perform the HTTP call and return HTTP status code and body.
     * @param httpUriRequest {@link HttpUriRequest} object
     * @return {@link WebClientResponse} object
     * http client may throw IOException and ClientProtocolException on error
     */

    @Throws(IOException::class, ClientProtocolException::class)
    private fun <T> performCallAndExtractTypedWebClientResponse(
        httpUriRequest: HttpUriRequest, responseType: Class<T>):
        TypedWebClientResponse<T> {
        val httpResponse = httpClient().execute(httpUriRequest)
        val statusCode = httpResponse.statusLine.statusCode
        httpResponse.entity.content.use {
            val body = getResponse(it, responseType)
            return TypedWebClientResponse(statusCode, body)
        }
    }

    suspend fun getNB(path: String): TypedWebClientResponse<String> {
        return getNB(path, null, String::class.java)
    }

    suspend fun getNB(path: String, additionalHeaders: Array<BasicHeader>?): TypedWebClientResponse<String> {
        return getNB(path, additionalHeaders, String::class.java)
    }

    suspend fun <T> getNB(path: String, additionalHeaders: Array<BasicHeader>?, responseType: Class<T>):
        TypedWebClientResponse<T> =
        withContext(Dispatchers.IO) {
            get(path, additionalHeaders!!, responseType)
        }

    suspend fun postNB(path: String, request: Any): TypedWebClientResponse<String> {
        return postNB(path, request, null, String::class.java)
    }

    suspend fun postNB(path: String, request: Any, additionalHeaders: Array<BasicHeader>?): TypedWebClientResponse<String> {
        return postNB(path, request, additionalHeaders, String::class.java)
    }

    suspend fun <T> postNB(path: String, request: Any, additionalHeaders: Array<BasicHeader>?,
                           responseType: Class<T>): TypedWebClientResponse<T> =
        withContext(Dispatchers.IO) {
            post(path, request, additionalHeaders!!, responseType)
        }

    suspend fun putNB(path: String, request: Any): TypedWebClientResponse<String> {
        return putNB(path, request, null, String::class.java)
    }

    suspend fun putNB(path: String, request: Any,
                      additionalHeaders: Array<BasicHeader>?): TypedWebClientResponse<String> {
        return putNB(path, request, additionalHeaders, String::class.java)
    }

    suspend fun <T> putNB(path: String, request: Any,
                          additionalHeaders: Array<BasicHeader>?,
                          responseType: Class<T>): TypedWebClientResponse<T> =
        withContext(Dispatchers.IO) {
            put(path, request, additionalHeaders!!, responseType)
        }

    suspend fun <T> deleteNB(path: String): TypedWebClientResponse<String> {
        return deleteNB(path, null, String::class.java)
    }

    suspend fun <T> deleteNB(path: String, additionalHeaders: Array<BasicHeader>?):
        TypedWebClientResponse<String> {
        return deleteNB(path, additionalHeaders, String::class.java)
    }

    suspend fun <T> deleteNB(path: String, additionalHeaders: Array<BasicHeader>?, responseType: Class<T>):
        TypedWebClientResponse<T> =
        withContext(Dispatchers.IO) {
            delete(path, additionalHeaders!!, responseType)
        }

    suspend fun <T> patchNB(path: String, request: Any, additionalHeaders: Array<BasicHeader>?, responseType: Class<T>):
        TypedWebClientResponse<T> =
        withContext(Dispatchers.IO) {
            patch(path, request, additionalHeaders!!, responseType)
        }

    suspend fun exchangeNB(methodType: String, path: String, request: Any): TypedWebClientResponse<String> {
        return exchangeNB(methodType, path, request, hashMapOf(),
            String::class.java)
    }

    suspend fun exchangeNB(methodType: String, path: String, request: Any, additionalHeaders: Map<String, String>?):
        TypedWebClientResponse<String> {
        return exchangeNB(methodType, path, request, additionalHeaders, String::class.java)
    }

    suspend fun <T> exchangeNB(methodType: String, path: String, request: Any,
                               additionalHeaders: Map<String, String>?,
                               responseType: Class<T>): TypedWebClientResponse<T> {

        //TODO: possible inconsistency
        //NOTE: this basic headers function is different from non-blocking
        val convertedHeaders: Array<BasicHeader> = basicHeaders(additionalHeaders!!)
        return when (HttpMethod.resolve(methodType)) {
            HttpMethod.GET -> getNB(path, convertedHeaders, responseType)
            HttpMethod.POST -> postNB(path, request, convertedHeaders, responseType)
            HttpMethod.DELETE -> deleteNB(path, convertedHeaders, responseType)
            HttpMethod.PUT -> putNB(path, request, convertedHeaders, responseType)
            HttpMethod.PATCH -> patchNB(path, request, convertedHeaders, responseType)
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
        defaultHeaders().forEach { (name, value) ->
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

    //TODO maybe there could be cases where we care about return headers?
    data class TypedWebClientResponse<T>(val status: Int, val body: T)
}
