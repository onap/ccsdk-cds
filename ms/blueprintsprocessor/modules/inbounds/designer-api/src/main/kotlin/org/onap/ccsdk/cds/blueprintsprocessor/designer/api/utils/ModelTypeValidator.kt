/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.utils

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ModelType
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.cds.controllerblueprints.core.data.CapabilityDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipType
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils

class ModelTypeValidator {
    companion object {

        /**
         * This is a validateModelTypeDefinition
         *
         * @param definitionType definitionType
         * @param definitionContent definitionContent
         * @return boolean
         */
        fun validateModelTypeDefinition(definitionType: String, definitionContent: JsonNode): Boolean {

            when (definitionType) {
                BlueprintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE -> {
                    JacksonUtils.readValue(definitionContent, DataType::class.java)
                        ?: throw BlueprintException("Model type definition is not DataType valid content $definitionContent")
                }
                BlueprintConstants.MODEL_DEFINITION_TYPE_NODE_TYPE -> {
                    JacksonUtils.readValue(definitionContent, NodeType::class.java)
                        ?: throw BlueprintException("Model type definition is not NodeType valid content $definitionContent")
                }
                BlueprintConstants.MODEL_DEFINITION_TYPE_ARTIFACT_TYPE -> {
                    JacksonUtils.readValue(definitionContent, ArtifactType::class.java)
                        ?: throw BlueprintException("Model type definition is not ArtifactType valid content $definitionContent")
                }
                BlueprintConstants.MODEL_DEFINITION_TYPE_CAPABILITY_TYPE -> {
                    JacksonUtils.readValue(definitionContent, CapabilityDefinition::class.java)
                        ?: throw BlueprintException("Model type definition is not CapabilityDefinition valid content $definitionContent")
                }
                BlueprintConstants.MODEL_DEFINITION_TYPE_RELATIONSHIP_TYPE -> {
                    JacksonUtils.readValue(definitionContent, RelationshipType::class.java)
                        ?: throw BlueprintException("Model type definition is not RelationshipType valid content $definitionContent")
                }
            }
            return true
        }

        /**
         * This is a validateModelType method
         *
         * @param modelType modelType
         * @return boolean
         */
        fun validateModelType(modelType: ModelType?): Boolean {
            checkNotNull(modelType) { "Model Type Information is missing." }

            val validRootTypes = BlueprintTypes.validModelTypes()

            check(validRootTypes.contains(modelType.definitionType)) {
                "Not Valid Model Root Type(${modelType.definitionType}), It should be $validRootTypes"
            }

            validateModelTypeDefinition(modelType.definitionType, modelType.definition)
            return true
        }
    }
}
