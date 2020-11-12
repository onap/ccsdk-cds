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
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.rest.utils.WebClientUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintRetryException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintIOUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.util.UriUtils
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

interface BlueprintWebClientService {

    fun defaultHeaders(): Map<String, String>

    fun host(uri: String): String

    fun httpClient(): CloseableHttpClient {
        return HttpClients.custom()
            .addInterceptorFirst(WebClientUtils.logRequest())
            .addInterceptorLast(WebClientUtils.logResponse())
            .build()
    }

    /** High performance non blocking Retry function, If execution block [block] throws BluePrintRetryException
     * exception then this will perform wait and retrigger accoring to times [times] with delay [delay]
     */
    suspend fun <T> retry(
        times: Int = 1,
        initialDelay: Long = 0,
        delay: Long = 1000,
        block: suspend (Int) -> T
    ): T {
        val exceptionBlock = { e: Exception ->
            if (e !is BluePrintRetryException) {
                throw e
            }
        }
        return BluePrintIOUtils.retry(times, initialDelay, delay, block, exceptionBlock)
    }

    fun exchangeResource(methodType: String, path: String, request: String): WebClientResponse<String> {
        return this.exchangeResource(methodType, path, request, defaultHeaders())
    }

    fun exchangeResource(
        methodType: String,
        path: String,
        request: String,
        headers: Map<String, String>
    ): WebClientResponse<String> {
        /**
         * TODO: Basic headers in the implementations of this client do not get added
         * in blocking version, whereas in NB version defaultHeaders get added.
         * the difference is in convertToBasicHeaders vs basicHeaders
         */
        val convertedHeaders: Array<BasicHeader> = convertToBasicHeaders(headers)
        val encodedPath = if (path.contains('%')) path else
            UriUtils.encodeQuery(path, StandardCharsets.UTF_8.name())
        return when (HttpMethod.resolve(methodType)) {
            HttpMethod.DELETE -> delete(encodedPath, convertedHeaders, String::class.java)
            HttpMethod.GET -> get(encodedPath, convertedHeaders, String::class.java)
            HttpMethod.POST -> post(encodedPath, request, convertedHeaders, String::class.java)
            HttpMethod.PUT -> put(encodedPath, request, convertedHeaders, String::class.java)
            HttpMethod.PATCH -> patch(encodedPath, request, convertedHeaders, String::class.java)
            else -> throw BluePrintProcessorException(
                "Unsupported methodType($methodType) attempted on path($encodedPath)"
            )
        }
    }

    // TODO: convert to multi-map
    fun convertToBasicHeaders(headers: Map<String, String>): Array<BasicHeader> {
        return headers.map { BasicHeader(it.key, it.value) }.toTypedArray()
    }

    fun <T> delete(path: String, headers: Array<BasicHeader>, responseType: Class<T>): WebClientResponse<T> {
        val httpDelete = HttpDelete(host(path))
        RestLoggerService.httpInvoking(headers)
        httpDelete.setHeaders(headers)
        return performCallAndExtractTypedWebClientResponse(httpDelete, responseType)
    }

    fun <T> get(path: String, headers: Array<BasicHeader>, responseType: Class<T>): WebClientResponse<T> {
        val httpGet = HttpGet(host(path))
        RestLoggerService.httpInvoking(headers)
        httpGet.setHeaders(headers)
        return performCallAndExtractTypedWebClientResponse(httpGet, responseType)
    }

    fun <T> post(path: String, request: Any, headers: Array<BasicHeader>, responseType: Class<T>): WebClientResponse<T> {
        val httpPost = HttpPost(host(path))
        val entity = StringEntity(strRequest(request))
        httpPost.entity = entity
        RestLoggerService.httpInvoking(headers)
        httpPost.setHeaders(headers)
        return performCallAndExtractTypedWebClientResponse(httpPost, responseType)
    }

    fun <T> put(path: String, request: Any, headers: Array<BasicHeader>, responseType: Class<T>): WebClientResponse<T> {
        val httpPut = HttpPut(host(path))
        val entity = StringEntity(strRequest(request))
        httpPut.entity = entity
        RestLoggerService.httpInvoking(headers)
        httpPut.setHeaders(headers)
        return performCallAndExtractTypedWebClientResponse(httpPut, responseType)
    }

    fun <T> patch(path: String, request: Any, headers: Array<BasicHeader>, responseType: Class<T>): WebClientResponse<T> {
        val httpPatch = HttpPatch(host(path))
        val entity = StringEntity(strRequest(request))
        httpPatch.entity = entity
        RestLoggerService.httpInvoking(headers)
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
        httpUriRequest: HttpUriRequest,
        responseType: Class<T>
    ):
            WebClientResponse<T> {
        val httpResponse = httpClient().execute(httpUriRequest)
        val statusCode = httpResponse.statusLine.statusCode
        httpResponse.entity.content.use {
            val body = getResponse(it, responseType)
            return WebClientResponse(statusCode, body)
        }
    }

    suspend fun getNB(path: String): WebClientResponse<String> {
        return getNB(path, null, String::class.java)
    }

    suspend fun getNB(path: String, additionalHeaders: Array<BasicHeader>?): WebClientResponse<String> {
        return getNB(path, additionalHeaders, String::class.java)
    }

    suspend fun <T> getNB(path: String, additionalHeaders: Array<BasicHeader>?, responseType: Class<T>):
            WebClientResponse<T> = withContext(Dispatchers.IO) {
        get(path, additionalHeaders!!, responseType)
    }

    suspend fun postNB(path: String, request: Any): WebClientResponse<String> {
        return postNB(path, request, null, String::class.java)
    }

    suspend fun postNB(path: String, request: Any, additionalHeaders: Array<BasicHeader>?): WebClientResponse<String> {
        return postNB(path, request, additionalHeaders, String::class.java)
    }

    suspend fun <T> postNB(
        path: String,
        request: Any,
        additionalHeaders: Array<BasicHeader>?,
        responseType: Class<T>
    ): WebClientResponse<T> = withContext(Dispatchers.IO) {
        post(path, request, additionalHeaders!!, responseType)
    }

    suspend fun putNB(path: String, request: Any): WebClientResponse<String> {
        return putNB(path, request, null, String::class.java)
    }

    suspend fun putNB(
        path: String,
        request: Any,
        additionalHeaders: Array<BasicHeader>?
    ): WebClientResponse<String> {
        return putNB(path, request, additionalHeaders, String::class.java)
    }

    suspend fun <T> putNB(
        path: String,
        request: Any,
        additionalHeaders: Array<BasicHeader>?,
        responseType: Class<T>
    ): WebClientResponse<T> = withContext(Dispatchers.IO) {
        put(path, request, additionalHeaders!!, responseType)
    }

    suspend fun <T> deleteNB(path: String): WebClientResponse<String> {
        return deleteNB(path, null, String::class.java)
    }

    suspend fun <T> deleteNB(path: String, additionalHeaders: Array<BasicHeader>?):
            WebClientResponse<String> {
        return deleteNB(path, additionalHeaders, String::class.java)
    }

    suspend fun <T> deleteNB(path: String, additionalHeaders: Array<BasicHeader>?, responseType: Class<T>):
            WebClientResponse<T> = withContext(Dispatchers.IO) {
        delete(path, additionalHeaders!!, responseType)
    }

    suspend fun <T> patchNB(path: String, request: Any, additionalHeaders: Array<BasicHeader>?, responseType: Class<T>):
            WebClientResponse<T> = withContext(Dispatchers.IO) {
        patch(path, request, additionalHeaders!!, responseType)
    }

    suspend fun exchangeNB(methodType: String, path: String, request: Any): WebClientResponse<String> {
        return exchangeNB(
            methodType, path, request, hashMapOf(),
            String::class.java
        )
    }

    suspend fun exchangeNB(methodType: String, path: String, request: Any, additionalHeaders: Map<String, String>?):
            WebClientResponse<String> {
        return exchangeNB(methodType, path, request, additionalHeaders, String::class.java)
    }

    suspend fun <T> exchangeNB(
        methodType: String,
        path: String,
        request: Any,
        additionalHeaders: Map<String, String>?,
        responseType: Class<T>
    ): WebClientResponse<T> {

        // TODO: possible inconsistency
        // NOTE: this basic headers function is different from non-blocking
        val convertedHeaders: Array<BasicHeader> = basicHeaders(additionalHeaders!!)
        return when (HttpMethod.resolve(methodType)) {
            HttpMethod.GET -> getNB(path, convertedHeaders, responseType)
            HttpMethod.POST -> postNB(path, request, convertedHeaders, responseType)
            HttpMethod.DELETE -> deleteNB(path, convertedHeaders, responseType)
            HttpMethod.PUT -> putNB(path, request, convertedHeaders, responseType)
            HttpMethod.PATCH -> patchNB(path, request, convertedHeaders, responseType)
            else -> throw BluePrintProcessorException("Unsupported methodType($methodType)")
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

    // TODO maybe there could be cases where we care about return headers?
    data class WebClientResponse<T>(val status: Int, val body: T)

    fun verifyAdditionalHeaders(restClientProperties: RestClientProperties): Map<String, String> {
        val customHeaders: MutableMap<String, String> = mutableMapOf()
        // Extract additionalHeaders from the requestProperties and
        // throw an error if HttpHeaders.AUTHORIZATION key (headers are case-insensitive)
        restClientProperties.additionalHeaders?.let {
            if (it.keys.map { k -> k.toLowerCase().trim() }.contains(HttpHeaders.AUTHORIZATION.toLowerCase())) {
                val errMsg = "Error in definition of endpoint ${restClientProperties.url}." +
                        " User-supplied \"additionalHeaders\" cannot contain AUTHORIZATION header with" +
                        " auth-type \"${RestLibConstants.TYPE_BASIC_AUTH}\""
                WebClientUtils.log.error(errMsg)
                throw BluePrintProcessorException(errMsg)
            } else {
                customHeaders.putAll(it)
            }
        }
        return customHeaders
    }
}
