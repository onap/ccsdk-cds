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

package org.onap.ccsdk.apps.controllerblueprints.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.ConfigModelConstant;
import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.service.common.ApplicationConstants;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModel;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModelContent;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ConfigModelContentRepository;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ConfigModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * ConfigModelService.java Purpose: Provide Service Template Service processing ConfigModelService
 *
 * @author Brinda Santh
 * @version 1.0
 */

@Service
public class ConfigModelService {

    private static Logger log = LoggerFactory.getLogger(ConfigModelService.class);

    private ConfigModelRepository configModelRepository;
    private ConfigModelContentRepository configModelContentRepository;
    private ConfigModelCreateService configModelCreateService;

    /**
     * This is a ConfigModelService constructor.
     * 
     * @param configModelRepository
     * @param configModelContentRepository
     * @param configModelCreateService
     */
    public ConfigModelService(ConfigModelRepository configModelRepository,
            ConfigModelContentRepository configModelContentRepository,
            ConfigModelCreateService configModelCreateService) {
        this.configModelRepository = configModelRepository;
        this.configModelContentRepository = configModelContentRepository;
        this.configModelCreateService = configModelCreateService;
    }

    /**
     * This is a getInitialConfigModel method
     * 
     * @param templateName
     * @return ConfigModel
     * @throws BluePrintException
     */
    public ConfigModel getInitialConfigModel(String templateName) throws BluePrintException {
        ConfigModel configModel = null;
        if (StringUtils.isNotBlank(templateName)) {
            configModel = new ConfigModel();
            configModel.setArtifactName(templateName);
            configModel.setArtifactType(ApplicationConstants.ASDC_ARTIFACT_TYPE_SDNC_MODEL);
            configModel.setUpdatedBy("xxxxx@xxx.com");
            ConfigModelContent configModelContent = new ConfigModelContent();
            configModelContent.setContentType(ConfigModelConstant.MODEL_CONTENT_TYPE_TOSCA_JSON);
            configModelContent.setName(templateName);
            String content = this.configModelCreateService.createInitialServiceTemplateContent(templateName);
            configModelContent.setContent(content);

            List<ConfigModelContent> configModelContents = new ArrayList<>();
            configModelContents.add(configModelContent);

            configModel.setConfigModelContents(configModelContents);
        }
        return configModel;
    }

    /**
     * This is a saveConfigModel method
     * 
     * @param configModel
     * @return ConfigModel
     * @throws BluePrintException
     */
    public ConfigModel saveConfigModel(ConfigModel configModel) throws BluePrintException {
        return this.configModelCreateService.saveConfigModel(configModel);
    }

    /**
     * This is a publishConfigModel method
     * 
     * @param id
     * @return ConfigModel
     * @throws BluePrintException
     */
    public ConfigModel publishConfigModel(Long id) throws BluePrintException {
        return this.configModelCreateService.publishConfigModel(id);
    }

    /**
     * This is a searchConfigModels method
     * 
     * @param tags
     * @return ConfigModel
     */
    public List<ConfigModel> searchConfigModels(String tags) {
        List<ConfigModel> models = configModelRepository.findByTagsContainingIgnoreCase(tags);
        if (models != null) {
            for (ConfigModel configModel : models) {
                configModel.setConfigModelContents(null);
            }
        }
        return models;
    }

    /**
     * This is a getConfigModelByNameAndVersion method
     * 
     * @param name
     * @param version
     * @return ConfigModel
     */
    public ConfigModel getConfigModelByNameAndVersion(String name, String version) {
        ConfigModel configModel = null;
        Optional<ConfigModel> dbConfigModel = null;
        if (StringUtils.isNotBlank(version)) {
            dbConfigModel = configModelRepository.findByArtifactNameAndArtifactVersion(name, version);
        } else {
            dbConfigModel = configModelRepository.findTopByArtifactNameOrderByArtifactVersionDesc(name);
        }
        if (dbConfigModel.isPresent()) {
            configModel = dbConfigModel.get();
        }
        return configModel;
    }

    /**
     * This is a getConfigModel method
     * 
     * @param id
     * @return ConfigModel
     */
    public ConfigModel getConfigModel(Long id) {
        ConfigModel configModel = null;
        if (id != null) {
            Optional<ConfigModel> dbConfigModel = configModelRepository.findById(id);
            if (dbConfigModel.isPresent()) {
                configModel = dbConfigModel.get();
            }
        }
        return configModel;
    }

    /**
     * This method returns clone of the given model id, by masking the other unrelated fields
     * 
     * @param id
     * @return
     */

    public ConfigModel getCloneConfigModel(Long id) {

        ConfigModel configModel = null;
        ConfigModel cloneConfigModel = null;
        if (id != null) {
            Optional<ConfigModel> dbConfigModel = configModelRepository.findById(id);
            if (dbConfigModel.isPresent()) {
                configModel = dbConfigModel.get();
                cloneConfigModel = configModel;
                cloneConfigModel.setUpdatedBy("xxxxx@xxx.com");
                cloneConfigModel.setArtifactName("XXXX");
                cloneConfigModel.setPublished("XXXX");
                cloneConfigModel.setPublished("XXXX");
                cloneConfigModel.setUpdatedBy("XXXX");
                cloneConfigModel.setId(null);
                cloneConfigModel.setTags(null);
                cloneConfigModel.setCreatedDate(new Date());
                List<ConfigModelContent> configModelContents = cloneConfigModel.getConfigModelContents();

                if (CollectionUtils.isNotEmpty(configModelContents)) {
                    for (ConfigModelContent configModelContent : configModelContents) {
                        if (configModelContent != null && StringUtils.isNotBlank(configModelContent.getContentType())) {
                            configModelContent.setId(null);
                            configModelContent.setCreationDate(new Date());

                            if (ConfigModelConstant.MODEL_CONTENT_TYPE_TOSCA_JSON
                                    .equalsIgnoreCase(configModelContent.getContentType())) {
                                ServiceTemplate serviceTemplate = JacksonUtils
                                        .readValue(configModelContent.getContent(), ServiceTemplate.class);
                                if (serviceTemplate != null && serviceTemplate.getMetadata() != null) {
                                    serviceTemplate.getMetadata()
                                            .put(BluePrintConstants.METADATA_TEMPLATE_AUTHOR, "XXXX");
                                    serviceTemplate.getMetadata()
                                            .put(BluePrintConstants.METADATA_TEMPLATE_VERSION, "1.0.0");
                                    serviceTemplate.getMetadata()
                                            .put(BluePrintConstants.METADATA_TEMPLATE_NAME, "XXXXXX");

                                    configModelContent.setContent(JacksonUtils.getJson(serviceTemplate));
                                }
                            }
                        }

                    }
                }
            }
        }
        return cloneConfigModel;
    }

    /**
     * This is a deleteConfigModel method
     * 
     * @param id
     */

    @Transactional
    public void deleteConfigModel(Long id) {
        Optional<ConfigModel> dbConfigModel = configModelRepository.findById(id);
        if (dbConfigModel.isPresent()) {
            configModelContentRepository.deleteByConfigModel(dbConfigModel.get());
            configModelRepository.delete(dbConfigModel.get());
        }
    }

}
