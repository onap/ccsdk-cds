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

import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

class MockBlueprintWebClientService(private var restClientProperties: RestClientProperties): BlueprintWebClientService {
    private var mockServer: ClientAndServer
    private var port: String = if (restClientProperties.url.split(":")[2].isEmpty()) "8080"
                            else restClientProperties.url.split(":")[2]
    private val POST_RESPONSE: String = "post"
    private val PUT_RESPONSE: String = "put"
    private val GET_RESPONSE: String = "get"

    init {
        mockServer  = ClientAndServer.startClientAndServer(port.toInt())

        // Create expected requests and responses
        //setRequest("POST", "30800/aai/v14/network/generic-vnfs/generic-vnf/123456", "")
        setRequest("GET", "/aai/v14/network/generic-vnfs/generic-vnf/123456", "")
        setRequest("GET", "/config/GENERIC-RESOURCE-API:services/service/10/service-data/vnfs/vnf/123456/vnf-data/vnf-topology/vnf-parameters-data/param/vnf_name", "")
        //setRequest("PUT", "aai/v14/network/generic-vnfs/generic-vnf/123456", "")
    }

    override fun defaultHeaders(): Map<String, String> {
        return mapOf(
                HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE)
    }

    override fun host(uri: String): String {
        return restClientProperties.url + uri
    }

    fun tearDown() {
        mockServer.close()
    }

    override fun exchangeResource(method: String, path: String, payload: String): String {
        return when (method) {
            "POST" -> {
                post(path, payload, emptyArray())
            }
            "PUT" -> {
                put(path, payload, emptyArray())
            }
            else -> {
                get(path, emptyArray())
            }
        }
    }

    private fun setRequest(method: String, path: String, payload: String) {
        val requestResponse = when (method) {
            "POST" -> {
                POST_RESPONSE
            }
            "PUT" -> {
                PUT_RESPONSE
            }
            else -> {
                GET_RESPONSE
            }

        }
        mockServer.`when`(
                request().withMethod(method)
                        .withPath(path)
                        .withBody(payload)
        ).respond(response().withStatusCode(200).withBody("{\"aai-resource\":\"$requestResponse\"}"))
    }
}