/*
 * Copyright © 2018 IBM Intellectual Property.
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
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.CbaContent;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModel;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ConfigModelContentRepository;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ConfigModelRepository;
import org.onap.ccsdk.apps.controllerblueprints.service.utils.CbaStateEnum;
import org.onap.ccsdk.apps.controllerblueprints.service.utils.ConfigModelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * This class acts as a Rest Service that would store in the Database the Blueprints.
 * @author Ruben Chang
 */

@Service
public class CbaToDatabaseService {

    //Log used to trace the transactions using the EELFLogger class
    private static EELFLogger log = EELFManager.getInstance().getLogger(CbaToDatabaseService.class);

    @Autowired
    private ConfigModelRepository configModelRepository;
    @Autowired
    private ConfigModelContentRepository configModelContentRepository;
    @Autowired
    private ConfigModelCreateService configModelCreateService;	
	@Autowired
    private CBAContentService cbaContentService;

    /**
     * This method will store the blueprints into the DB on the tables CONFIG_MODEL and CONFIG_MODEL_CONTENT
     * @param cbaArchiveToSave Path in which the components are stored
     * @return ConfigModel The Blueprint object stored in the DB
     */
    public ConfigModel storeBluePrints(String cbaDirectory, String cbaFileName, Path cbaArchiveToSave) throws BluePrintException {
        log.info("*************************** storeBluePrints **********************");
        ConfigModel configModel = null;
        CbaContent cbaContent;
        String version = "1.0";//TODO Read these information from metadata
        String description = "Initial description for CBA archive " + cbaFileName;//TODO

        List<String> serviceTemplateDirs = ConfigModelUtils.getBlueprintNames(cbaDirectory);
        if (CollectionUtils.isNotEmpty(serviceTemplateDirs)) {
            for (String fileName : serviceTemplateDirs) {
                try {
                    String bluePrintPath = cbaDirectory.concat("/").concat(fileName);
                    log.debug("***** Loading service template :  {}", bluePrintPath);
                    configModel = ConfigModelUtils.getConfigModel(bluePrintPath);

                    configModel = this.configModelCreateService.saveConfigModel(configModel);

                    log.info("Loaded service template successfully: {}", fileName);
                } catch (Exception e) {
                    throw new BluePrintException("Load config model " + fileName + " error : "+e.getMessage());
                }
            }
        } else {
            throw new BluePrintException("Invalid structure. The unzipped file does not contains Blueprints");
        }

        byte[] file;
        try {
            file = Files.readAllBytes(cbaArchiveToSave);
        } catch (IOException e) {
            throw new BluePrintException("Fail to read the CBA to save in database.", e);
        }

        cbaContent = this.cbaContentService.saveCBAContent(cbaFileName, version, CbaStateEnum.DRAFT.getState(), description, file);
        configModel.setConfigModelCBA(cbaContent);

        return configModel;
    }

    /**
     * This is a deleteConfigModel method
     *
     * @param id id
     * @throws BluePrintException BluePrintException
     */
    public void deleteCBA(@NotNull Long id) throws BluePrintException {
        Optional<ConfigModel> dbConfigModel = configModelRepository.findById(id);

       //TODO: Delete CBA and COnfigModel

    }
	
	/**
     * Get a list of the controller blueprint archives
     * @return List<CbaContent> List with the controller blueprint archives
     */
    public List<CbaContent> listCBAFiles() {
        return this.cbaContentService.getList();
    }

    /**
     * Find a Controller Blueprint Archive by UUID
     * @param uuID the User Identifier Controller Blueprint archive
     * @return Optional<CbaContent> the Controller Blueprint archive
     */
    public Optional<CbaContent> findByUUID(String uuID) {
        return this.cbaContentService.findByUUID(uuID);
    }
}