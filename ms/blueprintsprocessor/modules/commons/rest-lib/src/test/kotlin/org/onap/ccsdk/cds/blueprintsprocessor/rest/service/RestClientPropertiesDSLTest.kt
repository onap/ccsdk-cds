/*
 *  Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.rest.service

import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.rest.relationshipTemplateRestClient
import org.onap.ccsdk.cds.blueprintsprocessor.rest.relationshipTypeConnectsToRestClient
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.relationshipTypeConnectsTo
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.serviceTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RestClientPropertiesDSLTest {

    @Test
    fun testRestClientProperties() {

        val serviceTemplate = serviceTemplate("rest-properties-test", "1.0.0", "xxx.@xx.com", "rest") {
            topologyTemplate {
                relationshipTemplateRestClient("sample-basic-auth", "") {
                    basicAuth {
                        url("http://localhost:8080")
                        username("xxxxx")
                        password("******")
                    }
                }
                relationshipTemplateRestClient("sample-token-auth", "") {
                    tokenAuth {
                        url("http://localhost:8080")
                        token("sdfgfsadgsgf")
                    }
                }
                relationshipTemplateRestClient("sample-ssl-auth", "") {
                    sslAuth {
                        url("http://localhost:8080")
                        keyStoreInstance("instance")
                        sslTrust("sample-trust")
                        sslTrustPassword("sample-trust-password")
                        sslKey("sample-sslkey")
                        sslKeyPassword("sample-key-password")
                    }
                }
            }
            relationshipTypeConnectsToRestClient()
            relationshipTypeConnectsTo()
        }

        // println(serviceTemplate.asJsonString(true))
        assertNotNull(serviceTemplate, "failed to create service template")
        val relationshipTemplates = serviceTemplate.topologyTemplate?.relationshipTemplates
        assertNotNull(relationshipTemplates, "failed to get relationship templates")
        assertEquals(3, relationshipTemplates.size, "relationshipTemplates doesn't match")
        assertNotNull(relationshipTemplates["sample-basic-auth"], "failed to get sample-basic-auth")
        assertNotNull(relationshipTemplates["sample-token-auth"], "failed to get sample-token-auth")
        assertNotNull(relationshipTemplates["sample-ssl-auth"], "failed to get sample-ssl-auth")

        val relationshipTypes = serviceTemplate.relationshipTypes
        assertNotNull(relationshipTypes, "failed to get relationship types")
        assertEquals(2, relationshipTypes.size, "relationshipTypes doesn't match")
        assertNotNull(
            relationshipTypes[BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO],
            "failed to get ${BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO}"
        )
        assertNotNull(
            relationshipTypes[BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_REST_CLIENT],
            "failed to get ${BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_REST_CLIENT}"
        )
    }
}
