/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BluePrintRestLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.rest.SSLBasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.SSLRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.SSLTokenAuthRestClientProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [BluePrintRestLibConfiguration::class, BlueprintPropertyConfiguration::class, BluePrintProperties::class])
@TestPropertySource(properties =
["blueprintsprocessor.restclient.sample.type=basic-auth",
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
])

class BluePrintRestLibPropertyServiceTest {

    @Autowired
    lateinit var bluePrintRestLibPropertyService: BluePrintRestLibPropertyService

    @Test
    fun testRestClientProperties() {
        val properties = bluePrintRestLibPropertyService.restClientProperties(
                "blueprintsprocessor.restclient.sample")
        assertNotNull(properties, "failed to create property bean")
        assertNotNull(properties.url, "failed to get url property in" +
                " property bean")
    }

    @Test
    fun testSSLBasicProperties() {
        val properties = bluePrintRestLibPropertyService.restClientProperties(
                "blueprintsprocessor.restclient.sslbasic")
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
                "blueprintsprocessor.restclient.ssltoken")
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
                "blueprintsprocessor.restclient.ssl")
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
        val json: String = "{\n" +
                "  \"type\" : \"ssl-basic-auth\",\n" +
                "  \"url\" : \"https://localhost:8443\",\n" +
                "  \"keyStoreInstance\" : \"PKCS12\",\n" +
                "  \"sslTrust\" : \"src/test/resources/keystore.p12\",\n" +
                "  \"sslTrustPassword\" : \"changeit\",\n" +
                "  \"basicAuth\" : {\n" +
                "    \"username\" : \"admin\",\n" +
                "    \"password\" : \"cds\"\n" +
                "  }\n" +
                "}"
        val mapper = ObjectMapper()
        val actualObj: JsonNode = mapper.readTree(json)
        val properties = bluePrintRestLibPropertyService.restClientProperties(
                actualObj)
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
    fun testSSLTokenPropertiesAsJson() {
        val json: String = "{\n" +
                "  \"type\" : \"ssl-token-auth\",\n" +
                "  \"url\" : \"https://localhost:8443\",\n" +
                "  \"keyStoreInstance\" : \"PKCS12\",\n" +
                "  \"sslTrust\" : \"src/test/resources/keystore.p12\",\n" +
                "  \"sslTrustPassword\" : \"changeit\",\n" +
                "  \"tokenAuth\" : {\n" +
                "    \"token\" : \"72178473kjshdkjgvbsdkjv903274908\"\n" +
                "  }\n" +
                "}"
        val mapper = ObjectMapper()
        val actualObj: JsonNode = mapper.readTree(json)
        val properties = bluePrintRestLibPropertyService.restClientProperties(
                actualObj)
        assertNotNull(properties, "failed to create property bean")

        val p: SSLTokenAuthRestClientProperties =
                properties as SSLTokenAuthRestClientProperties

        assertEquals(p.tokenAuth!!.token!!, "72178473kjshdkjgvbsdkjv903274908")
        assertEquals(p.sslTrust, "src/test/resources/keystore.p12")
        assertEquals(p.sslTrustPassword, "changeit")
        assertEquals(p.keyStoreInstance, "PKCS12")
    }

    @Test
    fun testSSLNoAuthPropertiesAsJson() {
        val json: String = "{\n" +
                "  \"type\" : \"ssl-basic-auth\",\n" +
                "  \"url\" : \"https://localhost:8443\",\n" +
                "  \"keyStoreInstance\" : \"PKCS12\",\n" +
                "  \"sslTrust\" : \"src/test/resources/keystore.p12\",\n" +
                "  \"sslTrustPassword\" : \"changeit\",\n" +
                "  \"sslKey\" : \"src/test/resources/keystore.p12\",\n" +
                "  \"sslKeyPassword\" : \"changeit\"\n" +
                "}"
        val mapper = ObjectMapper()
        val actualObj: JsonNode = mapper.readTree(json)
        val properties = bluePrintRestLibPropertyService.restClientProperties(
                actualObj)
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
    fun testBlueprintWebClientService() {
        val blueprintWebClientService = bluePrintRestLibPropertyService
                .blueprintWebClientService("sample")
        assertNotNull(blueprintWebClientService, "failed to create blu" +
                "eprintWebClientService")
    }

}

