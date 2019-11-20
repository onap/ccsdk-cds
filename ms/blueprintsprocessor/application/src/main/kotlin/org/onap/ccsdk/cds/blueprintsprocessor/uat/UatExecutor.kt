/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.cds.blueprintsprocessor.uat

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.atLeast
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import java.util.concurrent.ConcurrentHashMap
import org.apache.http.HttpHeaders
import org.apache.http.HttpStatus
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.mockito.Answers
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService.WebClientResponse
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.LogColor.COLOR_MOCKITO
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.LogColor.markerOf
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.MockInvocationLogger
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.Base64Utils

/**
 * Assumptions:
 *
 * - Application HTTP service is bound to loopback interface;
 * - Password is either defined in plain (with "{noop}" prefix), or it's the same of username.
 *
 * @author Eliezio Oliveira
 */
@Component
class UatExecutor(
    private val environment: ConfigurableEnvironment,
    private val restClientFactory: BluePrintRestLibPropertyService,
    private val mapper: ObjectMapper
) {

    companion object {
        private const val NOOP_PASSWORD_PREFIX = "{noop}"

        private val log: Logger = LoggerFactory.getLogger(UatExecutor::class.java)
        private val mockLoggingListener = MockInvocationLogger(markerOf(COLOR_MOCKITO))
    }

    // use lazy evaluation to postpone until localServerPort is injected by Spring
    private val baseUrl: String by lazy {
        "http://127.0.0.1:${localServerPort()}"
    }

    @Throws(AssertionError::class)
    fun execute(uatSpec: String, cbaBytes: ByteArray) {
        val uat = UatDefinition.load(mapper, uatSpec)
        execute(uat, cbaBytes)
    }

    /**
     *
     * The UAT can range from minimum to completely defined.
     *
     * @return an updated UAT with all NB and SB messages.
     */
    @Throws(AssertionError::class)
    fun execute(uat: UatDefinition, cbaBytes: ByteArray): UatDefinition {
        val defaultHeaders = listOf(BasicHeader(HttpHeaders.AUTHORIZATION, clientAuthToken()))
        val httpClient = HttpClientBuilder.create()
            .setDefaultHeaders(defaultHeaders)
            .build()
        // Only if externalServices are defined
        val mockInterceptor = MockPreInterceptor()
        // Always defined and used, whatever the case
        val spyInterceptor = SpyPostInterceptor(mapper)
        restClientFactory.setInterceptors(mockInterceptor, spyInterceptor)
        try {
            // Configure mocked external services and save their expected requests for further validation
            val requestsPerClient = uat.externalServices.associateBy(
                { service ->
                    createRestClientMock(service.expectations).also { restClient ->
                        // side-effect: register restClient to override real instance
                        mockInterceptor.registerMock(service.selector, restClient)
                    }
                },
                { service -> service.expectations.map { it.request } }
            )

            val newProcesses = httpClient.use { client ->
                uploadBlueprint(client, cbaBytes)

                // Run processes
                uat.processes.map { process ->
                    log.info("Executing process '${process.name}'")
                    val responseNormalizer = JsonNormalizer.getNormalizer(mapper, process.responseNormalizerSpec)
                    val actualResponse = processBlueprint(client, process.request,
                        process.expectedResponse, responseNormalizer)
                    ProcessDefinition(process.name, process.request, actualResponse, process.responseNormalizerSpec)
                }
            }

            // Validate requests to external services
            for ((mockClient, requests) in requestsPerClient) {
                requests.forEach { request ->
                    verify(mockClient, atLeastOnce()).exchangeResource(
                        eq(request.method),
                        eq(request.path),
                        argThat { assertJsonEquals(request.body, this) },
                        argThat(RequiredMapEntriesMatcher(request.headers)))
                }
                // Don't mind the invocations to the overloaded exchangeResource(String, String, String)
                verify(mockClient, atLeast(0)).exchangeResource(any(), any(), any())
                verifyNoMoreInteractions(mockClient)
            }

            val newExternalServices = spyInterceptor.getSpies()
                .map(SpyService::asServiceDefinition)

            return UatDefinition(newProcesses, newExternalServices)
        } finally {
            restClientFactory.clearInterceptors()
        }
    }

    private fun createRestClientMock(restExpectations: List<ExpectationDefinition>):
            BlueprintWebClientService {
        val restClient = mock<BlueprintWebClientService>(
            defaultAnswer = Answers.RETURNS_SMART_NULLS,
            // our custom verboseLogging handler
            invocationListeners = arrayOf(mockLoggingListener)
        )

        // Delegates to overloaded exchangeResource(String, String, String, Map<String, String>)
        whenever(restClient.exchangeResource(any(), any(), any()))
            .thenAnswer { invocation ->
                val method = invocation.arguments[0] as String
                val path = invocation.arguments[1] as String
                val request = invocation.arguments[2] as String
                restClient.exchangeResource(method, path, request, emptyMap())
            }
        for (expectation in restExpectations) {
            whenever(restClient.exchangeResource(
                eq(expectation.request.method),
                eq(expectation.request.path),
                any(),
                any()))
                .thenReturn(WebClientResponse(expectation.response.status, expectation.response.body.toString()))
        }
        return restClient
    }

    @Throws(AssertionError::class)
    private fun uploadBlueprint(client: HttpClient, cbaBytes: ByteArray) {
        val multipartEntity = MultipartEntityBuilder.create()
            .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
            .addBinaryBody("file", cbaBytes, ContentType.DEFAULT_BINARY, "cba.zip")
            .build()
        val request = HttpPost("$baseUrl/api/v1/blueprint-model/publish").apply {
            entity = multipartEntity
        }
        client.execute(request) { response ->
            val statusLine = response.statusLine
            assertThat(statusLine.statusCode, equalTo(HttpStatus.SC_OK))
        }
    }

    @Throws(AssertionError::class)
    private fun processBlueprint(
        client: HttpClient,
        requestBody: JsonNode,
        expectedResponse: JsonNode?,
        responseNormalizer: (String) -> String
    ): JsonNode {
        val stringEntity = StringEntity(mapper.writeValueAsString(requestBody), ContentType.APPLICATION_JSON)
        val request = HttpPost("$baseUrl/api/v1/execution-service/process").apply {
            entity = stringEntity
        }
        val response = client.execute(request) { response ->
            val statusLine = response.statusLine
            assertThat(statusLine.statusCode, equalTo(HttpStatus.SC_OK))
            val entity = response.entity
            assertThat("Response contains no content", entity, notNullValue())
            entity.content.bufferedReader().use { it.readText() }
        }
        val actualResponse = responseNormalizer(response)
        if (expectedResponse != null) {
            assertJsonEquals(expectedResponse, actualResponse)
        }
        return mapper.readTree(actualResponse)!!
    }

    @Throws(AssertionError::class)
    private fun assertJsonEquals(expected: JsonNode?, actual: String): Boolean {
        // special case
        if ((expected == null) && actual.isBlank()) {
            return true
        }
        // general case
        JSONAssert.assertEquals(expected?.toString(), actual, JSONCompareMode.LENIENT)
        // assertEquals throws an exception whenever match fails
        return true
    }

    private fun localServerPort(): Int =
        (environment.getProperty("local.server.port")
            ?: environment.getRequiredProperty("blueprint.httpPort")).toInt()

    private fun clientAuthToken(): String {
        val username = environment.getRequiredProperty("security.user.name")
        val password = environment.getRequiredProperty("security.user.password")
        val plainPassword = when {
            password.startsWith(NOOP_PASSWORD_PREFIX) -> password.substring(NOOP_PASSWORD_PREFIX.length)
            else -> username
        }
        return "Basic " + Base64Utils.encodeToString("$username:$plainPassword".toByteArray())
    }

    private class MockPreInterceptor : BluePrintRestLibPropertyService.PreInterceptor {
        private val mocks = ConcurrentHashMap<String, BlueprintWebClientService>()

        override fun getInstance(jsonNode: JsonNode): BlueprintWebClientService? {
            TODO("jsonNode-keyed services not yet supported")
        }

        override fun getInstance(selector: String): BlueprintWebClientService? =
            mocks[selector]

        fun registerMock(selector: String, client: BlueprintWebClientService) {
            mocks[selector] = client
        }
    }

    private class SpyPostInterceptor(private val mapper: ObjectMapper) : BluePrintRestLibPropertyService.PostInterceptor {

        private val spies = ConcurrentHashMap<String, SpyService>()

        override fun getInstance(jsonNode: JsonNode, service: BlueprintWebClientService): BlueprintWebClientService {
            TODO("jsonNode-keyed services not yet supported")
        }

        override fun getInstance(selector: String, service: BlueprintWebClientService): BlueprintWebClientService {
            val spiedService = SpyService(mapper, selector, service)
            spies[selector] = spiedService
            return spiedService
        }

        fun getSpies(): List<SpyService> =
            spies.values.toList()
    }

    private class SpyService(
        private val mapper: ObjectMapper,
        val selector: String,
        private val realService: BlueprintWebClientService
    ) :
        BlueprintWebClientService by realService {

        private val expectations: MutableList<ExpectationDefinition> = mutableListOf()

        override fun exchangeResource(methodType: String, path: String, request: String): WebClientResponse<String> =
            exchangeResource(methodType, path, request, DEFAULT_HEADERS)

        override fun exchangeResource(
            methodType: String,
            path: String,
            request: String,
            headers: Map<String, String>
        ): WebClientResponse<String> {
            val requestDefinition = RequestDefinition(methodType, path, headers, toJson(request))
            val realAnswer = realService.exchangeResource(methodType, path, request, headers)
            val responseBody = when {
                // TODO: confirm if we need to normalize the response here
                realAnswer.status == HttpStatus.SC_OK -> toJson(realAnswer.body)
                else -> null
            }
            val responseDefinition = ResponseDefinition(realAnswer.status, responseBody)
            expectations.add(ExpectationDefinition(requestDefinition, responseDefinition))
            return realAnswer
        }

        fun asServiceDefinition() =
            ServiceDefinition(selector, expectations)

        private fun toJson(str: String): JsonNode? {
            return when {
                str.isNotBlank() -> mapper.readTree(str)
                else -> null
            }
        }

        companion object {
            private val DEFAULT_HEADERS = mapOf(
                HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE
            )
        }
    }
}
