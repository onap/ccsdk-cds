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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import kotlin.test.Test
import kotlin.test.assertNotNull

class ResourceSourceDSLTest {

    @Test
    fun testNodeTypeSourceInput() {
        val nodeType = BlueprintTypes.nodeTypeSourceInput()
        // println(nodeType.asJsonString(true))
        assertNotNull(nodeType, "failed to generate nodeTypeSourceInput")
    }

    @Test
    fun testNodeTypeSourceDefault() {
        val nodeType = BlueprintTypes.nodeTypeSourceDefault()
        // println(nodeType.asJsonString(true))
        assertNotNull(nodeType, "failed to generate nodeTypeSourceDefault")
    }

    @Test
    fun testNodeTypeSourceDb() {
        val nodeType = BlueprintTypes.nodeTypeSourceDb()
        // println(nodeType.asJsonString(true))
        assertNotNull(nodeType, "failed to generate nodeTypeSourceDb")
    }

    @Test
    fun testNodeTypeSourceRest() {
        val nodeType = BlueprintTypes.nodeTypeSourceRest()
        // println(nodeType.asJsonString(true))
        assertNotNull(nodeType, "failed to generate nodeTypeSourceRest")
    }

    @Test
    fun testNodeTypeSourceCapability() {
        val nodeType = BlueprintTypes.nodeTypeSourceCapability()
        // println(nodeType.asJsonString(true))
        assertNotNull(nodeType, "failed to generate nodeTypeSourceCapability")
    }

    @Test
    fun testNodeTemplateSourceInput() {
        val nodeTemplate = BlueprintTypes.nodeTemplateSourceInput("InputSystem", "") {
        }
        // println(nodeTemplate.asJsonString(true))
        assertNotNull(nodeTemplate, "failed to generate nodeTemplateSourceInput")
    }

    @Test
    fun testNodeTemplateSourceDefault() {
        val nodeTemplate = BlueprintTypes.nodeTemplateSourceDefault("DefaultSystem", "") {
        }
        // println(nodeTemplate.asJsonString(true))
        assertNotNull(nodeTemplate, "failed to generate nodeTemplateSourceDefault")
    }

    @Test
    fun testNodeTemplateSourceDb() {
        val nodeTemplate = BlueprintTypes.nodeTemplateSourceDb("DbSystem", "") {
            definedProperties {
                type("SQL")
                query("SELECT * FROM DB WHERE name = \$name")
                endpointSelector("db-source-endpoint")
                inputKeyMapping {
                    map("name", "\$name")
                }
                outputKeyMapping {
                    map("field_name", "\$fieldValue")
                }
                keyDependencies(arrayListOf("name"))
            }
        }
        // println(nodeTemplate.asJsonString(true))
        assertNotNull(nodeTemplate, "failed to generate nodeTemplateSourceDb")
    }

    @Test
    fun testNodeTemplateSourceRest() {
        val nodeTemplate = BlueprintTypes.nodeTemplateSourceRest("restSystem", "") {
            definedProperties {
                type("JSON")
                endpointSelector("rest-source-endpoint")
                expressionType("JSON_PATH")
                urlPath("/location")
                path(".\$name")
                verb("GET")
                payload("sample payload")
                inputKeyMapping {
                    map("name", "\$name")
                }
                outputKeyMapping {
                    map("field_name", "\$fieldValue")
                }
                keyDependencies(arrayListOf("name"))
            }
        }
        // println(nodeTemplate.asJsonString(true))
        assertNotNull(nodeTemplate, "failed to generate nodeTemplateSourceRest")
    }

    @Test
    fun testNodeTemplateSourceCapability() {
        val nodeTemplate = BlueprintTypes.nodeTemplateSourceCapability("capabiltySystem", "") {
            definedProperties {
                type("kotlin")
                scriptClassReference("Scripts/Sample.kt")
                keyDependencies(arrayListOf("name"))
            }
        }
        // println(nodeTemplate.asJsonString(true))
        assertNotNull(nodeTemplate, "failed to generate nodeTemplateSourceCapability")
    }
}
