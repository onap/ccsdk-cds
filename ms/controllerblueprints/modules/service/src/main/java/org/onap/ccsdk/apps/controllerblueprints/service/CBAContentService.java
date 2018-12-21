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
import org.onap.ccsdk.apps.controllerblueprints.service.domain.CbaContent;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.CBAContentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * CBAContentService.java Purpose: Provide CBAContent Template Service processing
 * CBAContentService
 *
 * @author Ruben Chang
 * @version 1.0
 */

@Service
public class CBAContentService {

    private static EELFLogger log = EELFManager.getInstance().getLogger(CBAContentService.class);

    private CBAContentRepository cbaContentRepository;

    /**
     * Constructor of the class
     * @param cbaContentRepository CRUD methods for entity CBAContentRepository
     */
    public CBAContentService(CBAContentRepository cbaContentRepository) {
        this.cbaContentRepository = cbaContentRepository;
        log.info("CBAContentRepository sucessfully instantiated");
    }

    /**
     * Save the CBAContent into the CBA_CONTENT table
     * @param cbaName The name of the file
     * @param cbaVersion version number of the CBA archive
     * @param cbaState int that would represent the state. Refer to the CbaStateEnum
     * @param cbaDescription Brief description that would help to identify and recognize the CBA archive
     * @param file the file
     * @return CbaContent the record saved into the table CBA_CONTENT
     */
    public CbaContent saveCBAContent(String cbaName, String cbaVersion, int cbaState, String cbaDescription, byte[] file){
        CbaContent cbaContent = new CbaContent();
        cbaContent.setCbaName(cbaName);
        cbaContent.setCbaVersion(cbaVersion);
        cbaContent.setCbaState(cbaState);
        cbaContent.setCbaDescription(cbaDescription);
        cbaContent.setCbaFile(file);
        cbaContentRepository.saveAndFlush(cbaContent);
        return cbaContent;
    }

    /**
     * Get the list of Controller Blueprint archives
     * @return List<CbaContent> list with the controller blueprint archives
     */
    public List<CbaContent> getList(){
        return cbaContentRepository.findAll();
    }

    /**
     * Get a single Controller Blueprint archive by uuID
     * @param uuID the userID controller blueprint identifier
     * @return Optional<CbaContent>
     */
    public Optional<CbaContent> findByUUID(String uuID) {
        return cbaContentRepository.findById(uuID);
    }

    /**
     * Method deleteCBAById: Delete a CBA in data base with it associated Blueprint Model
     * @param uuid the uuid that identify the CBA
     */
    public void deleteCBAById(String uuid) {
        cbaContentRepository.deleteById(uuid);
    }

}
