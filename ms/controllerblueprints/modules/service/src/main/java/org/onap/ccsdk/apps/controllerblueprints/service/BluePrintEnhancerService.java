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

package org.onap.ccsdk.apps.controllerblueprints.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.ConfigModelConstant;
import org.onap.ccsdk.apps.controllerblueprints.core.data.*;
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintEnhancerDefaultService;
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRepoService;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BluePrintEnhancerService
 *
 * @author Brinda Santh DATE : 8/8/2018
 */

@Service
public class BluePrintEnhancerService extends BluePrintEnhancerDefaultService {

    private static EELFLogger log = EELFManager.getInstance().getLogger(BluePrintEnhancerService.class);

    private Map<String, DataType> recipeDataTypes = new HashMap<>();

    public BluePrintEnhancerService(BluePrintRepoService bluePrintEnhancerRepoDBService) {
        super(bluePrintEnhancerRepoDBService);
    }

    @Override
    public void enrichTopologyTemplate(@NotNull ServiceTemplate serviceTemplate) throws BluePrintException{
        super.enrichTopologyTemplate(serviceTemplate);

        // Update the Recipe Inputs and DataTypes
        populateRecipeInputs(serviceTemplate);
    }


    @Override
    public void enrichNodeTemplate(@NotNull String nodeTemplateName, @NotNull NodeTemplate nodeTemplate) throws BluePrintException {
        super.enrichNodeTemplate(nodeTemplateName, nodeTemplate);

        String nodeTypeName = nodeTemplate.getType();
        log.info("*** Enriching NodeType: {}", nodeTypeName);
        // Get NodeType from Repo and Update Service Template
        NodeType nodeType = super.populateNodeType(nodeTypeName);

        // Enrich NodeType
        super.enrichNodeType(nodeTypeName, nodeType);

        // Custom for Artifact Population
        if (StringUtils.isNotBlank(nodeType.getDerivedFrom())
                && ConfigModelConstant.MODEL_TYPE_NODE_ARTIFACT.equalsIgnoreCase(nodeType.getDerivedFrom())) {
            populateArtifactTemplateMappingDataType(nodeTemplateName, nodeTemplate);
        }

        //Enrich Node Template Artifacts
        super.enrichNodeTemplateArtifactDefinition(nodeTemplateName, nodeTemplate);

    }


    private void populateArtifactTemplateMappingDataType(@NotNull String nodeTemplateName, @NotNull NodeTemplate nodeTemplate)
            throws BluePrintException {
        log.info("****** Processing Artifact Node Template : {}", nodeTemplateName);

        if (nodeTemplate.getProperties() != null) {

            if (!nodeTemplate.getProperties().containsKey(ConfigModelConstant.PROPERTY_RECIPE_NAMES)) {
                throw new BluePrintException("Node Template (" + nodeTemplateName + ") doesn't have "
                        + ConfigModelConstant.PROPERTY_RECIPE_NAMES + " property.");
            }

            // Modified for ONAP converted Object to JsonNode
            JsonNode recipeNames = nodeTemplate.getProperties().get(ConfigModelConstant.PROPERTY_RECIPE_NAMES);

            log.info("Processing Receipe Names : {} ", recipeNames);

            if (recipeNames != null && recipeNames.isArray() && recipeNames.size() > 0) {

                Map<String, PropertyDefinition> mappingProperties =
                        getCapabilityMappingProperties(nodeTemplateName, nodeTemplate);

                for (JsonNode recipeNameNode : recipeNames) {
                    String recipeName = recipeNameNode.textValue();
                    processRecipe(nodeTemplateName, mappingProperties, recipeName);
                }
            }
        }
    }

    private void processRecipe(@NotNull String nodeTemplateName, Map<String, PropertyDefinition> mappingProperties, String recipeName) {
        if (StringUtils.isNotBlank(recipeName)) {
            DataType recipeDataType = this.recipeDataTypes.get(recipeName);
            if (recipeDataType == null) {
                log.info("DataType not present for the recipe({})", recipeName);
                recipeDataType = new DataType();
                recipeDataType.setVersion("1.0.0");
                recipeDataType.setDescription(
                        "This is Dynamic Data type definition generated from resource mapping for the config template name "
                                + nodeTemplateName + ".");
                recipeDataType.setDerivedFrom(ConfigModelConstant.MODEL_TYPE_DATA_TYPE_DYNAMIC);
                Map<String, PropertyDefinition> dataTypeProperties = new HashMap<>();
                recipeDataType.setProperties(dataTypeProperties);
            } else {
                log.info("DataType Already present for the recipe({})", recipeName);
            }

            // Merge all the Recipe Properties
            mergeDataTypeProperties(recipeDataType, mappingProperties);

            // Overwrite Recipe DataType
            this.recipeDataTypes.put(recipeName, recipeDataType);

        }
    }

    private Map<String, PropertyDefinition> getCapabilityMappingProperties(String nodeTemplateName,
                                                                           NodeTemplate nodeTemplate) throws BluePrintException {

        Map<String, PropertyDefinition> dataTypeProperties = null;
        if (nodeTemplate != null && MapUtils.isNotEmpty(nodeTemplate.getCapabilities())) {
            CapabilityAssignment capability =
                    nodeTemplate.getCapabilities().get(ConfigModelConstant.CAPABILITY_PROPERTY_MAPPING);

            if (capability != null && capability.getProperties() != null) {

                String resourceAssignmentContent = JacksonUtils
                        .getJson(capability.getProperties().get(ConfigModelConstant.CAPABILITY_PROPERTY_MAPPING));

                List<ResourceAssignment> resourceAssignments =
                        JacksonUtils.getListFromJson(resourceAssignmentContent, ResourceAssignment.class);

                Preconditions.checkNotNull(resourceAssignments, "Failed to Processing Resource Mapping " + resourceAssignmentContent);
                dataTypeProperties = new HashMap<>();

                for (ResourceAssignment resourceAssignment : resourceAssignments) {
                    if (resourceAssignment != null
                            // && Boolean.valueOf(resourceAssignment.getInputParameter())
                            && resourceAssignment.getProperty() != null
                            && StringUtils.isNotBlank(resourceAssignment.getName())) {

                        // Enrich the Property Definition
                        super.enrichPropertyDefinition(resourceAssignment.getName(), resourceAssignment.getProperty());

                        dataTypeProperties.put(resourceAssignment.getName(), resourceAssignment.getProperty());

                    }
                }

            }
        }
        return dataTypeProperties;
    }

    private void mergeDataTypeProperties(DataType dataType, Map<String, PropertyDefinition> mergeProperties) {
        if (dataType != null && dataType.getProperties() != null && mergeProperties != null) {
            // Add the Other Template Properties
            mergeProperties.forEach((mappingKey, propertyDefinition) -> dataType.getProperties().put(mappingKey, propertyDefinition));
        }
    }

    private void populateRecipeInputs(ServiceTemplate serviceTemplate) {
        if (serviceTemplate.getTopologyTemplate() != null
                && MapUtils.isNotEmpty(serviceTemplate.getTopologyTemplate().getInputs())
                && MapUtils.isNotEmpty(this.recipeDataTypes)
                && MapUtils.isNotEmpty(serviceTemplate.getDataTypes())) {
            this.recipeDataTypes.forEach((recipeName, recipeDataType) -> {
                String dataTypePrefix = recipeName.replace("-action", "") + "-request";
                String dataTypeName = "dt-" + dataTypePrefix;

                serviceTemplate.getDataTypes().put(dataTypeName, recipeDataType);

                PropertyDefinition customInputProperty = new PropertyDefinition();
                customInputProperty.setDescription("This is Dynamic Data type for the receipe " + recipeName + ".");
                customInputProperty.setRequired(Boolean.FALSE);
                customInputProperty.setType(dataTypeName);
                serviceTemplate.getTopologyTemplate().getInputs().put(dataTypePrefix, customInputProperty);

            });
        }
    }
}
