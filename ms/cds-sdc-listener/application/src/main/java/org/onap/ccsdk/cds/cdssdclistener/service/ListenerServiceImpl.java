/*
 * Copyright © 2017-2019 AT&T, Bell Canada
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

package org.onap.ccsdk.cds.cdssdclistener.service;

import static java.nio.file.Files.walk;
import static org.onap.sdc.utils.DistributionStatusEnum.COMPONENT_DONE_ERROR;
import static org.onap.sdc.utils.DistributionStatusEnum.COMPONENT_DONE_OK;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
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
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.onap.ccsdk.cds.cdssdclistener.client.CdsSdcListenerAuthClientInterceptor;
import org.onap.ccsdk.cds.cdssdclistener.dto.CdsSdcListenerDto;
import org.onap.ccsdk.cds.cdssdclistener.handler.BluePrintProcesssorHandler;
import org.onap.ccsdk.cds.cdssdclistener.status.CdsSdcListenerStatus;
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintUploadInput;
import org.onap.ccsdk.cds.controllerblueprints.management.api.FileChunk;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("listenerservice")
public class ListenerServiceImpl implements ListenerService {

    @Autowired
    private BluePrintProcesssorHandler bluePrintProcesssorHandler;

    @Autowired
    private CdsSdcListenerAuthClientInterceptor cdsSdcListenerAuthClientInterceptor;

    @Autowired
    private CdsSdcListenerStatus listenerStatus;

    @Autowired
    private CdsSdcListenerDto cdsSdcListenerDto;

    @Value("${listenerservice.config.grpcAddress}")
    private String grpcAddress;

    @Value("${listenerservice.config.grpcPort}")
    private int grpcPort;

    private static final String CBA_ZIP_PATH = "Artifacts/Resources/[a-zA-Z0-9-_]+/Deployment/CONTROLLER_BLUEPRINT_ARCHIVE/[a-zA-Z0-9-_]+[.]zip";
    private static final int SUCCESS_CODE = 200;
    private static final Logger LOGGER = LoggerFactory.getLogger(ListenerServiceImpl.class);

    @Override
    public void extractBluePrint(String csarArchivePath, String cbaArchivePath) {
        Path cbaStorageDir = getStorageDirectory(cbaArchivePath);
        try (ZipFile zipFile = new ZipFile(csarArchivePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String fileName = entry.getName();
                if (Pattern.matches(CBA_ZIP_PATH, fileName)) {
                    final String cbaArchiveName = Paths.get(fileName).getFileName().toString();
                    LOGGER.info("Storing the CBA archive {}", cbaArchiveName);
                    storeBluePrint(zipFile, cbaArchiveName, cbaStorageDir, entry);
                }
            }

        } catch (Exception e) {
            LOGGER.error("Failed to extract blueprint {}", e);
        }
    }

    private void storeBluePrint(ZipFile zipFile, String fileName, Path cbaArchivePath, ZipEntry entry) {
        Path targetLocation = cbaArchivePath.resolve(fileName);
        LOGGER.info("The target location for zip file is {}", targetLocation);
        File targetZipFile = new File(targetLocation.toString());

        try {
            targetZipFile.createNewFile();
        } catch (IOException e) {
            LOGGER.error("Could not able to create file {}", targetZipFile, e);
        }

        try (InputStream inputStream = zipFile.getInputStream(entry); OutputStream out = new FileOutputStream(
            targetZipFile)) {
            IOUtils.copy(inputStream, out);
            LOGGER.info("Successfully store the CBA archive {} at this location", targetZipFile);
        } catch (Exception e) {
            LOGGER.error("Failed to put zip file into target location {}, {}", targetLocation, e);
        }
    }

    @Override
    public void saveBluePrintToCdsDatabase(Path cbaArchivePath, ManagedChannel channel) {
        Optional<List<File>> zipFiles = getFilesFromDisk(cbaArchivePath);
        zipFiles.ifPresent(files -> prepareRequestForCdsBackend(files, channel));
    }

    @Override
    public void extractCsarAndStore(IDistributionClientDownloadResult result, String csarArchivePath) {

        // Create CSAR storage directory
        Path csarStorageDir = getStorageDirectory(csarArchivePath);
        byte[] payload = result.getArtifactPayload();
        String csarFileName = result.getArtifactFilename();
        Path targetLocation = csarStorageDir.resolve(csarFileName);

        LOGGER.info("The target location for the CSAR file is {}", targetLocation);

        File targetCsarFile = new File(targetLocation.toString());

        try (FileOutputStream outFile = new FileOutputStream(targetCsarFile)) {
            outFile.write(payload, 0, payload.length);
        } catch (Exception e) {
            LOGGER.error("Failed to put CSAR file into target location {}, {}", targetLocation, e);
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

    private void prepareRequestForCdsBackend(List<File> files, ManagedChannel managedChannel) {
        final String distributionId = cdsSdcListenerDto.getDistributionId();

        files.forEach(zipFile -> {
            try {
                final BluePrintUploadInput request = generateBluePrintUploadInputBuilder(zipFile);

                // Send request to CDS Backend.
                final Status responseStatus = bluePrintProcesssorHandler.sendRequest(request, managedChannel);

                if (responseStatus.getCode() != SUCCESS_CODE) {
                    final String errorMessage = String.format("Failed to store the CBA archive into CDS DB due to %s",
                        responseStatus.getErrorMessage());
                    listenerStatus.sendResponseStatusBackToSDC(distributionId,
                        COMPONENT_DONE_ERROR, errorMessage);
                    LOGGER.error(errorMessage);

                } else {
                    LOGGER.info(responseStatus.getMessage());
                    listenerStatus.sendResponseStatusBackToSDC(distributionId,
                        COMPONENT_DONE_OK, null);
                }

            } catch (Exception e) {
                final String errorMessage = String.format("Failure due to %s", e.getMessage());
                listenerStatus.sendResponseStatusBackToSDC(distributionId, COMPONENT_DONE_ERROR, errorMessage);
                LOGGER.error("Failure due to {}", e);
            } finally {
                //Delete the file from the local disk.
                boolean fileDeleted = zipFile.delete();

                if (!fileDeleted) {
                    LOGGER.error("Could not able to delete the zip file {}", zipFile);
                }
            }
        });
    }

    private BluePrintUploadInput generateBluePrintUploadInputBuilder(File file) throws IOException {
        byte[] bytes = FileUtils.readFileToByteArray(file);
        FileChunk fileChunk = FileChunk.newBuilder().setChunk(ByteString.copyFrom(bytes)).build();

        return BluePrintUploadInput.newBuilder()
                .setFileChunk(fileChunk)
                .build();
    }

    /**
     * Extract files from the given path
     *
     * @param path where files reside.
     * @return list of files.
     */
    public Optional<List<File>> getFilesFromDisk(Path path) {
        try (Stream<Path> fileTree = walk(path)) {
            // Get the list of files from the path
            return Optional.of(fileTree.filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList()));
        } catch (IOException e) {
            LOGGER.error("Failed to find the file due to {}", e);
        }
        return Optional.empty();
    }
}
