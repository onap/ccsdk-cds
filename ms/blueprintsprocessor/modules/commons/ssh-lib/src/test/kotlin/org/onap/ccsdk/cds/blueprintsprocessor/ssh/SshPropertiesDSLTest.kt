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

package org.onap.ccsdk.cds.blueprintsprocessor.ssh

import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.relationshipTypeConnectsTo
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.serviceTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SshPropertiesDSLTest {

    @Test
    fun testSshPropertiesDSL() {
        val serviceTemplate = serviceTemplate("ssh-properties-test", "1.0.0", "xxx.@xx.com", "ssh") {
            topologyTemplate {
                relationshipTemplateSshClient("sample-basic-auth", "SSH Connection") {
                    basicAuth {
                        username("sample-user")
                        password("sample-password")
                        host("sample-host")
                        connectionTimeOut(30)
                    }
                }
            }
            relationshipTypeConnectsToSshClient()
            relationshipTypeConnectsTo()
        }

        // println(serviceTemplate.asJsonString(true))
        assertNotNull(serviceTemplate, "failed to create service template")
        val relationshipTemplates = serviceTemplate.topologyTemplate?.relationshipTemplates
        assertNotNull(relationshipTemplates, "failed to get relationship templates")
        assertEquals(1, relationshipTemplates.size, "relationshipTemplates doesn't match")
        assertNotNull(relationshipTemplates["sample-basic-auth"], "failed to get sample-basic-auth")

        val relationshipTypes = serviceTemplate.relationshipTypes
        assertNotNull(relationshipTypes, "failed to get relationship types")
        assertEquals(2, relationshipTypes.size, "relationshipTypes doesn't match")
        assertNotNull(
            relationshipTypes[BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO],
            "failed to get ${BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO}"
        )
        assertNotNull(
            relationshipTypes[BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_SSH_CLIENT],
            "failed to get ${BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_SSH_CLIENT}"
        )
    }
}
