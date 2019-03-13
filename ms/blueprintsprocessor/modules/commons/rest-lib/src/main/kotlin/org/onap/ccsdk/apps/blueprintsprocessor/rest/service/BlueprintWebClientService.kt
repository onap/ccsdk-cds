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

import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.onap.ccsdk.apps.blueprintsprocessor.rest.utils.WebClientUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.springframework.http.HttpMethod
import java.nio.charset.Charset

interface BlueprintWebClientService {

    fun headers(): Array<BasicHeader>

    fun host(uri: String): String

    fun httpClient(): CloseableHttpClient {
        return HttpClients.custom()
            .addInterceptorFirst(WebClientUtils.logRequest())
            .addInterceptorLast(WebClientUtils.logResponse())
            .build()
    }

    fun exchangeResource(methodType: String, path: String, request: String): String {
        return when (HttpMethod.resolve(methodType)) {
            HttpMethod.DELETE -> delete(path)
            HttpMethod.GET -> get(path)
            HttpMethod.POST -> post(path, request)
            HttpMethod.PUT -> put(path, request)
            else -> throw BluePrintProcessorException("Unsupported methodType($methodType)")
        }
    }

    fun delete(path: String): String {
        val httpDelete = HttpDelete(host(path))
        httpDelete.setHeaders(headers())
        httpClient().execute(httpDelete).entity.content.use {
            return IOUtils.toString(it, Charset.defaultCharset())
        }
    }

    fun get(path: String): String {
        val httpGet = HttpGet(host(path))
        httpGet.setHeaders(headers())
        httpClient().execute(httpGet).entity.content.use {
            return IOUtils.toString(it, Charset.defaultCharset())
        }
    }

    fun post(path: String, request: String): String {
        val httpPost = HttpPost(host(path))
        val entity = StringEntity(request)
        httpPost.entity = entity
        httpPost.setHeaders(headers())
        httpClient().execute(httpPost).entity.content.use {
            return IOUtils.toString(it, Charset.defaultCharset())
        }
    }

    fun put(path: String, request: String): String {
        val httpPut = HttpPut(host(path))
        val entity = StringEntity(request)
        httpPut.entity = entity
        httpPut.setHeaders(headers())
        httpClient().execute(httpPut).entity.content.use {
            return IOUtils.toString(it, Charset.defaultCharset())
        }
    }
}