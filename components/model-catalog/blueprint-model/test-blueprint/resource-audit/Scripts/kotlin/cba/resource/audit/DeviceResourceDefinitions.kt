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

package cba.resource.audit

import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.resourceDefinitions
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes

const val SOURCE_SDNO = "SDN-O"
const val SOURCE_SDNC = "SDNC"
const val SOURCE_AAI = "AAI"

val deviceResourceDefinitions = BlueprintTypes.resourceDefinitions {
    // Port Speed Definitions
    resourceDefinition(name = "port-speed", description = "Port Speed") {
        property(type = "string", required = true)
        sources {
            sourceCapability(id = SOURCE_SDNO, description = "SDN-O Source") {
                definedProperties {
                    type(BlueprintConstants.SCRIPT_KOTLIN)
                    scriptClassReference("cba.resource.audit.processor.PortSpeedRAProcessor")
                    keyDependencies(arrayListOf("device-id"))
                }
            }
            sourceDb(id = SOURCE_SDNC, description = "SDNC Controller") {
                definedProperties {
                    endpointSelector("processor-db")
                    query("SELECT PORT_SPEED FROM XXXX WHERE DEVICE_ID = :device_id")
                    inputKeyMapping {
                        map("device_id", "\$device-id")
                    }
                    keyDependencies(arrayListOf("device-id"))
                }
            }
            sourceRest(id = SOURCE_AAI, description = "AAI Source") {
                definedProperties {
                    endpointSelector("aai")
                    urlPath("/vnf/\$device_id")
                    path(".\$port-speed")
                    inputKeyMapping {
                        map("device_id", "\$device-id")
                    }
                    keyDependencies(arrayListOf("device-id"))
                }
            }
        }
    }
}
