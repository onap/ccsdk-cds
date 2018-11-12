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

import com.google.common.base.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.jetbrains.annotations.NotNull;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.data.ArtifactType;
import org.onap.ccsdk.apps.controllerblueprints.core.data.DataType;
import org.onap.ccsdk.apps.controllerblueprints.core.data.NodeType;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModel;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ResourceDictionary;
import org.onap.ccsdk.apps.controllerblueprints.service.utils.ConfigModelUtils;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * DataBaseInitService.java Purpose: Provide DataBaseInitService Service
 *
 * @author Brinda Santh
 * @version 1.0
 */

@Component
@ConditionalOnProperty(name = "blueprints.load.initial-data", havingValue = "true")
public class DataBaseInitService {

    private static EELFLogger log = EELFManager.getInstance().getLogger(DataBaseInitService.class);
    private ModelTypeService modelTypeService;
    private ResourceDictionaryService resourceDictionaryService;
    private ConfigModelService configModelService;

    @Value("${load.dataTypePath}")
    private String dataTypePath;
    @Value("${load.nodeTypePath}")
    private String nodeTypePath;
    @Value("${load.artifactTypePath}")
    private String artifactTypePath;
    @Value("${load.resourceDictionaryPath}")
    private String resourceDictionaryPath;
    @Value("${load.blueprintsPath}")
    private String bluePrintsPath;

    @Autowired
    private ResourcePatternResolver resourceLoader;

    /**
     * This is a DataBaseInitService, used to load the initial data
     *
     * @param modelTypeService modelTypeService
     * @param resourceDictionaryService resourceDictionaryService
     * @param configModelService configModelService
     */
    public DataBaseInitService(ModelTypeService modelTypeService, ResourceDictionaryService resourceDictionaryService,
                               ConfigModelService configModelService) {
        this.modelTypeService = modelTypeService;
        this.resourceDictionaryService = resourceDictionaryService;
        this.configModelService = configModelService;
        log.info("DataBaseInitService started...");

    }

    @SuppressWarnings("unused")
    @EventListener(ApplicationReadyEvent.class)
    private void initDatabase() {
        log.info("loading dataTypePath from DIR : {}", dataTypePath);
        log.info("loading nodeTypePath from DIR : {}", nodeTypePath);
        log.info("loading artifactTypePath from DIR : {}", artifactTypePath);
        log.info("loading resourceDictionaryPath from DIR : {}", resourceDictionaryPath);
        log.info("loading bluePrintsPath from DIR : {}", bluePrintsPath);

        loadModelType();
        loadResourceDictionary();
        // TODO("Enable after Multi file Service Template Repository implementation in place")
        //loadBlueprints();
    }

    private void loadModelType() {
        log.info(" *************************** loadModelType **********************");
        try {
            Resource[] dataTypefiles = getPathResources(dataTypePath, ".json");
            StrBuilder errorBuilder = new StrBuilder();
                for (Resource file : dataTypefiles) {
                    if (file != null) {
                        loadDataType(file, errorBuilder);
                    }
                }

            Resource[] nodeTypefiles = getPathResources(nodeTypePath, ".json");
                       for (Resource file : nodeTypefiles) {
                    if (file != null) {
                        loadNodeType(file, errorBuilder);
                    }
                }


            Resource[] artifactTypefiles = getPathResources(artifactTypePath, ".json");

                for (Resource file : artifactTypefiles) {
                    if (file != null) {
                        loadArtifactType(file, errorBuilder);
                    }
                }


            if (!errorBuilder.isEmpty()) {
                log.error(errorBuilder.toString());
            }
        } catch (Exception e) {
            log.error("Failed in Data type loading", e);
        }
    }

    private void loadResourceDictionary() {
        log.info(
                " *************************** loadResourceDictionary **********************");
        try {
            Resource[] dataTypefiles = getPathResources(resourceDictionaryPath, ".json");

                StrBuilder errorBuilder = new StrBuilder();
                String fileName;
                for (Resource file : dataTypefiles) {
                    try {
                        fileName = file.getFilename();
                        log.trace("Loading : {}", fileName);
                        String definitionContent = getResourceContent(file);
                        ResourceDefinition resourceDefinition =
                                JacksonUtils.readValue(definitionContent, ResourceDefinition.class);
                        if (resourceDefinition != null) {
                            Preconditions.checkNotNull(resourceDefinition.getProperty(), "Failed to get Property Definition");
                            ResourceDictionary resourceDictionary = new ResourceDictionary();
                            resourceDictionary.setName(resourceDefinition.getName());
                            resourceDictionary.setDefinition(resourceDefinition);

                            Preconditions.checkNotNull(resourceDefinition.getProperty(), "Property field is missing");
                            resourceDictionary.setDescription(resourceDefinition.getProperty().getDescription());
                            resourceDictionary.setDataType(resourceDefinition.getProperty().getType());
                            if(resourceDefinition.getProperty().getEntrySchema() != null){
                                resourceDictionary.setEntrySchema(resourceDefinition.getProperty().getEntrySchema().getType());
                            }
                            resourceDictionary.setUpdatedBy(resourceDefinition.getUpdatedBy());
                            if (StringUtils.isBlank(resourceDefinition.getTags())) {
                                resourceDictionary.setTags(
                                        resourceDefinition.getName() + ", " + resourceDefinition.getUpdatedBy()
                                                + ", " + resourceDefinition.getUpdatedBy());

                            } else {
                                resourceDictionary.setTags(resourceDefinition.getTags());
                            }
                            resourceDictionaryService.saveResourceDictionary(resourceDictionary);

                            log.trace(" Loaded successfully : {}", file.getFilename());
                        } else {
                            throw new BluePrintException("couldn't get dictionary from content information");
                        }
                    } catch (Exception e) {
                        errorBuilder.appendln("Dictionary loading Errors : " + file.getFilename() + ":" + e.getMessage());
                    }
                }
                if (!errorBuilder.isEmpty()) {
                    log.error(errorBuilder.toString());
                }


        } catch (Exception e) {
            log.error(
                    "Failed in Resource dictionary loading", e);
        }
    }

    private void loadBlueprints() {
        log.info("*************************** loadServiceTemplate **********************");
        try {
            List<String> serviceTemplateDirs = ConfigModelUtils.getBlueprintNames(bluePrintsPath);
            if (CollectionUtils.isNotEmpty(serviceTemplateDirs)) {
                StrBuilder errorBuilder = new StrBuilder();
                for (String fileName : serviceTemplateDirs) {
                    try {
                        String bluePrintPath = this.bluePrintsPath.concat("/").concat(fileName);
                        log.debug("***** Loading service template :  {}", bluePrintPath);
                        ConfigModel configModel = ConfigModelUtils.getConfigModel(bluePrintPath);

                        configModel = this.configModelService.saveConfigModel(configModel);

                        log.info("Publishing : {}", configModel.getId());

                        this.configModelService.publishConfigModel(configModel.getId());

                        log.info("Loaded service template successfully: {}", fileName);

                    } catch (Exception e) {
                        errorBuilder.appendln("load config model " + fileName + " error : " + e.getMessage());
                    }
                }

                if (!errorBuilder.isEmpty()) {
                    log.error(errorBuilder.toString());
                }
            }
        } catch (Exception e) {
            log.error("Failed in Service Template loading", e);
        }
    }

    private void loadNodeType(Resource file, StrBuilder errorBuilder) {
        try {
            log.trace("Loading Node Type : {}", file.getFilename());
            String nodeKey = file.getFilename().replace(".json", "");
            String definitionContent = getResourceContent(file);
            NodeType nodeType = JacksonUtils.readValue(definitionContent, NodeType.class);
            Preconditions.checkNotNull(nodeType, String.format("failed to get node type from file : %s", file.getFilename()));
            ModelType modelType = new ModelType();
            modelType.setDefinitionType(BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TYPE);
            modelType.setDerivedFrom(nodeType.getDerivedFrom());
            modelType.setDescription(nodeType.getDescription());
            modelType.setDefinition(JacksonUtils.jsonNode(definitionContent));
            modelType.setModelName(nodeKey);
            modelType.setVersion(nodeType.getVersion());
            modelType.setUpdatedBy("System");
            modelType.setTags(nodeKey + "," + BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TYPE + ","
                    + nodeType.getDerivedFrom());
            modelTypeService.saveModel(modelType);
            log.trace("Loaded Node Type successfully : {}", file.getFilename());
        } catch (Exception e) {
            errorBuilder.appendln("Node type loading error : " + file.getFilename() + ":" + e.getMessage());
        }
    }

    private void loadDataType(@NotNull Resource file, StrBuilder errorBuilder) {
        try {
            log.trace("Loading Data Type: {}", file.getFilename());
            String dataKey = file.getFilename().replace(".json", "");
            String definitionContent = getResourceContent(file);
            DataType dataType = JacksonUtils.readValue(definitionContent, DataType.class);
            Preconditions.checkNotNull(dataType, String.format("failed to get data type from file : %s", file.getFilename()));
            ModelType modelType = new ModelType();
            modelType.setDefinitionType(BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE);
            modelType.setDerivedFrom(dataType.getDerivedFrom());
            modelType.setDescription(dataType.getDescription());
            modelType.setDefinition(JacksonUtils.jsonNode(definitionContent));
            modelType.setModelName(dataKey);
            modelType.setVersion(dataType.getVersion());
            modelType.setUpdatedBy("System");
            modelType.setTags(dataKey + "," + dataType.getDerivedFrom() + ","
                    + BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE);
            modelTypeService.saveModel(modelType);
            log.trace(" Loaded Data Type successfully : {}", file.getFilename());
        } catch (Exception e) {
            errorBuilder.appendln("Data type loading error : " + file.getFilename() + ":" + e.getMessage());
        }
    }

    private void loadArtifactType(Resource file, StrBuilder errorBuilder) {
        try {
            log.trace("Loading Artifact Type: {}", file.getFilename());
            String dataKey = file.getFilename().replace(".json", "");
            String definitionContent = getResourceContent(file);
            ArtifactType artifactType = JacksonUtils.readValue(definitionContent, ArtifactType.class);
            Preconditions.checkNotNull(artifactType, String.format("failed to get artifact type from file : %s", file.getFilename()));
            ModelType modelType = new ModelType();
            modelType.setDefinitionType(BluePrintConstants.MODEL_DEFINITION_TYPE_ARTIFACT_TYPE);
            modelType.setDerivedFrom(artifactType.getDerivedFrom());
            modelType.setDescription(artifactType.getDescription());
            modelType.setDefinition(JacksonUtils.jsonNode(definitionContent));
            modelType.setModelName(dataKey);
            modelType.setVersion(artifactType.getVersion());
            modelType.setUpdatedBy("System");
            modelType.setTags(dataKey + "," + artifactType.getDerivedFrom() + ","
                    + BluePrintConstants.MODEL_DEFINITION_TYPE_ARTIFACT_TYPE);
            modelTypeService.saveModel(modelType);
            log.trace("Loaded Artifact Type successfully : {}", file.getFilename());
        } catch (Exception e) {
            errorBuilder.appendln("Artifact type loading error : " + file.getFilename() + ":" + e.getMessage());
        }
    }

    private Resource[] getPathResources(String path, String extension) throws IOException {
        return resourceLoader.getResources("file:" + path + "/*" + extension);
    }

    private String getResourceContent(Resource resource) throws IOException {
        return IOUtils.toString(resource.getInputStream(), Charset.defaultCharset());
    }

}
