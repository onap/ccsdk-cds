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

package org.onap.ccsdk.cds.blueprintsprocessor.nats

import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.getInput
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.relationshipTypeConnectsTo
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.serviceTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NatsPropertiesDSLTest {

    @Test
    fun testNatsPropertiesDSL() {
        val serviceTemplate = serviceTemplate("nats-dsl", "1.0.0", "xx@xx.com", "nats") {
            topologyTemplate {
                relationshipTemplateNats("sample-token-auth", "Nats TokenAuth endpoint") {
                    tokenAuth {
                        host("nats://localhost:4222")
                        token("tokenAuth")
                        monitoringSelector(getInput("monitoringUrl"))
                    }
                }
                relationshipTemplateNats("sample-tls-auth", "Nats TLS endpoint.") {
                    tlsAuth {
                        host("nats://localhost:4222")
                    }
                }
            }

            relationshipTypes(
                arrayListOf(
                    BluePrintTypes.relationshipTypeConnectsToNats(),
                    BluePrintTypes.relationshipTypeConnectsTo()
                )
            )
        }

        assertNotNull(serviceTemplate, "failed to create service template")
        val relationshipTemplates = serviceTemplate.topologyTemplate?.relationshipTemplates
        assertNotNull(relationshipTemplates, "failed to get relationship templates")
        assertEquals(2, relationshipTemplates.size, "relationshipTemplates doesn't match")
        assertNotNull(relationshipTemplates["sample-token-auth"], "failed to get sample-token-auth")
        assertNotNull(relationshipTemplates["sample-tls-auth"], "failed to get sample-tls-auth")
        // println(serviceTemplate.asJsonString(true))
    }
}
