/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package org.onap.ccsdk.cds.cdssdclistener.service;

import static java.nio.file.Files.walk;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.onap.ccsdk.cds.cdssdclistener.client.CdsSdcListenerAuthClientInterceptor;
import org.onap.ccsdk.cds.cdssdclistener.handler.BluePrintProcesssorHandler;
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintUploadInput;
import org.onap.ccsdk.cds.controllerblueprints.management.api.FileChunk;
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
            LOGGER.error("Failed to extract blueprint", e);
        }
    }

    private void storeBluePrint(ZipFile zipFile, String fileName, Path cbaArchivePath, ZipEntry entry) {
        Path targetLocation = cbaArchivePath.resolve(fileName);
        LOGGER.info("The target location is {}", targetLocation);
        File targetZipFile = new File(targetLocation.toString());

        try {
            targetZipFile.createNewFile();

        } catch (IOException e) {
            LOGGER.error("Could not able to create file {}", targetZipFile, e);
        }

        try (InputStream inputStream = zipFile.getInputStream(entry); OutputStream out = new FileOutputStream(
            targetZipFile)) {
            IOUtils.copy(inputStream, out);
            LOGGER.info("Succesfully store the CBA archive {} at this location", targetZipFile);
        } catch (Exception e) {
            LOGGER.error("Failed to put zip file into target location {}, {}", targetLocation, e);
        }
    }

    private Path getStorageDirectory(String path) {
        Path fileStorageLocation = Paths.get(path).toAbsolutePath().normalize();

        if (!fileStorageLocation.toFile().exists()) {
            try {
                return Files.createDirectories(fileStorageLocation);
            } catch (IOException e) {
                LOGGER.error("Fail to create directory", e);
            }
        }
        return fileStorageLocation;
    }

    @Override
    public void saveBluePrintToCdsDatabase(Path cbaArchivePath) {
        try (Stream<Path> fileTree = walk(cbaArchivePath)) {

            // Get the list of files from the path
            List<File> files = fileTree.filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());

            final ManagedChannel managedChannel = getManagedChannel();

            for (File zipFile : files) {
                BluePrintUploadInput request = generateRequest(zipFile);
                Status responseStatus = bluePrintProcesssorHandler.sendRequest(request, managedChannel);

                if (responseStatus.getCode() != SUCCESS_CODE) {
                    LOGGER.error("Failed to store the CBA archive into CDS DB due to {}",
                        responseStatus.getErrorMessage());
                } else {
                    LOGGER.info(responseStatus.getMessage());
                }

                //Delete the file from the local disk.
                boolean fileDeleted = zipFile.delete();

                if (!fileDeleted) {
                    LOGGER.error("Could not able to delete the zip file at this location {}",
                        cbaArchivePath.getFileName());
                }
            }

        } catch (IOException e) {
            LOGGER.error("Failed due to {}", e);
        }
    }

    private ManagedChannel getManagedChannel() {
        return ManagedChannelBuilder.forAddress(grpcAddress, grpcPort)
                                    .usePlaintext()
                                    .intercept(cdsSdcListenerAuthClientInterceptor)
                                    .build();
    }

    private BluePrintUploadInput generateRequest(File file) throws IOException {
        byte[] bytes = FileUtils.readFileToByteArray(file);
        FileChunk fileChunk = FileChunk.newBuilder().setChunk(ByteString.copyFrom(bytes)).build();

        return BluePrintUploadInput.newBuilder()
                .setFileChunk(fileChunk)
                .build();
    }
}
