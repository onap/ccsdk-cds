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
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.onap.ccsdk.apps.blueprintsprocessor.rest.service

import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.*
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.onap.ccsdk.apps.blueprintsprocessor.rest.utils.WebClientUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.springframework.http.HttpMethod
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

    fun exchangeResource(methodType: String, path: String, request: String): String {
        return this.exchangeResource(methodType, path, request, defaultHeaders())
    }

    fun exchangeResource(methodType: String, path: String, request: String, headers: Map<String, String>): String {
        val convertedHeaders: Array<BasicHeader> = convertToBasicHeaders(headers)
        return when (HttpMethod.resolve(methodType)) {
            HttpMethod.DELETE -> delete(path, convertedHeaders)
            HttpMethod.GET -> get(path, convertedHeaders)
            HttpMethod.POST -> post(path, request, convertedHeaders)
            HttpMethod.PUT -> put(path, request, convertedHeaders)
            HttpMethod.PATCH -> patch(path, request, convertedHeaders)
            else -> throw BluePrintProcessorException("Unsupported methodType($methodType)")
        }
    }

    fun convertToBasicHeaders(headers: Map<String, String>): Array<BasicHeader> {
        val convertedHeaders = Array<BasicHeader>(headers.size){ BasicHeader("","") }
        var currentElement = 0
        for ((name, value) in headers) {
            convertedHeaders[currentElement++] = BasicHeader(name, value)
        }
        return convertedHeaders
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

    fun post(path: String, request: String, headers: Array<BasicHeader>): String {
        val httpPost = HttpPost(host(path))
        val entity = StringEntity(request)
        httpPost.entity = entity
        httpPost.setHeaders(headers)
        httpClient().execute(httpPost).entity.content.use {
            return IOUtils.toString(it, Charset.defaultCharset())
        }
    }

    fun put(path: String, request: String, headers: Array<BasicHeader>): String {
        val httpPut = HttpPut(host(path))
        val entity = StringEntity(request)
        httpPut.entity = entity
        httpPut.setHeaders(headers)
        httpClient().execute(httpPut).entity.content.use {
            return IOUtils.toString(it, Charset.defaultCharset())
        }
    }

    fun patch(path: String, request: String, headers: Array<BasicHeader>): String {
        val httpPatch = HttpPatch(host(path))
        val entity = StringEntity(request)
        httpPatch.entity = entity
        httpPatch.setHeaders(headers)
        httpClient().execute(httpPatch).entity.content.use {
            return IOUtils.toString(it, Charset.defaultCharset())
        }
    }
}