/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2018-2019 AT&T Intellectual Property.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.grpc.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.BasicAuthGrpcClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.BlueprintGrpcLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TLSAuthGrpcClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TLSAuthGrpcServerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TokenAuthGrpcClientProperties
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [
        BlueprintGrpcLibConfiguration::class,
        BlueprintPropertyConfiguration::class, BlueprintPropertiesService::class
    ]
)
@TestPropertySource(
    properties =
        [
            "blueprintsprocessor.grpcclient.sample.type=basic-auth",
            "blueprintsprocessor.grpcclient.sample.host=127.0.0.1",
            "blueprintsprocessor.grpcclient.sample.port=50505",
            "blueprintsprocessor.grpcclient.sample.username=sampleuser",
            "blueprintsprocessor.grpcclient.sample.password=sampleuser",

            "blueprintsprocessor.grpcclient.token.type=token-auth",
            "blueprintsprocessor.grpcclient.token.host=127.0.0.1",
            "blueprintsprocessor.grpcclient.token.port=50505",
            "blueprintsprocessor.grpcclient.token.username=sampleuser",
            "blueprintsprocessor.grpcclient.token.password=sampleuser",

            "blueprintsprocessor.grpcserver.tls-sample.type=tls-auth",
            "blueprintsprocessor.grpcserver.tls-sample.port=50505",
            "blueprintsprocessor.grpcserver.tls-sample.certChain=server1.pem",
            "blueprintsprocessor.grpcserver.tls-sample.privateKey=server1.key",
            "blueprintsprocessor.grpcserver.tls-sample.trustCertCollection=ca.pem",

            "blueprintsprocessor.grpcclient.tls-sample.type=tls-auth",
            "blueprintsprocessor.grpcclient.tls-sample.host=127.0.0.1",
            "blueprintsprocessor.grpcclient.tls-sample.port=50505",
            "blueprintsprocessor.grpcclient.tls-sample.trustCertCollection=ca.pem",
            "blueprintsprocessor.grpcclient.tls-sample.clientCertChain=client.pem",
            "blueprintsprocessor.grpcclient.tls-sample.clientPrivateKey=client.key"
        ]
)
class BlueprintGrpcLibPropertyServiceTest {

    @Autowired
    lateinit var bluePrintGrpcLibPropertyService: BlueprintGrpcLibPropertyService

    /**
     * Tests the GRPC client properties with selector for basic auth.
     */
    @Test
    fun testGrpcClientProperties() {
        val properties = bluePrintGrpcLibPropertyService.grpcClientProperties(
            "blueprintsprocessor.grpcclient.sample"
        )
            as BasicAuthGrpcClientProperties
        assertNotNull(properties, "failed to create property bean")
        assertNotNull(
            properties.host, "failed to get host property in property bean"
        )
        assertNotNull(
            properties.port, "failed to get host property in property bean"
        )
        assertNotNull(
            properties.username, "failed to get host property in property bean"
        )
        assertNotNull(
            properties.password, "failed to get host property in property bean"
        )
    }

    /**
     * Tests the GRPC client properties with JSON node for token auth.
     */
    @Test
    fun testGrpcClientPropertiesWithJson() {
        val json: String = "{\n" +
            "  \"type\" : \"token-auth\",\n" +
            "  \"host\" : \"127.0.0.1\",\n" +
            "  \"port\" : \"50505\"\n" +
            "}"
        val mapper = ObjectMapper()
        val actualObj: JsonNode = mapper.readTree(json)
        val properties = bluePrintGrpcLibPropertyService.grpcClientProperties(
            actualObj
        ) as TokenAuthGrpcClientProperties
        assertNotNull(properties, "failed to create property bean")
        assertEquals(properties.host, "127.0.0.1")
        assertNotNull(properties.port, "50505")
    }

    /**
     * Tests the GRPC client service with selector for basic auth.
     */
    @Test
    fun testGrpcClientServiceBasic() {
        val svc = bluePrintGrpcLibPropertyService.blueprintGrpcClientService(
            "sample"
        )
        assertTrue(svc is BasicAuthGrpcClientService)
    }

    /**
     * Tests the GRPC client service with selector for token auth.
     */
    @Test
    fun testGrpcClientServiceToken() {
        val svc = bluePrintGrpcLibPropertyService.blueprintGrpcClientService(
            "token"
        )
        assertTrue(svc is TokenAuthGrpcClientService)
    }

    /**
     * Tests the GRPC client service with JSON node for basic auth.
     */
    @Test
    fun testGrpcClientServiceWithJson() {
        val json: String = "{\n" +
            "  \"type\" : \"basic-auth\",\n" +
            "  \"host\" : \"127.0.0.1\",\n" +
            "  \"port\" : \"50505\",\n" +
            "  \"username\" : \"sampleuser\",\n" +
            "  \"password\" : \"samplepwd\"\n" +
            "}"
        val mapper = ObjectMapper()
        val actualObj: JsonNode = mapper.readTree(json)
        val svc = bluePrintGrpcLibPropertyService
            .blueprintGrpcClientService(actualObj)
        assertTrue(svc is BasicAuthGrpcClientService)
    }

    @Test
    fun testGrpcClientTLSProperties() {
        val properties = bluePrintGrpcLibPropertyService
            .grpcClientProperties("blueprintsprocessor.grpcclient.tls-sample") as TLSAuthGrpcClientProperties
        assertNotNull(properties, "failed to create property bean")
        assertNotNull(properties.host, "failed to get host property in property bean")
        assertNotNull(properties.port, "failed to get host property in property bean")
        assertNotNull(properties.trustCertCollection, "failed to get trustCertCollection property in property bean")
        assertNotNull(properties.clientCertChain, "failed to get clientCertChain property in property bean")
        assertNotNull(properties.clientPrivateKey, "failed to get clientPrivateKey property in property bean")

        val configDsl = """{
            "type" : "tls-auth",
            "host" : "localhost",
            "port" : "50505",
            "trustCertCollection" : "server1.pem",
            "clientCertChain" : "server1.key",
            "clientPrivateKey" : "ca.pem"
            }           
        """.trimIndent()
        val jsonProperties = bluePrintGrpcLibPropertyService
            .grpcClientProperties(configDsl.jsonAsJsonType()) as TLSAuthGrpcClientProperties
        assertNotNull(jsonProperties, "failed to create property bean from json")
    }

    @Test
    fun testGrpcServerTLSProperties() {
        val properties = bluePrintGrpcLibPropertyService
            .grpcServerProperties("blueprintsprocessor.grpcserver.tls-sample") as TLSAuthGrpcServerProperties
        assertNotNull(properties, "failed to create property bean")
        assertNotNull(properties.port, "failed to get host property in property bean")
        assertNotNull(properties.trustCertCollection, "failed to get trustCertCollection property in property bean")
        assertNotNull(properties.certChain, "failed to get certChain property in property bean")
        assertNotNull(properties.privateKey, "failed to get privateKey property in property bean")

        val configDsl = """{
            "type" : "tls-auth",
            "port" : "50505",
            "certChain" : "server1.pem",
            "privateKey" : "server1.key",
            "trustCertCollection" : "ca.pem"
            }           
        """.trimIndent()
        val jsonProperties = bluePrintGrpcLibPropertyService
            .grpcServerProperties(configDsl.jsonAsJsonType()) as TLSAuthGrpcServerProperties
        assertNotNull(jsonProperties, "failed to create property bean from json")

        val grpcServerService = bluePrintGrpcLibPropertyService.blueprintGrpcServerService("tls-sample")
        assertNotNull(grpcServerService, "failed to get grpc server service")
        Assert.assertEquals(TLSAuthGrpcServerService::class.java, grpcServerService.javaClass)
    }
}
