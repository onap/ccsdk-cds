/*
 * Copyright © 2019 Bell Canada
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

package org.onap.ccsdk.cds.sdclistener.service;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintUploadInput;
import org.onap.ccsdk.cds.controllerblueprints.management.api.FileChunk;
import org.onap.ccsdk.cds.controllerblueprints.management.api.UploadAction;
import org.onap.ccsdk.cds.sdclistener.client.SdcListenerAuthClientInterceptor;
import org.onap.ccsdk.cds.sdclistener.dto.SdcListenerDto;
import org.onap.ccsdk.cds.sdclistener.handler.BluePrintProcesssorHandler;
import org.onap.ccsdk.cds.sdclistener.status.SdcListenerStatus;
import org.onap.ccsdk.cds.sdclistener.util.FileUtil;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.lang.String.format;
import static org.onap.ccsdk.cds.sdclistener.status.SdcListenerStatus.NotificationType.SDC_LISTENER_COMPONENT;
import static org.onap.sdc.utils.DistributionStatusEnum.COMPONENT_DONE_ERROR;
import static org.onap.sdc.utils.DistributionStatusEnum.COMPONENT_DONE_OK;

@Component
@ConfigurationProperties("listenerservice")
public class ListenerServiceImpl implements ListenerService {

    @Autowired
    private BluePrintProcesssorHandler bluePrintProcesssorHandler;

    @Autowired
    private SdcListenerAuthClientInterceptor sdcListenerAuthClientInterceptor;

    @Autowired
    private SdcListenerStatus listenerStatus;

    @Autowired
    private SdcListenerDto sdcListenerDto;

    @Value("${listenerservice.config.grpcAddress}")
    private String grpcAddress;

    @Value("${listenerservice.config.grpcPort}")
    private int grpcPort;

    private static final String CBA_ZIP_PATH =
            "Artifacts/[a-zA-Z0-9-_.]+/Deployment/CONTROLLER_BLUEPRINT_ARCHIVE/[a-zA-Z0-9-_.()]+[.]zip";
    private static final int SUCCESS_CODE = 200;
    private static final Logger LOGGER = LoggerFactory.getLogger(ListenerServiceImpl.class);

    @Override
    public void extractBluePrint(String csarArchivePath, String cbaArchivePath) {
        int validPathCount = 0;
        final String distributionId = getDistributionId();
        final String artifactUrl = getArtifactUrl();
        Path cbaStorageDir = getStorageDirectory(cbaArchivePath);
        try (ZipFile zipFile = new ZipFile(csarArchivePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String fileName = entry.getName();
                if (Pattern.matches(CBA_ZIP_PATH, fileName)) {
                    validPathCount++;
                    final String cbaArchiveName = Paths.get(fileName).getFileName().toString();
                    LOGGER.info("Storing the CBA archive {}", cbaArchiveName);
                    storeBluePrint(zipFile, cbaArchiveName, cbaStorageDir, entry);
                }
            }

            if (validPathCount == 0) {
                LOGGER.info(
                        "CBA archive doesn't exist in the CSAR Package or it doesn't exist as per the given path {}",
                        CBA_ZIP_PATH);
                listenerStatus.sendResponseBackToSdc(distributionId, COMPONENT_DONE_OK, null, artifactUrl,
                        SDC_LISTENER_COMPONENT);
            }

        } catch (Exception e) {
            final String errorMessage = format("Failed to extract blueprint %s", e.getMessage());
            listenerStatus.sendResponseBackToSdc(distributionId, COMPONENT_DONE_ERROR, errorMessage, artifactUrl,
                    SDC_LISTENER_COMPONENT);
            LOGGER.error(errorMessage);
        }
    }

    private void storeBluePrint(ZipFile zipFile, String fileName, Path cbaArchivePath, ZipEntry entry) {
        Path targetLocation = cbaArchivePath.resolve(fileName);
        LOGGER.info("The target location for zip file is {}", targetLocation);
        File targetZipFile = new File(targetLocation.toString());

        try {
            if (!targetZipFile.createNewFile()) {
                LOGGER.warn("Overwriting zip file {}", targetLocation);
            }
        } catch (IOException e) {
            LOGGER.error("Could not able to create file {}", targetZipFile, e);
        }

        try (InputStream inputStream = zipFile.getInputStream(entry);
                OutputStream out = new FileOutputStream(targetZipFile)) {
            IOUtils.copy(inputStream, out);
            LOGGER.info("Successfully store the CBA archive {} at this location", targetZipFile);
        } catch (Exception e) {
            LOGGER.error("Failed to put zip file into target location {}, {}", targetLocation, e);
        }
    }

    @Override
    public void saveBluePrintToCdsDatabase(Path cbaArchivePath, ManagedChannel channel) {
        List<File> zipFiles = FileUtil.getFilesFromDisk(cbaArchivePath);
        if (!zipFiles.isEmpty()) {
            prepareRequestForCdsBackend(zipFiles, channel, cbaArchivePath.toString());
        }
    }

    @Override
    public void extractCsarAndStore(IDistributionClientDownloadResult result, Path csarArchivePath) {

        // Create CSAR storage directory
        Path csarStorageDir = getStorageDirectory(csarArchivePath.toString());
        byte[] payload = result.getArtifactPayload();
        String csarFileName = result.getArtifactFilename();
        Path targetLocation = csarStorageDir.resolve(csarFileName);

        LOGGER.info("The target location for the CSAR file is {}", targetLocation);

        File targetCsarFile = new File(targetLocation.toString());

        try (FileOutputStream outFile = new FileOutputStream(targetCsarFile)) {
            outFile.write(payload, 0, payload.length);
            if (!csarArchivePath.toFile().exists()) {
                LOGGER.error("Could not able to store the CSAR at this location {}", csarArchivePath);
            }
        } catch (Exception e) {
            LOGGER.error("Fail to write the data into FileOutputStream {}, {}", targetLocation, e);
        }
    }

    private Path getStorageDirectory(String path) {
        Path fileStorageLocation = Paths.get(path).toAbsolutePath().normalize();

        if (!fileStorageLocation.toFile().exists()) {
            try {
                return Files.createDirectories(fileStorageLocation);
            } catch (IOException e) {
                LOGGER.error("Fail to create directory {}, {}", e, fileStorageLocation);
            }
        }
        return fileStorageLocation;
    }

    private void prepareRequestForCdsBackend(List<File> files, ManagedChannel managedChannel, String path) {
        final String distributionId = getDistributionId();
        final String artifactUrl = getArtifactUrl();

        files.forEach(zipFile -> {
            try {
                final BluePrintUploadInput request = generateBluePrintUploadInputBuilder(zipFile, path);

                // Send request to CDS Backend.
                final Status responseStatus = bluePrintProcesssorHandler.sendRequest(request, managedChannel);

                if (responseStatus.getCode() != SUCCESS_CODE) {
                    final String errorMessage = format("Failed to store the CBA archive into CDS DB due to %s",
                            responseStatus.getErrorMessage());
                    listenerStatus.sendResponseBackToSdc(distributionId, COMPONENT_DONE_ERROR, errorMessage,
                            artifactUrl, SDC_LISTENER_COMPONENT);
                    LOGGER.error(errorMessage);
                } else {
                    LOGGER.info(responseStatus.getMessage());
                    listenerStatus.sendResponseBackToSdc(distributionId, COMPONENT_DONE_OK, null, artifactUrl,
                            SDC_LISTENER_COMPONENT);
                }

            } catch (Exception e) {
                final String errorMessage = format("Failure due to %s", e.getMessage());
                listenerStatus.sendResponseBackToSdc(distributionId, COMPONENT_DONE_ERROR, errorMessage, artifactUrl,
                        SDC_LISTENER_COMPONENT);
                LOGGER.error(errorMessage);
            }
        });
    }

    private BluePrintUploadInput generateBluePrintUploadInputBuilder(File file, String path) throws IOException {
        byte[] bytes = FileUtils.readFileToByteArray(file);
        FileChunk fileChunk = FileChunk.newBuilder().setChunk(ByteString.copyFrom(bytes)).build();
        FileUtil.deleteFile(file, path);
        return BluePrintUploadInput.newBuilder()
                .setCommonHeader(CommonHeader.newBuilder().setRequestId(UUID.randomUUID().toString())
                        .setSubRequestId(UUID.randomUUID().toString()).setOriginatorId("SDC-LISTENER").build())
                .setActionIdentifiers(
                        ActionIdentifiers.newBuilder().setActionName(UploadAction.PUBLISH.toString()).build())
                .setFileChunk(fileChunk).build();
    }

    private String getDistributionId() {
        return sdcListenerDto.getDistributionId();
    }

    private String getArtifactUrl() {
        return sdcListenerDto.getArtifactUrl();
    }

}
