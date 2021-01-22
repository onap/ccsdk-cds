/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.grpc

import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.relationshipTypeConnectsTo
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.serviceTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GrpcPropertiesDSLTest {

    @Test
    fun testGrpcServerPropertiesDSL() {
        val serviceTemplate = serviceTemplate("grpc-properties-test", "1.0.0", "xxx.@xx.com", "grpc") {
            topologyTemplate {
                relationshipTemplateGrpcServer("sample-tls-auth", "Grpc Server") {
                    tlsAuth {
                        port(40002)
                        certChain("sample-cert-chains")
                        privateKey("sample-private-key")
                        trustCertCollection("sample-trust-cert-collection")
                    }
                }
                relationshipTemplateGrpcServer("sample-token-auth", "Grpc Server") {
                    tokenAuth {
                        port(40002)
                        token("sample-token")
                    }
                }
            }
            relationshipTypeConnectsToGrpcServer()
            relationshipTypeConnectsTo()
        }

        // println(serviceTemplate.asJsonString(true))
        assertNotNull(serviceTemplate, "failed to create service template")
        val relationshipTemplates = serviceTemplate.topologyTemplate?.relationshipTemplates
        assertNotNull(relationshipTemplates, "failed to get relationship templates")
        assertEquals(2, relationshipTemplates.size, "relationshipTemplates doesn't match")
        assertNotNull(relationshipTemplates["sample-tls-auth"], "failed to get sample-tls-auth")
        assertNotNull(relationshipTemplates["sample-token-auth"], "failed to get sample-token-auth")

        val relationshipTypes = serviceTemplate.relationshipTypes
        assertNotNull(relationshipTypes, "failed to get relationship types")
        assertEquals(2, relationshipTypes.size, "relationshipTypes doesn't match")
        assertNotNull(
            relationshipTypes[BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO],
            "failed to get ${BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO}"
        )
        assertNotNull(
            relationshipTypes[BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_GRPC_SERVER],
            "failed to get ${BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_GRPC_SERVER}"
        )
    }

    @Test
    fun testGrpcClientPropertiesDSL() {
        val serviceTemplate = serviceTemplate("grpc-properties-test", "1.0.0", "xxx.@xx.com", "grpc") {
            topologyTemplate {
                relationshipTemplateGrpcClient("sample-tls-auth", "Grpc Server") {
                    tlsAuth {
                        host("localhost")
                        port(40002)
                        clientCertChain("sample-certchains")
                        clientPrivateKey("sample-private-key")
                        trustCertCollection("sample-trust-cert-collection")
                    }
                }
                relationshipTemplateGrpcClient("sample-basic-auth", "Grpc Server") {
                    basicAuth {
                        host("localhost")
                        port(40002)
                        username("sample-user")
                        password("credential")
                    }
                }
                relationshipTemplateGrpcClient("sample-token-auth", "Grpc Server") {
                    tokenAuth {
                        host("localhost")
                        port(40002)
                        token("sample-token")
                    }
                }
            }
            relationshipTypeConnectsToGrpcClient()
            relationshipTypeConnectsTo()
        }

        // println(serviceTemplate.asJsonString(true))
        assertNotNull(serviceTemplate, "failed to create service template")
        val relationshipTemplates = serviceTemplate.topologyTemplate?.relationshipTemplates
        assertNotNull(relationshipTemplates, "failed to get relationship templates")
        assertEquals(3, relationshipTemplates.size, "relationshipTemplates doesn't match")
        assertNotNull(relationshipTemplates["sample-tls-auth"], "failed to get sample-tls-auth")
        assertNotNull(relationshipTemplates["sample-basic-auth"], "failed to get sample-basic-auth")
        assertNotNull(relationshipTemplates["sample-token-auth"], "failed to get sample-token-auth")

        val relationshipTypes = serviceTemplate.relationshipTypes
        assertNotNull(relationshipTypes, "failed to get relationship types")
        assertEquals(2, relationshipTypes.size, "relationshipTypes doesn't match")
        assertNotNull(
            relationshipTypes[BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO],
            "failed to get ${BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO}"
        )
        assertNotNull(
            relationshipTypes[BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_GRPC_CLIENT],
            "failed to get ${BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_GRPC_CLIENT}"
        )
    }
}
