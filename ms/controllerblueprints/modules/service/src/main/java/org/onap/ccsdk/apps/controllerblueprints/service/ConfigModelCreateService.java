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
import org.jetbrains.annotations.NotNull;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.ConfigModelConstant;
import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.service.common.ApplicationConstants;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModel;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModelContent;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ConfigModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ServiceTemplateCreateService.java Purpose: Provide Service Template Create Service processing
 * ServiceTemplateCreateService
 *
 * @author Brinda Santh
 * @version 1.0
 */

@Service
public class ConfigModelCreateService {

    private static Logger log = LoggerFactory.getLogger(ConfigModelCreateService.class);

    private ConfigModelRepository configModelRepository;
    private ConfigModelValidatorService configModelValidatorService;

    /**
     * This is a ConfigModelCreateService
     *
     * @param configModelRepository       ConfigModelRepository
     * @param configModelValidatorService ConfigModelValidatorService
     */
    public ConfigModelCreateService(ConfigModelRepository configModelRepository,
                                    ConfigModelValidatorService configModelValidatorService) {
        this.configModelRepository = configModelRepository;
        this.configModelValidatorService = configModelValidatorService;
    }

    /**
     * This is a createInitialServiceTemplateContent method
     *
     * @param templateName templateName
     * @return String
     * @throws BluePrintException BluePrintException
     */
    public String createInitialServiceTemplateContent(String templateName) throws BluePrintException {
        String serviceTemplateContent = null;
        if (StringUtils.isNotBlank(templateName)) {
            try {
                serviceTemplateContent = IOUtils.toString(ConfigModelCreateService.class.getClassLoader()
                        .getResourceAsStream("service_template/" + templateName + ".json"), Charset.defaultCharset());
            } catch (IOException e) {
                throw new BluePrintException(e.getMessage(), e);
            }

        }
        return serviceTemplateContent;
    }

    /**
     * This is a createInitialServiceTemplate method
     *
     * @param templateName templateName
     * @return ServiceTemplate
     * @throws BluePrintException BluePrintException
     */
    public ServiceTemplate createInitialServiceTemplate(String templateName) throws BluePrintException {
        ServiceTemplate serviceTemplate = null;
        if (StringUtils.isNotBlank(templateName)) {
            try {
                String serviceTemplateContent = IOUtils.toString(ConfigModelCreateService.class.getClassLoader()
                        .getResourceAsStream("service_template/" + templateName + ".json"), Charset.defaultCharset());
                if (StringUtils.isNotBlank(serviceTemplateContent)) {
                    serviceTemplate = JacksonUtils.readValue(serviceTemplateContent, ServiceTemplate.class);
                }
            } catch (IOException e) {
                throw new BluePrintException(e.getMessage(), e);
            }

        }
        return serviceTemplate;
    }

    /**
     * This is a saveConfigModel method
     *
     * @param configModel configModel
     * @return ConfigModel
     * @throws BluePrintException BluePrintException
     */
    public ConfigModel saveConfigModel(ConfigModel configModel) throws BluePrintException {

        if (configModel != null) {
            String artifactName = configModel.getArtifactName();
            String artifactVersion = configModel.getArtifactVersion();
            String author = configModel.getUpdatedBy();
            // configModel.setTags(artifactName);

            if (StringUtils.isBlank(author)) {
                throw new BluePrintException("Artifact Author is missing in the Service Template");
            }

            if (StringUtils.isBlank(artifactName)) {
                throw new BluePrintException("Artifact Name is missing in the Service Template");
            }

            if (StringUtils.isBlank(artifactVersion)) {
                throw new BluePrintException("Artifact Version is missing in the Service Template");
            }
            ConfigModel updateConfigModel;

            Optional<ConfigModel> dbConfigModelOptional = Optional.empty();

            if (configModel.getId() != null) {
                log.info("Searching for config model id : {}", configModel.getId());
                dbConfigModelOptional = configModelRepository.findById(configModel.getId());
            }

            if (!dbConfigModelOptional.isPresent()) {
                log.info("Searching for config model name :"
                        + configModel.getArtifactName() + ", version " + configModel.getArtifactVersion());
                dbConfigModelOptional = configModelRepository.findByArtifactNameAndArtifactVersion(
                        configModel.getArtifactName(), configModel.getArtifactVersion());
            }

            if (dbConfigModelOptional.isPresent()) {
                updateConfigModel = dbConfigModelOptional.get();
                log.info("Processing for config model id : {} with config model content count : {}"
                        , updateConfigModel.getId(), updateConfigModel.getConfigModelContents().size());
            } else {
                ConfigModel tempConfigModel = new ConfigModel();
                tempConfigModel.setArtifactType(ApplicationConstants.ASDC_ARTIFACT_TYPE_SDNC_MODEL);
                tempConfigModel.setArtifactName(artifactName);
                tempConfigModel.setArtifactVersion(artifactVersion);
                tempConfigModel.setUpdatedBy(author);
                tempConfigModel.setPublished(ApplicationConstants.ACTIVE_N);
                tempConfigModel.setTags(artifactName);
                configModelRepository.saveAndFlush(tempConfigModel);
                updateConfigModel = tempConfigModel;
            }

            Long dbConfigModelId = updateConfigModel.getId();

            if (dbConfigModelId == null) {
                throw new BluePrintException("failed to get the initial saved config model id.");
            }

            log.info("Processing for config model id : {}", dbConfigModelId);

            deleteConfigModelContent(dbConfigModelId);

            addConfigModelContent(dbConfigModelId, configModel);

            // Populate Content model types
            updateConfigModel = updateConfigModel(dbConfigModelId, artifactName, artifactVersion, author);


            return updateConfigModel;
        } else {
            throw new BluePrintException("Config model information is missing");
        }

    }

    private void deleteConfigModelContent(Long dbConfigModelId) {
        if (dbConfigModelId != null) {
            ConfigModel dbConfigModel = configModelRepository.getOne(dbConfigModelId);
            if (CollectionUtils.isNotEmpty(dbConfigModel.getConfigModelContents())) {
                dbConfigModel.getConfigModelContents().clear();
                log.debug("Configuration Model content deleting : {}", dbConfigModel.getConfigModelContents());
                configModelRepository.saveAndFlush(dbConfigModel);
            }

        }
    }

    private void addConfigModelContent(Long dbConfigModelId, ConfigModel configModel) {
        if (dbConfigModelId != null && configModel != null
                && CollectionUtils.isNotEmpty(configModel.getConfigModelContents())) {
            ConfigModel dbConfigModel = configModelRepository.getOne(dbConfigModelId);
            for (ConfigModelContent configModelContent : configModel.getConfigModelContents()) {
                if (configModelContent != null) {
                    configModelContent.setId(null);
                    configModelContent.setConfigModel(dbConfigModel);
                    dbConfigModel.getConfigModelContents().add(configModelContent);
                    log.debug("Configuration Model content adding : {}", configModelContent);
                }
            }
            configModelRepository.saveAndFlush(dbConfigModel);
        }
    }

    private ConfigModel updateConfigModel(Long dbConfigModelId, String artifactName, String artifactVersion,
                                          String author) throws BluePrintException {

        ConfigModel dbConfigModel = configModelRepository.getOne(dbConfigModelId);
        // Populate tags from metadata
        String tags = getConfigModelTags(dbConfigModel);
        if (StringUtils.isBlank(tags)) {
            throw new BluePrintException("Failed to populate tags for the config model name " + artifactName);
        }
        dbConfigModel.setArtifactType(ApplicationConstants.ASDC_ARTIFACT_TYPE_SDNC_MODEL);
        dbConfigModel.setArtifactName(artifactName);
        dbConfigModel.setArtifactVersion(artifactVersion);
        dbConfigModel.setUpdatedBy(author);
        dbConfigModel.setPublished(ApplicationConstants.ACTIVE_N);
        dbConfigModel.setTags(tags);
        configModelRepository.saveAndFlush(dbConfigModel);
        log.info("Config model ({}) saved successfully.", dbConfigModel.getId());
        return dbConfigModel;
    }

    private List<String> getValidContentTypes() {
        List<String> valids = new ArrayList<>();
        valids.add(ConfigModelConstant.MODEL_CONTENT_TYPE_TOSCA_JSON);
        valids.add(ConfigModelConstant.MODEL_CONTENT_TYPE_TEMPLATE);
        return valids;

    }

    private String getConfigModelTags(ConfigModel configModel) throws BluePrintException {
        String tags = null;
        if (CollectionUtils.isNotEmpty(configModel.getConfigModelContents())) {

            for (ConfigModelContent configModelContent : configModel.getConfigModelContents()) {
                if (configModelContent != null && StringUtils.isNotBlank(configModelContent.getContentType())) {

                    if (!getValidContentTypes().contains(configModelContent.getContentType())) {
                        throw new BluePrintException(configModelContent.getContentType()
                                + " is not a valid content type, It should be any one of this "
                                + getValidContentTypes());
                    }

                    if (configModelContent.getContentType().equals(ConfigModelConstant.MODEL_CONTENT_TYPE_TOSCA_JSON)) {
                        ServiceTemplate serviceTemplate =
                                JacksonUtils.readValue(configModelContent.getContent(), ServiceTemplate.class);
                        Preconditions.checkNotNull(serviceTemplate, "failed to transform service template content");
                        if (serviceTemplate.getMetadata() != null) {
                            serviceTemplate.getMetadata().put(BluePrintConstants.METADATA_TEMPLATE_AUTHOR,
                                    configModel.getUpdatedBy());
                            serviceTemplate.getMetadata().put(BluePrintConstants.METADATA_TEMPLATE_VERSION,
                                    configModel.getArtifactVersion());
                            serviceTemplate.getMetadata().put(BluePrintConstants.METADATA_TEMPLATE_NAME,
                                    configModel.getArtifactName());
                        }
                        tags = String.valueOf(serviceTemplate.getMetadata());
                    }
                }
            }
        }
        return tags;
    }

    /**
     * This is a publishConfigModel method
     *
     * @param id id
     * @return ConfigModel
     * @throws BluePrintException BluePrintException
     */
    public ConfigModel publishConfigModel(@NotNull Long id) throws BluePrintException {
        ConfigModel dbConfigModel = null;
        Optional<ConfigModel> dbConfigModelOptional = configModelRepository.findById(id);
        if (dbConfigModelOptional.isPresent()) {
            dbConfigModel = dbConfigModelOptional.get();
            List<ConfigModelContent> configModelContents = dbConfigModel.getConfigModelContents();
            if (configModelContents != null && !configModelContents.isEmpty()) {
                for (ConfigModelContent configModelContent : configModelContents) {
                    if (configModelContent.getContentType()
                            .equals(ConfigModelConstant.MODEL_CONTENT_TYPE_TOSCA_JSON)) {
                        ServiceTemplate serviceTemplate = JacksonUtils
                                .readValue(configModelContent.getContent(), ServiceTemplate.class);
                        if (serviceTemplate != null) {
                            validateServiceTemplate(serviceTemplate);
                        }
                    }
                }
            }
            dbConfigModel.setPublished(ApplicationConstants.ACTIVE_Y);
            configModelRepository.save(dbConfigModel);
            log.info("Config model ({}) published successfully.", id);
        } else {
            throw new BluePrintException(String.format("Couldn't get Config model for id :(%s)", id));
        }
        return dbConfigModel;
    }

    /**
     * This is a validateServiceTemplate method
     *
     * @param serviceTemplate Service Template
     * @return ServiceTemplate
     * @throws BluePrintException BluePrintException
     */
    public ServiceTemplate validateServiceTemplate(ServiceTemplate serviceTemplate) throws BluePrintException {
        return this.configModelValidatorService.validateServiceTemplate(serviceTemplate);
    }
}
