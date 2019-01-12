/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.common.ApplicationConstants;
import org.onap.ccsdk.apps.controllerblueprints.core.config.BluePrintLoadConfiguration;
import org.onap.ccsdk.apps.controllerblueprints.core.data.ErrorCode;
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintCatalogService;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintFileUtils;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.BlueprintModel;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.BlueprintModelSearch;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ControllerBlueprintModelContentRepository;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ControllerBlueprintModelRepository;
import org.onap.ccsdk.apps.controllerblueprints.service.repository.ControllerBlueprintModelSearchRepository;
import org.onap.ccsdk.apps.controllerblueprints.service.utils.BluePrintEnhancerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * BlueprintModelService.java Purpose: Provide Service Template Service processing BlueprintModelService
 *
 * @author Brinda Santh
 * @version 1.0
 */

@Service
public class BlueprintModelService {

    @Autowired
    private BluePrintLoadConfiguration bluePrintLoadConfiguration;

    @Autowired
    private BluePrintCatalogService bluePrintCatalogService;

    @Autowired
    private ControllerBlueprintModelSearchRepository blueprintModelSearchRepository;

    @Autowired
    private ControllerBlueprintModelRepository blueprintModelRepository;

    @Autowired
    private ControllerBlueprintModelContentRepository blueprintModelContentRepository;

    private static final String BLUEPRINT_MODEL_ID_FAILURE_MSG = "failed to get blueprint model id(%s) from repo";
    private static final String BLUEPRINT_MODEL_NAME_VERSION_FAILURE_MSG = "failed to get blueprint model by name(%s)" +
        " and version(%s) from repo";

    /**
     * This is a saveBlueprintModel method
     *
     * @param filePart filePart
     * @return Mono<BlueprintModelSearch>
     * @throws BluePrintException BluePrintException
     */
    public Mono<BlueprintModelSearch> saveBlueprintModel(FilePart filePart) throws BluePrintException {
        try {
            Path cbaLocation = BluePrintFileUtils.Companion
                .getCbaStorageDirectory(bluePrintLoadConfiguration.blueprintArchivePath);
            return BluePrintEnhancerUtils.Companion.saveCBAFile(filePart, cbaLocation).map(fileName -> {
                String blueprintId = null;
                try {
                    blueprintId = bluePrintCatalogService
                        .saveToDatabase(cbaLocation.toFile(), false);
                } catch (BluePrintException e) {
                    // FIXME handle expection
                }
                return blueprintModelSearchRepository.findById(blueprintId).get();
            });
        } catch (IOException e) {
            throw new BluePrintException(ErrorCode.IO_FILE_INTERRUPT.getValue(),
                String.format("I/O Error while uploading the CBA file: %s", e.getMessage()), e);
        }
    }

    /**
     * This is a publishBlueprintModel method to change the status published to YES
     *
     * @param id id
     * @return BlueprintModelSearch
     * @throws BluePrintException BluePrintException
     */
    public BlueprintModelSearch publishBlueprintModel(String id) throws BluePrintException {
        BlueprintModelSearch blueprintModelSearch;
        Optional<BlueprintModelSearch> dbBlueprintModel = blueprintModelSearchRepository.findById(id);
        if (dbBlueprintModel.isPresent()) {
            blueprintModelSearch = dbBlueprintModel.get();
        } else {
            String msg = String.format(BLUEPRINT_MODEL_ID_FAILURE_MSG, id);
            throw new BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.getValue(), msg);
        }
        blueprintModelSearch.setPublished(ApplicationConstants.ACTIVE_Y);
        return blueprintModelSearchRepository.saveAndFlush(blueprintModelSearch);
    }

    /**
     * This is a searchBlueprintModels method
     *
     * @param tags tags
     * @return List<BlueprintModelSearch>
     */
    public List<BlueprintModelSearch> searchBlueprintModels(String tags) {
        return blueprintModelSearchRepository.findByTagsContainingIgnoreCase(tags);
    }

    /**
     * This is a getBlueprintModelSearchByNameAndVersion method
     *
     * @param name name
     * @param version version
     * @return BlueprintModelSearch
     * @throws BluePrintException BluePrintException
     */
    public BlueprintModelSearch getBlueprintModelSearchByNameAndVersion(@NotNull String name, @NotNull String version)
        throws BluePrintException {
        BlueprintModelSearch blueprintModelSearch;
        Optional<BlueprintModelSearch> dbBlueprintModel = blueprintModelSearchRepository
            .findByArtifactNameAndArtifactVersion(name, version);
        if (dbBlueprintModel.isPresent()) {
            blueprintModelSearch = dbBlueprintModel.get();
        } else {
            throw new BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.getValue(),
                String.format(BLUEPRINT_MODEL_NAME_VERSION_FAILURE_MSG, name, version));
        }
        return blueprintModelSearch;
    }

    /**
     * This is a downloadBlueprintModelFileByNameAndVersion method to download a Blueprint by Name and Version
     *
     * @param name name
     * @param version version
     * @return ResponseEntity<Resource>
     * @throws BluePrintException BluePrintException
     */
    public ResponseEntity<Resource> downloadBlueprintModelFileByNameAndVersion(@NotNull String name,
        @NotNull String version)
        throws BluePrintException {
        BlueprintModel blueprintModel;
        try {
            blueprintModel = getBlueprintModelByNameAndVersion(name, version);
        } catch (BluePrintException e) {
            throw new BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.getValue(), String.format("Error while " +
                "downloading the CBA file: %s", e.getMessage()), e);
        }
        String fileName = blueprintModel.getId() + ".zip";
        byte[] file = blueprintModel.getBlueprintModelContent().getContent();
        return prepareResourceEntity(fileName, file);
    }

    /**
     * This is a downloadBlueprintModelFile method to find the target file to download and return a file resource
     *
     * @return ResponseEntity<Resource>
     * @throws BluePrintException BluePrintException
     */
    public ResponseEntity<Resource> downloadBlueprintModelFile(@NotNull String id) throws BluePrintException {
        BlueprintModel blueprintModel;
        try {
            blueprintModel = getBlueprintModel(id);
        } catch (BluePrintException e) {
            throw new BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.getValue(), String.format("Error while " +
                "downloading the CBA file: %s", e.getMessage()), e);
        }
        String fileName = blueprintModel.getId() + ".zip";
        byte[] file = blueprintModel.getBlueprintModelContent().getContent();
        return prepareResourceEntity(fileName, file);
    }

    /**
     * @return ResponseEntity<Resource>
     */
    private ResponseEntity<Resource> prepareResourceEntity(String fileName, byte[] file) {
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("text/plain"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
            .body(new ByteArrayResource(file));
    }

    /**
     * This is a getBlueprintModel method
     *
     * @param id id
     * @return BlueprintModel
     * @throws BluePrintException BluePrintException
     */
    private BlueprintModel getBlueprintModel(@NotNull String id) throws BluePrintException {
        BlueprintModel blueprintModel;
        Optional<BlueprintModel> dbBlueprintModel = blueprintModelRepository.findById(id);
        if (dbBlueprintModel.isPresent()) {
            blueprintModel = dbBlueprintModel.get();
        } else {
            String msg = String.format(BLUEPRINT_MODEL_ID_FAILURE_MSG, id);
            throw new BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.getValue(), msg);
        }
        return blueprintModel;
    }

    /**
     * This is a getBlueprintModelByNameAndVersion method
     *
     * @param name name
     * @param version version
     * @return BlueprintModel
     * @throws BluePrintException BluePrintException
     */
    private BlueprintModel getBlueprintModelByNameAndVersion(@NotNull String name, @NotNull String version)
        throws BluePrintException {
        BlueprintModel blueprintModel = blueprintModelRepository
            .findByArtifactNameAndArtifactVersion(name, version);
        if (blueprintModel != null) {
            return blueprintModel;
        } else {
            String msg = String.format(BLUEPRINT_MODEL_NAME_VERSION_FAILURE_MSG, name, version);
            throw new BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.getValue(), msg);
        }
    }

    /**
     * This is a getBlueprintModelSearch method
     *
     * @param id id
     * @return BlueprintModelSearch
     * @throws BluePrintException BluePrintException
     */
    public BlueprintModelSearch getBlueprintModelSearch(@NotNull String id) throws BluePrintException {
        BlueprintModelSearch blueprintModelSearch;
        Optional<BlueprintModelSearch> dbBlueprintModel = blueprintModelSearchRepository.findById(id);
        if (dbBlueprintModel.isPresent()) {
            blueprintModelSearch = dbBlueprintModel.get();
        } else {
            String msg = String.format(BLUEPRINT_MODEL_ID_FAILURE_MSG, id);
            throw new BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.getValue(), msg);
        }

        return blueprintModelSearch;
    }

    /**
     * This is a deleteBlueprintModel method
     *
     * @param id id
     * @throws BluePrintException BluePrintException
     */
    @Transactional
    public void deleteBlueprintModel(@NotNull String id) throws BluePrintException {
        Optional<BlueprintModel> dbBlueprintModel = blueprintModelRepository.findById(id);
        if (dbBlueprintModel.isPresent()) {
            blueprintModelContentRepository.deleteByBlueprintModel(dbBlueprintModel.get());
            blueprintModelRepository.delete(dbBlueprintModel.get());
        } else {
            String msg = String.format(BLUEPRINT_MODEL_ID_FAILURE_MSG, id);
            throw new BluePrintException(ErrorCode.RESOURCE_NOT_FOUND.getValue(), msg);
        }
    }

    /**
     * This is a getAllBlueprintModel method to retrieve all the BlueprintModel in Database
     *
     * @return List<BlueprintModelSearch> list of the controller blueprint archives
     */
    public List<BlueprintModelSearch> getAllBlueprintModel() {
        return blueprintModelSearchRepository.findAll();
    }
}
