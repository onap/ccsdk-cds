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
import org.jetbrains.annotations.NotNull;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintFileUtils;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.CbaContent;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModel;
import org.onap.ccsdk.apps.controllerblueprints.service.model.BlueprintModelResponse;
import org.onap.ccsdk.apps.controllerblueprints.service.model.ItemCbaResponse;
import org.onap.ccsdk.apps.controllerblueprints.service.utils.CbaStateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CbaService.java Purpose: Provide Service Template Service processing CbaService
 *
 * @author Steve Siani
 * @version 1.0
 */

@Service
public class CbaService {

    private static EELFLogger log = EELFManager.getInstance().getLogger(CbaService.class);

    @Value("${controllerblueprints.blueprintArchivePath}")
    private String cbaArchivePath;
    private Path cbaLocation;

    @Autowired
    private CbaFileManagementService cbaFileManagementService;

    @Autowired
    private CbaToDatabaseService cbaToDatabaseService;


    /**
     * This method would be used by SpringBoot to initialize the cba location
     */
    @EventListener(ApplicationReadyEvent.class)
    private void initCbaService() {
        this.cbaLocation = BluePrintFileUtils.Companion.getCbaStorageDirectory(cbaArchivePath);
        log.info("CBA service Initiated...");
    }

    /**
     * This is a uploadCBAFile method
     * take a {@link FilePart}, transfer it to disk using WebFlux and return a {@link Mono} representing the result
     *
     * @param filePart - the request part containing the file to be saved
     * @return a {@link Mono<  BlueprintModelResponse  >} representing the result of the operation
     */
    public Mono<BlueprintModelResponse> uploadCBAFile(FilePart filePart) {

        try {
            return this.cbaFileManagementService.saveCBAFile(filePart, cbaLocation).map(fileName -> {
                ConfigModel configModel;
                BlueprintModelResponse blueprintModelResponse = null;

                try {
                    String cbaDirectory = this.cbaFileManagementService.decompressCBAFile(fileName, cbaLocation);
                    configModel = this.cbaToDatabaseService.storeBluePrints(cbaDirectory, fileName, cbaLocation.resolve(fileName));
                    blueprintModelResponse = new BlueprintModelResponse(configModel.getId(), configModel.getArtifactName(), configModel.getArtifactVersion(), configModel.getArtifactDescription(), configModel.getConfigModelCBA().getCbaUUID());
                } catch (BluePrintException be) {
                    Mono.error(new BluePrintException("Error loading CBA in database.", be));
                } finally {
                    try {
                        this.cbaFileManagementService.cleanupSavedCBA(fileName, cbaLocation);
                    } catch (BluePrintException be) {
                        Mono.error(new BluePrintException("Error while cleaning up.", be));
                    }
                }
                return blueprintModelResponse;
            });
        } catch (IOException | BluePrintException e) {
            return Mono.error(new BluePrintException("Error uploading the CBA file in channel.", e));
        }
    }

    /**
     * This is a deleteCba method
     *
     * @param id id
     * @throws BluePrintException BluePrintException
     */
    public void deleteCBA(@NotNull Long id) throws BluePrintException {
        this.cbaToDatabaseService.deleteCBA(id);
    }

    /**
     * This is a downloadCBAFile method to find the target file to download and return a file ressource using MONO
     *
     * @param (id)
     * @return ResponseEntity<Resource>
     */
    public ResponseEntity<Resource> downloadCBAFile(@NotNull String id) {
        Optional<CbaContent> optionalContent = this.cbaToDatabaseService.findByUUID(id);

        CbaContent cbaContent = optionalContent.get();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/plain"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cbaContent.getCbaName() + "\"")
                .body(new ByteArrayResource(cbaContent.getCbaFile()));
    }

    /**
     * This is a findCBAByID method to find a CBA By the UUID
     *
     * @param (id)
     * @return ItemCbaResponse
     */
    public ItemCbaResponse findCBAByID(@NotNull String id) {
        ItemCbaResponse response = new ItemCbaResponse();
        Optional<CbaContent> optionalContent = this.cbaToDatabaseService.findByUUID(id);

        CbaContent cbaContent = optionalContent.get();
        response.setName(cbaContent.getCbaName());
        response.setState(cbaContent.getCbaState());
        response.setId(cbaContent.getCbaUUID());
        response.setVersion(cbaContent.getCbaVersion());
        response.setDescription(cbaContent.getCbaDescription());
        return response;
    }

    /**
     * This is a findAllCBA method to retrieve all the CBAs in Database
     *
     * @return List<ItemCbaResponse> list with the controller blueprint archives
     */
    public List<ItemCbaResponse> findAllCBA() {
        List<ItemCbaResponse> responseList = new ArrayList<>();
        List<CbaContent> cbaContents = this.cbaToDatabaseService.listCBAFiles();

        for(CbaContent content: cbaContents){
            ItemCbaResponse response = new ItemCbaResponse();
            response.setName(content.getCbaName());
            response.setState(content.getCbaState());
            response.setId(content.getCbaUUID());
            response.setVersion(content.getCbaVersion());
            response.setDescription(content.getCbaDescription());

            responseList.add(response);
        }
        return responseList;
    }

    /**
     * This is a findCBAByNameAndVersion method to find a CBA by Name and version
     *
     * @param (name, version)
     * @return
     * @throws BluePrintException BluePrintException
     */
    public ItemCbaResponse findCBAByNameAndVersion(@NotNull String name, @NotNull String version) throws BluePrintException {
        return null;
    }
}