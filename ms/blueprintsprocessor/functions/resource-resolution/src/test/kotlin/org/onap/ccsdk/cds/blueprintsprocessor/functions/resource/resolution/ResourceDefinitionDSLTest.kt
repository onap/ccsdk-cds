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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ResourceDefinitionDSLTest {

    @Test
    fun testResourceDefinitionDSL() {
        val testResourceDefinition = BlueprintTypes.resourceDefinition(
            "service-instance-id",
            "VFW Service Instance Name"
        ) {
            tags("service-instance-name, vfw, resources")
            updatedBy("brindasanth@onap.com")
            property("string", true)
            sources {
                sourceInput("input", "") {}
                sourceDefault("default", "") {}
                sourceDb("sdnctl", "") {
                    definedProperties {
                        type("SQL")
                        query("SELECT name FROM SERVICE_INSTANCE WHERE id = \$id")
                        endpointSelector("db-source-endpoint")
                        inputKeyMapping {
                            map("id", "\$service-instance-id")
                        }
                        outputKeyMapping {
                            map("service-instance-name", "\$name")
                        }
                        keyDependencies(arrayListOf("service-instance-id"))
                    }
                }
                sourceRest("odl-mdsal", "") {
                    definedProperties {
                        type("JSON")
                        endpointSelector("rest-source-endpoint")
                        expressionType("JSON_PATH")
                        urlPath("/service-instance/\$id")
                        path(".\$name")
                        verb("GET")
                        payload("sample payload")
                        inputKeyMapping {
                            map("id", "\$service-instance-id")
                        }
                        outputKeyMapping {
                            map("service-instance-name", "\$name")
                        }
                        keyDependencies(arrayListOf("service-instance-id"))
                    }
                }
                sourceCapability("custom-component", "") {
                    definedProperties {
                        type("kotlin")
                        scriptClassReference("Scripts/ServiceInstance.kt")
                        keyDependencies(arrayListOf("service-instance-id"))
                    }
                }
            }
        }
        // println(resourceDefinition.asJsonString(true))
        assertNotNull(testResourceDefinition, "failed to generate testResourceDefinition")

        val testResourceDefinitions = BlueprintTypes.resourceDefinitions {
            resourceDefinition(testResourceDefinition)
        }
        assertNotNull(testResourceDefinitions, "failed to generate testResourceDefinitions")
        assertEquals(1, testResourceDefinitions.size, "testResourceDefinitions size doesn't match")
    }

    @Test
    fun testResourceAssignment() {
        val testResourceAssignment = BlueprintTypes.resourceAssignment(
            "instance-name",
            "service-instance-name", "odl-mdsal"
        ) {
            inputParameter(true)
            property("string", true)
            dependencies(arrayListOf("service-instance-id"))
        }
        // println(resourceAssignment.asJsonString(true))
        assertNotNull(testResourceAssignment, "failed to generate resourceAssignment")

        val testResourceAssignments = BlueprintTypes.resourceAssignments {
            resourceAssignment(testResourceAssignment)
            resourceAssignment(
                "instance-name1",
                "service-instance-name", "odl-mdsal"
            ) {
                inputParameter(true)
                property("string", true)
                dependencies(arrayListOf("service-instance-id"))
            }
        }
        // println(testResourceAssignments.asJsonString(true))
        assertNotNull(testResourceAssignments, "failed to generate testResourceAssignments")
        assertEquals(2, testResourceAssignments.size, "testResourceAssignments size doesn't match")
    }
}
