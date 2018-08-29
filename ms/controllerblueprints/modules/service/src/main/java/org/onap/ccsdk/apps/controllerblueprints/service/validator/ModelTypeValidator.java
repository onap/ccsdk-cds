/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.apps.controllerblueprints.service.validator;

import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.data.ArtifactType;
import org.onap.ccsdk.apps.controllerblueprints.core.data.CapabilityDefinition;
import org.onap.ccsdk.apps.controllerblueprints.core.data.DataType;
import org.onap.ccsdk.apps.controllerblueprints.core.data.NodeType;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType;

import java.util.ArrayList;
import java.util.List;

/**
 * ModelTypeValidation.java Purpose: Provide Validation Service for Model Type ModelTypeValidation
 *
 * @author Brinda Santh
 * @version 1.0
 */

public class ModelTypeValidator {

    private ModelTypeValidator() {

    }

    private static List<String> getValidModelDefinitionType() {
        List<String> validTypes = new ArrayList<>();
        validTypes.add(BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE);
        validTypes.add(BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TYPE);
        validTypes.add(BluePrintConstants.MODEL_DEFINITION_TYPE_ARTIFACT_TYPE);
        validTypes.add(BluePrintConstants.MODEL_DEFINITION_TYPE_CAPABILITY_TYPE);
        validTypes.add(BluePrintConstants.MODEL_DEFINITION_TYPE_RELATIONSHIP_TYPE);
        return validTypes;
    }

    /**
     * This is a validateModelTypeDefinition
     * 
     * @param definitionType
     * @param definitionContent
     * @return boolean
     * @throws BluePrintException
     */
    public static boolean validateModelTypeDefinition(String definitionType, String definitionContent)
            throws BluePrintException {
        if (StringUtils.isNotBlank(definitionContent)) {
            if (BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE.equalsIgnoreCase(definitionType)) {
                DataType dataType = JacksonUtils.readValue(definitionContent, DataType.class);
                if (dataType == null) {
                    throw new BluePrintException(
                            "Model type definition is not DataType valid content " + definitionContent);
                }
            } else if (BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TYPE.equalsIgnoreCase(definitionType)) {
                NodeType nodeType = JacksonUtils.readValue(definitionContent, NodeType.class);
                if (nodeType == null) {
                    throw new BluePrintException(
                            "Model type definition is not NodeType valid content " + definitionContent);
                }
            } else if (BluePrintConstants.MODEL_DEFINITION_TYPE_ARTIFACT_TYPE.equalsIgnoreCase(definitionType)) {
                ArtifactType artifactType = JacksonUtils.readValue(definitionContent, ArtifactType.class);
                if (artifactType == null) {
                    throw new BluePrintException(
                            "Model type definition is not ArtifactType valid content " + definitionContent);
                }
            }else if (BluePrintConstants.MODEL_DEFINITION_TYPE_CAPABILITY_TYPE.equalsIgnoreCase(definitionType)) {
                CapabilityDefinition capabilityDefinition =
                        JacksonUtils.readValue(definitionContent, CapabilityDefinition.class);
                if (capabilityDefinition == null) {
                    throw new BluePrintException(
                            "Model type definition is not CapabilityDefinition valid content " + definitionContent);
                }
            }

        }
        return true;
    }

    /**
     * This is a validateModelType method
     * 
     * @param modelType
     * @return boolean
     */
    public static boolean validateModelType(ModelType modelType) throws BluePrintException {
        if (modelType != null) {

            if (StringUtils.isBlank(modelType.getModelName())) {
                throw new BluePrintException("Model Name Information is missing.");
            }

            if (StringUtils.isBlank(modelType.getDefinitionType())) {
                throw new BluePrintException("Model Root Type Information is missing.");
            }
            if (StringUtils.isBlank(modelType.getDerivedFrom())) {
                throw new BluePrintException("Model Type Information is missing.");
            }

            if (StringUtils.isBlank(modelType.getDefinition())) {
                throw new BluePrintException("Model Definition Information is missing.");
            }
            if (StringUtils.isBlank(modelType.getDescription())) {
                throw new BluePrintException("Model Description Information is missing.");
            }

            if (StringUtils.isBlank(modelType.getVersion())) {
                throw new BluePrintException("Model Version Information is missing.");
            }

            if (StringUtils.isBlank(modelType.getUpdatedBy())) {
                throw new BluePrintException("Model Updated By Information is missing.");
            }

            List<String> validRootTypes = getValidModelDefinitionType();
            if (!validRootTypes.contains(modelType.getDefinitionType())) {
                throw new BluePrintException("Not Valid Model Root Type(" + modelType.getDefinitionType()
                        + "), It sould be " + validRootTypes);
            }

            validateModelTypeDefinition(modelType.getDefinitionType(), modelType.getDefinition());

        } else {
            throw new BluePrintException("Model Type Information is missing.");
        }

        return true;

    }

}
