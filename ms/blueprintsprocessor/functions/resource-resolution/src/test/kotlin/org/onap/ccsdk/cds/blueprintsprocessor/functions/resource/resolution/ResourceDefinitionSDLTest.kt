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

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import kotlin.test.Test
import kotlin.test.assertNotNull

class ResourceDefinitionSDLTest {

    @Test
    fun testResourceDefinitionDSL() {
        val resourceDefinition = BluePrintTypes.resourceDefinition("service-instance-id") {
            tags("service-instance-name, vfw, resources")
            updatedBy("brindasanth@onap.com")
            property("service-instance-name", "string", true, "VFW Service Instance Name")
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
        //println(resourceDefinition.asJsonString(true))
        assertNotNull(resourceDefinition, "failed to generate resourceDefinition")
    }
}