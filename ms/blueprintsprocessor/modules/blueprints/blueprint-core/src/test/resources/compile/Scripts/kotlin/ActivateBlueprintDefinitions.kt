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

package cba.scripts

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.dataType
import org.onap.ccsdk.cds.controllerblueprints.core.dsl.serviceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.AbstractBlueprintDefinitions

class ActivateBlueprintDefinitions : AbstractBlueprintDefinitions() {

    override fun serviceTemplate(): ServiceTemplate {

        return serviceTemplate(
            "sample-blue-print", "1.0.0",
            "brindasanth@onap.com", "sample, blueprints"
        ) {
            topologyTemplate {
                workflowNodeTemplate("activate", "component-resource-resolution", "") {
                    operation("ResourceResolutionExecutor", "") {
                        inputs {
                            property("string-value", "sample")
                        }
                    }
                }
            }
        }
    }

    override fun loadOtherDefinitions() {
        /** Sample Definitions */
        val customDataType = dataType(
            "custom-datatype", "1.0.0",
            BlueprintConstants.MODEL_TYPE_DATATYPES_ROOT, ""
        ) {
            property("name", BlueprintConstants.DATA_TYPE_STRING, true, "")
            property("value", BlueprintConstants.DATA_TYPE_STRING, true, "")
        }
        /** Loading to definitions */
        addOtherDefinition("datatype-custom-datatype", customDataType)
    }
}
