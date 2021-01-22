/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
 * Modifications Copyright © 2019 Huawei.
 * Modifications Copyright © 2019 Bell Canada.
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
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BlueprintRestLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.rest.SSLBasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.SSLRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.SSLTokenAuthRestClientProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [
        BlueprintRestLibConfiguration::class, BlueprintPropertyConfiguration::class,
        BlueprintPropertiesService::class
    ]
)
@TestPropertySource(
    properties =
        [
            "blueprintsprocessor.restclient.sample.type=basic-auth",
            "blueprintsprocessor.restclient.sample.url=http://localhost:8080",
            "blueprintsprocessor.restclient.sample.userId=sampleuser",
            "blueprintsprocessor.restclient.sslbasic.type=ssl-basic-auth",
            "blueprintsprocessor.restclient.sslbasic.url=https://localhost:8443",
            "blueprintsprocessor.restclient.sslbasic.username=admin",
            "blueprintsprocessor.restclient.sslbasic.password=cds",
            "blueprintsprocessor.restclient.sslbasic.keyStoreInstance=PKCS12",
            "blueprintsprocessor.restclient.sslbasic.sslTrust=src/test/resources/keystore.p12",
            "blueprintsprocessor.restclient.sslbasic.sslTrustPassword=changeit",
            "blueprintsprocessor.restclient.ssltoken.type=ssl-token-auth",
            "blueprintsprocessor.restclient.ssltoken.url=https://localhost:8443",
            "blueprintsprocessor.restclient.ssltoken.token=72178473kjshdkjgvbsdkjv903274908",
            "blueprintsprocessor.restclient.ssltoken.keyStoreInstance=PKCS12",
            "blueprintsprocessor.restclient.ssltoken.sslTrust=src/test/resources/keystore.p12",
            "blueprintsprocessor.restclient.ssltoken.sslTrustPassword=changeit",
            "blueprintsprocessor.restclient.ssl.type=ssl-no-auth",
            "blueprintsprocessor.restclient.ssl.url=https://localhost:8443",
            "blueprintsprocessor.restclient.ssl.keyStoreInstance=PKCS12",
            "blueprintsprocessor.restclient.ssl.sslTrust=src/test/resources/keystore.p12",
            "blueprintsprocessor.restclient.ssl.sslTrustPassword=changeit",
            "blueprintsprocessor.restclient.ssl.sslKey=src/test/resources/keystore.p12",
            "blueprintsprocessor.restclient.ssl.sslKeyPassword=changeit"
        ]
)
class BlueprintRestLibPropertyServiceTest {

    @Autowired
    lateinit var bluePrintRestLibPropertyService: BlueprintRestLibPropertyService

    @Test
    fun testRestClientProperties() {
        val properties = bluePrintRestLibPropertyService.restClientProperties(
            "blueprintsprocessor.restclient.sample"
        )
        assertNotNull(properties, "failed to create property bean")
        assertNotNull(
            properties.url,
            "failed to get url property in" +
                " property bean"
        )
    }

    @Test
    fun testSSLBasicProperties() {
        val properties = bluePrintRestLibPropertyService.restClientProperties(
            "blueprintsprocessor.restclient.sslbasic"
        )
        assertNotNull(properties, "failed to create property bean")
        val p: SSLBasicAuthRestClientProperties =
            properties as SSLBasicAuthRestClientProperties

        assertEquals(p.basicAuth!!.username, "admin")
        assertEquals(p.basicAuth!!.password, "cds")
        assertEquals(p.sslTrust, "src/test/resources/keystore.p12")
        assertEquals(p.sslTrustPassword, "changeit")
        assertEquals(p.keyStoreInstance, "PKCS12")
    }

    @Test
    fun testSSLTokenProperties() {
        val properties = bluePrintRestLibPropertyService.restClientProperties(
            "blueprintsprocessor.restclient.ssltoken"
        )
        assertNotNull(properties, "failed to create property bean")

        val p: SSLTokenAuthRestClientProperties =
            properties as SSLTokenAuthRestClientProperties

        assertEquals(p.tokenAuth!!.token!!, "72178473kjshdkjgvbsdkjv903274908")
        assertEquals(p.sslTrust, "src/test/resources/keystore.p12")
        assertEquals(p.sslTrustPassword, "changeit")
        assertEquals(p.keyStoreInstance, "PKCS12")
    }

    @Test
    fun testSSLNoAuthProperties() {
        val properties = bluePrintRestLibPropertyService.restClientProperties(
            "blueprintsprocessor.restclient.ssl"
        )
        assertNotNull(properties, "failed to create property bean")

        val p: SSLRestClientProperties =
            properties as SSLRestClientProperties

        assertEquals(p.sslTrust, "src/test/resources/keystore.p12")
        assertEquals(p.sslTrustPassword, "changeit")
        assertEquals(p.keyStoreInstance, "PKCS12")
        assertEquals(p.sslKey, "src/test/resources/keystore.p12")
        assertEquals(p.sslKeyPassword, "changeit")
    }

    @Test
    fun testSSLBasicPropertiesAsJson() {
        val actualObj: JsonNode = defaultMapper.readTree(sslBasicAuthEndpointWithHeadersField())
        val properties = bluePrintRestLibPropertyService.restClientProperties(
            actualObj
        )
        assertNotNull(properties, "failed to create property bean")
        val p: SSLBasicAuthRestClientProperties = properties as SSLBasicAuthRestClientProperties

        assertEquals("admin", p.basicAuth!!.username)
        assertEquals("cds", p.basicAuth!!.password)
        assertEquals("src/test/resources/keystore.p12", p.sslTrust)
        assertEquals("changeit", p.sslTrustPassword)
        assertEquals("PKCS12", p.keyStoreInstance)
        assertEquals("ssl-basic-auth", p.type)
        assertEquals("https://localhost:8443", p.url)
    }

    @Test
    fun testSSLTokenPropertiesAsJson() {
        val actualObj: JsonNode = defaultMapper.readTree(sslTokenAuthEndpointWithHeadersField())
        val properties =
            bluePrintRestLibPropertyService.restClientProperties(actualObj)
        assertNotNull(properties, "failed to create property bean")

        val p: SSLTokenAuthRestClientProperties = properties as SSLTokenAuthRestClientProperties

        assertEquals("72178473kjshdkjgvbsdkjv903274908", p.tokenAuth!!.token!!)
        assertEquals("src/test/resources/keystore.p12", p.sslTrust)
        assertEquals("changeit", p.sslTrustPassword)
        assertEquals("PKCS12", p.keyStoreInstance)
        assertEquals("ssl-token-auth", p.type)
        assertEquals("https://localhost:8443", p.url)
    }

    @Test
    fun testSSLNoAuthPropertiesAsJson() {
        val actualObj: JsonNode = defaultMapper.readTree(sslNoAuthEndpointWithHeadersField())
        val properties = bluePrintRestLibPropertyService.restClientProperties(
            actualObj
        )
        assertNotNull(properties, "failed to create property bean")

        val p: SSLRestClientProperties =
            properties as SSLRestClientProperties

        assertEquals("src/test/resources/keystore.p12", p.sslTrust)
        assertEquals("changeit", p.sslTrustPassword)
        assertEquals("PKCS12", p.keyStoreInstance)
        assertEquals("src/test/resources/keystore.p12", p.sslKey)
        assertEquals("changeit", p.sslKeyPassword)
        assertEquals("ssl-no-auth", p.type)
        assertEquals("https://localhost:8443", p.url)
    }

    @Test
    fun testBlueprintWebClientService() {
        val blueprintWebClientService = bluePrintRestLibPropertyService
            .blueprintWebClientService("sample")
        assertNotNull(
            blueprintWebClientService,
            "failed to create blueprintWebClientService"
        )
    }

    @Test
    fun testBlueprintWebClientServiceWithJsonNode() {
        val actualObj: JsonNode = defaultMapper.readTree(sslBasicAuthEndpointWithHeadersField())
        val blueprintWebClientService = bluePrintRestLibPropertyService
            .blueprintWebClientService(actualObj)
        assertNotNull(blueprintWebClientService, "failed to create blueprintWebClientService")
    }

    // pass the result of $typeEndpointWithHeadersField() output with and without headers to compare.
    private fun validateHeadersDidNotChangeWithEmptyAdditionalHeaders(noHeaders: String, withHeaders: String) {
        val parsedObj: JsonNode = defaultMapper.readTree(noHeaders)
        val bpWebClientService =
            bluePrintRestLibPropertyService.blueprintWebClientService(parsedObj)
        val extractedHeaders = bpWebClientService.convertToBasicHeaders(mapOf())

        val parsedObjWithHeaders: JsonNode = defaultMapper.readTree(withHeaders)
        val bpWebClientServiceWithHeaders =
            bluePrintRestLibPropertyService.blueprintWebClientService(parsedObjWithHeaders)
        val extractedHeadersWithAdditionalHeaders = bpWebClientServiceWithHeaders.convertToBasicHeaders(mapOf())
        // Array<BasicHeader<>> -> Map<String,String>
        val headersMap = extractedHeaders.map { it.name to it.value }.toMap()
        val additionalHeadersMap = extractedHeadersWithAdditionalHeaders.map { it.name to it.value }.toMap()
        assertEquals(headersMap, additionalHeadersMap)
    }

    @Test
    fun `BasicAuth WebClientService with empty additionalHeaders does not modify headers`() {
        val endPointJson = basicAuthEndpointWithHeadersField()
        val endPointWithHeadersJson = basicAuthEndpointWithHeadersField(emptyAdditionalHeaders)
        validateHeadersDidNotChangeWithEmptyAdditionalHeaders(endPointJson, endPointWithHeadersJson)
    }

    private fun acceptsOneAdditionalHeadersTest(endPointWithHeadersJson: String) {
        val parsedObj: JsonNode = defaultMapper.readTree(endPointWithHeadersJson)
        val bpWebClientService =
            bluePrintRestLibPropertyService.blueprintWebClientService(parsedObj)
        val extractedHeaders = bpWebClientService.convertToBasicHeaders(mapOf())
        assertEquals(1, extractedHeaders.filter { it.name == "key1" }.count())
    }

    @Test
    fun `BasicAuth WebClientService accepts one additionalHeaders`() {
        val endPointWithHeadersJson = basicAuthEndpointWithHeadersField(oneAdditionalParameter)
        acceptsOneAdditionalHeadersTest(endPointWithHeadersJson)
    }

    private fun acceptsMultipleAdditionalHeaders(endPointWithHeadersJson: String) {
        val parsedObj: JsonNode = defaultMapper.readTree(endPointWithHeadersJson)
        val bpWebClientService =
            bluePrintRestLibPropertyService.blueprintWebClientService(parsedObj)
        val extractedHeaders = bpWebClientService.convertToBasicHeaders(mapOf())
        assertEquals(1, extractedHeaders.filter { it.name == "key1" }.count())
        assertEquals(1, extractedHeaders.filter { it.name == "key2" }.count())
        assertEquals(1, extractedHeaders.filter { it.name == "key3" }.count())
    }

    @Test
    fun `BasicAuth WebClientService accepts multiple additionalHeaders`() {
        val endPointWithHeadersJson = basicAuthEndpointWithHeadersField(threeAdditionalHeaders)
        acceptsMultipleAdditionalHeaders(endPointWithHeadersJson)
    }

    private fun additionalHeadersChangedContentTypeToAPPLICATION_XML(endPointWithHeadersJson: String) {
        val parsedObj: JsonNode = defaultMapper.readTree(endPointWithHeadersJson)
        val bpWebClientService =
            bluePrintRestLibPropertyService.blueprintWebClientService(parsedObj)
        val extractedHeaders = bpWebClientService.convertToBasicHeaders(mapOf())
        assertEquals(
            MediaType.APPLICATION_XML.toString(),
            extractedHeaders.filter { it.name == HttpHeaders.CONTENT_TYPE }[0].value!!
        )
    }

    @Test
    fun `BasicAuth WebClientService additionalHeaders can overwrite default Content-Type`() {
        // default content type is application/json
        val endPointWithHeadersJson = basicAuthEndpointWithHeadersField(contentTypeAdditionalHeader)
        additionalHeadersChangedContentTypeToAPPLICATION_XML(endPointWithHeadersJson)
    }

    // called from within "assertFailsWith(exceptionClass = BlueprintProcessorException::class) {"
    private fun attemptToPutAuthorizationHeaderIntoAdditionalHeaders(endPointWithHeadersJson: String) {
        val parsedObj: JsonNode = defaultMapper.readTree(endPointWithHeadersJson)
        val bpWebClientService =
            bluePrintRestLibPropertyService.blueprintWebClientService(parsedObj)
        bpWebClientService.convertToBasicHeaders(mapOf())
    }

    @Test
    fun `BasicAuth WebClientService throws BlueprintProcessorException if additionalHeaders contain Authorization`() {
        assertFailsWith(exceptionClass = BlueprintProcessorException::class) {
            val endPointWithHeadersJson = basicAuthEndpointWithHeadersField(additionalHeadersWithAuth)
            attemptToPutAuthorizationHeaderIntoAdditionalHeaders(endPointWithHeadersJson)
        }
        // spec says headers are case insensitive...
        assertFailsWith(exceptionClass = BlueprintProcessorException::class) {
            val endPointWithHeadersJson = basicAuthEndpointWithHeadersField(additionalHeadersWithAuthLowercased)
            attemptToPutAuthorizationHeaderIntoAdditionalHeaders(endPointWithHeadersJson)
        }
    }

    @Test
    fun `TokenAuth WebClientService with empty additionalHeaders does not modify headers`() {
        val endPointJson = sslTokenAuthEndpointWithHeadersField()
        val endPointWithHeadersJson = sslTokenAuthEndpointWithHeadersField(emptyAdditionalHeaders)
        validateHeadersDidNotChangeWithEmptyAdditionalHeaders(endPointJson, endPointWithHeadersJson)
    }

    @Test
    fun `TokenAuth WebClientService accepts one additionalHeaders`() {
        val endPointWithHeadersJson = sslTokenAuthEndpointWithHeadersField(oneAdditionalParameter)
        acceptsOneAdditionalHeadersTest(endPointWithHeadersJson)
    }

    @Test
    fun `TokenAuth WebClientService accepts multiple additionalHeaders`() {
        val endPointWithHeadersJson = sslTokenAuthEndpointWithHeadersField(threeAdditionalHeaders)
        acceptsMultipleAdditionalHeaders(endPointWithHeadersJson)
    }

    @Test
    fun `TokenAuth WebClientService additionalHeaders can overwrite default Content-Type`() {
        // default content type is application/json
        val endPointWithHeadersJson = sslTokenAuthEndpointWithHeadersField(contentTypeAdditionalHeader)
        additionalHeadersChangedContentTypeToAPPLICATION_XML(endPointWithHeadersJson)
    }

    @Test
    fun `TokenAuth WebClientService throws BlueprintProcessorException if additionalHeaders contain Authorization`() {
        assertFailsWith(exceptionClass = BlueprintProcessorException::class) {
            val endPointWithHeadersJson = sslTokenAuthEndpointWithHeadersField(additionalHeadersWithAuth)
            attemptToPutAuthorizationHeaderIntoAdditionalHeaders(endPointWithHeadersJson)
        }
        // spec says headers are case insensitive...
        assertFailsWith(exceptionClass = BlueprintProcessorException::class) {
            val endPointWithHeadersJson = sslTokenAuthEndpointWithHeadersField(additionalHeadersWithAuthLowercased)
            attemptToPutAuthorizationHeaderIntoAdditionalHeaders(endPointWithHeadersJson)
        }
    }

    // TESTS FOR SSL BASIC AUTH headers
    @Test
    fun `SSLBasicAuth WebClientService with empty additionalHeaders does not modify headers`() {
        val endPointJson = sslBasicAuthEndpointWithHeadersField()
        val endPointWithHeadersJson = sslBasicAuthEndpointWithHeadersField(emptyAdditionalHeaders)
        validateHeadersDidNotChangeWithEmptyAdditionalHeaders(endPointJson, endPointWithHeadersJson)
    }

    @Test
    fun `SSLBasicAuth WebClientService accepts one additionalHeaders`() {
        val endPointWithHeadersJson = sslBasicAuthEndpointWithHeadersField(oneAdditionalParameter)
        acceptsOneAdditionalHeadersTest(endPointWithHeadersJson)
    }

    @Test
    fun `SSLBasicAuth WebClientService accepts multiple additionalHeaders`() {
        val endPointWithHeadersJson = sslBasicAuthEndpointWithHeadersField(threeAdditionalHeaders)
        acceptsMultipleAdditionalHeaders(endPointWithHeadersJson)
    }

    @Test
    fun `SSLBasicAuth WebClientService additionalHeaders can overwrite default Content-Type`() {
        // default content type is application/json
        val endPointWithHeadersJson = sslBasicAuthEndpointWithHeadersField(contentTypeAdditionalHeader)
        additionalHeadersChangedContentTypeToAPPLICATION_XML(endPointWithHeadersJson)
    }

    @Test
    fun `SSLBasicAuth WebClientService throws BlueprintProcessorException if additionalHeaders contain Authorization`() {
        assertFailsWith(exceptionClass = BlueprintProcessorException::class) {
            val endPointWithHeadersJson = sslBasicAuthEndpointWithHeadersField(additionalHeadersWithAuth)
            attemptToPutAuthorizationHeaderIntoAdditionalHeaders(endPointWithHeadersJson)
        }
        // spec says headers are case insensitive...
        assertFailsWith(exceptionClass = BlueprintProcessorException::class) {
            val endPointWithHeadersJson = sslBasicAuthEndpointWithHeadersField(additionalHeadersWithAuthLowercased)
            attemptToPutAuthorizationHeaderIntoAdditionalHeaders(endPointWithHeadersJson)
        }
    }

    // SSL-NO-AUTH headers tests
    @Test
    fun `SSLNoAuth WebClientService with empty additionalHeaders does not modify headers`() {
        val endPointJson = sslNoAuthEndpointWithHeadersField()
        val endPointWithHeadersJson = sslNoAuthEndpointWithHeadersField(emptyAdditionalHeaders)
        validateHeadersDidNotChangeWithEmptyAdditionalHeaders(endPointJson, endPointWithHeadersJson)
    }

    @Test
    fun `SSLNoAuth WebClientService accepts one additionalHeaders`() {
        val endPointWithHeadersJson = sslNoAuthEndpointWithHeadersField(oneAdditionalParameter)
        acceptsOneAdditionalHeadersTest(endPointWithHeadersJson)
    }

    @Test
    fun `SSLNoAuth WebClientService accepts multiple additionalHeaders`() {
        val endPointWithHeadersJson = sslNoAuthEndpointWithHeadersField(threeAdditionalHeaders)
        acceptsMultipleAdditionalHeaders(endPointWithHeadersJson)
    }

    @Test
    fun `SSLNoAuth WebClientService additionalHeaders can overwrite default Content-Type`() {
        // default content type is application/json
        val endPointWithHeadersJson = sslNoAuthEndpointWithHeadersField(contentTypeAdditionalHeader)
        additionalHeadersChangedContentTypeToAPPLICATION_XML(endPointWithHeadersJson)
    }

    @Test
    fun `SSLNoAuth WebClientService throws BlueprintProcessorException if additionalHeaders contain Authorization`() {
        assertFailsWith(exceptionClass = BlueprintProcessorException::class) {
            val endPointWithHeadersJson = sslNoAuthEndpointWithHeadersField(additionalHeadersWithAuth)
            attemptToPutAuthorizationHeaderIntoAdditionalHeaders(endPointWithHeadersJson)
        }
        // spec says headers are case insensitive...
        assertFailsWith(exceptionClass = BlueprintProcessorException::class) {
            val endPointWithHeadersJson = sslNoAuthEndpointWithHeadersField(additionalHeadersWithAuthLowercased)
            attemptToPutAuthorizationHeaderIntoAdditionalHeaders(endPointWithHeadersJson)
        }
    }

    companion object BlueprintRestLibPropertyServiceTest {

        val defaultMapper = ObjectMapper()
        val expectedTokenAuthDefaultHeaders = mapOf<String, String>(
            "Content-Type" to "application/json",
            "Accept" to "application/json",
            "Authorization" to "72178473kjshdkjgvbsdkjv903274908"
        )

        val endPointWithHeadersJsonWithBasicAuthHeader = basicAuthEndpointWithHeadersField(
            """,
              "additionalHeaders" : {
                 "authorization": "Basic aGF2ZTphbmljZWRheQo="
              }
            """.trimIndent()
        )

        private fun sslTokenAuthEndpointWithHeadersField(headers: String = ""): String =
            """{
            "type" : "ssl-token-auth",
            "url" : "https://localhost:8443",
            "keyStoreInstance" : "PKCS12",
            "sslTrust" : "src/test/resources/keystore.p12",
            "sslTrustPassword" : "changeit",
              "tokenAuth" : {
                "token" : "72178473kjshdkjgvbsdkjv903274908"
              }$headers
            }
            """.trimIndent()

        private fun sslBasicAuthEndpointWithHeadersField(headers: String = ""): String =
            """{
          "type" : "ssl-basic-auth",
          "url" : "https://localhost:8443",
          "keyStoreInstance" : "PKCS12",
          "sslTrust" : "src/test/resources/keystore.p12",
          "sslTrustPassword" : "changeit",
          "basicAuth" : {
            "username" : "admin",
            "password" : "cds"
          }$headers
        }
            """.trimIndent()

        private fun sslNoAuthEndpointWithHeadersField(headers: String = ""): String = """{
          "type" : "ssl-no-auth",
          "url" : "https://localhost:8443",
          "keyStoreInstance" : "PKCS12",
          "sslTrust" : "src/test/resources/keystore.p12",
          "sslTrustPassword" : "changeit",
          "sslKey" : "src/test/resources/keystore.p12",
          "sslKeyPassword" : "changeit"$headers
        }
        """.trimIndent()

        // Don't forget to supply "," as the first char to make valid JSON
        private fun basicAuthEndpointWithHeadersField(headers: String = ""): String =
            """{
              "type": "basic-auth",
              "url": "http://127.0.0.1:8000",
              "username": "user",
              "password": "pass"$headers
            }
            """.trimIndent()

        private val emptyAdditionalHeaders = """,
          "additionalHeaders" : {
          }
        """.trimIndent()

        private val oneAdditionalParameter = """,
          "additionalHeaders" : {
            "key1": "value1"
          }
        """.trimIndent()

        private val threeAdditionalHeaders = """,
          "additionalHeaders" : {
            "key1": "value1",
            "key2": "value2",
            "key3": "value3"
          }
        """.trimIndent()

        private val contentTypeAdditionalHeader = """,
          "additionalHeaders" : {
            "${HttpHeaders.CONTENT_TYPE}": "${MediaType.APPLICATION_XML}"
          }
        """.trimIndent()

        private val additionalHeadersWithAuth = """,
          "additionalHeaders" : {
             "Authorization": "Basic aGF2ZTphbmljZWRheQo="
          }
        """.trimIndent()

        private val additionalHeadersWithAuthLowercased = """,
          "additionalHeaders" : {
             "authorization": "Basic aGF2ZTphbmljZWRheQo="
          }
        """.trimIndent()
    }
}
