/*
 * Copyright © 2019 IBM, Bell Canada.
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
package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.mock

import org.apache.http.message.BasicHeader
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.nio.charset.Charset
import java.util.Base64

class MockBlueprintWebClientService(private var restClientProperties: RestClientProperties) :
    BlueprintWebClientService {

    private var mockServer: ClientAndServer
    private var port: String = if (restClientProperties.url.split(":")[2].isEmpty()) "8080"
    else restClientProperties.url.split(":")[2]
    private var headers: Map<String, String>

    init {
        mockServer = ClientAndServer.startClientAndServer(port.toInt())
        headers = defaultHeaders()

        // Create expected requests and responses
        setRequest("GET", "/aai/v21/network/generic-vnfs/generic-vnf/123456")
        setRequest(
            "GET",
            "/config/GENERIC-RESOURCE-API:services/service/10/service-data/vnfs/vnf/123456/" +
                "vnf-data/vnf-topology/vnf-parameters-data/param/vnf_name"
        )
        setRequestWithPayload(
            "PUT", "/query",
            "{\r\n\"start\": \"\\/nodes\\/vf-modules?vf-module-name=vf-module-name\",\r\n\"query\": \"\\/query\\/related-to?startingNodeType=vf-module&relatedToNodeType=generic-vnf\"\r\n}"
        )
    }

    override fun defaultHeaders(): Map<String, String> {
        val encodedCredentials = this.setBasicAuth("admin", "aaiTest")
        return mapOf(
            HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.AUTHORIZATION to "Basic $encodedCredentials"
        )
    }

    override fun host(uri: String): String {
        return restClientProperties.url + uri
    }

    fun tearDown() {
        mockServer.close()
    }

    override fun exchangeResource(
        method: String,
        path: String,
        payload: String
    ): BlueprintWebClientService.WebClientResponse<String> {
        val header = arrayOf(BasicHeader(HttpHeaders.AUTHORIZATION, headers[HttpHeaders.AUTHORIZATION]))
        return when (method) {
            "POST" -> {
                post(path, payload, header, String::class.java)
            }
            "PUT" -> {
                put(path, payload, header, String::class.java)
            }
            else -> {
                get(path, header, String::class.java)
            }
        }
    }

    private fun setRequest(method: String, path: String) {
        val requestResponse = when (method) {
            "POST" -> {
                "Post response"
            }
            "PUT" -> {
                "Put response"
            }
            else -> {
                "Get response"
            }
        }
        mockServer.`when`(
            request().withHeaders(Header(HttpHeaders.AUTHORIZATION, headers[HttpHeaders.AUTHORIZATION]))
                .withMethod(method)
                .withPath(path)
        ).respond(response().withStatusCode(200).withBody("{\"aai-resource\":\"$requestResponse\"}"))
    }

    private fun setRequestWithPayload(method: String, path: String, payload: String) {
        val requestResponse = when (method) {
            "POST" -> {
                "Post response"
            }
            "PUT" -> {
                "Put response"
            }
            else -> {
                "Get response"
            }
        }
        mockServer.`when`(
            request().withHeaders(Header(HttpHeaders.AUTHORIZATION, headers[HttpHeaders.AUTHORIZATION]))
                .withMethod(method)
                .withPath(path)
                .withQueryStringParameter("format", "resource")
                .withBody(payload)
        ).respond(response().withStatusCode(200).withBody("{\"aai-resource\":\"$requestResponse\"}"))
    }

    private fun setBasicAuth(username: String, password: String): String {
        val credentialsString = "$username:$password"
        return Base64.getEncoder().encodeToString(
            credentialsString.toByteArray(Charset.defaultCharset())
        )
    }
}
