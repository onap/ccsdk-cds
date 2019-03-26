/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package org.onap.ccsdk.cds.cdssdclistener.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("listenerservice")
public class ListenerServiceImpl implements ListenerService {

    @Value("${listenerservice.config.csarArchive}")
    private String csarArchivePath;

    @Value("${listenerservice.config.cbaArchive}")
    private String cbaArchivePath;

    private static final String CBA_ZIP_PATH = "Artifacts/Resources/[a-zA-Z0-9-_]+/Deployment/CONTROLLER_BLUEPRINT_ARCHIVE/[a-zA-Z0-9-_]+[.]zip";
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
                    storeBluePrint(zipFile, cbaArchiveName, cbaStorageDir, entry);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to extract blueprint", e);
        }
    }

    private void storeBluePrint(ZipFile zipFile, String fileName, Path cbaArchivePath, ZipEntry entry) {
        final String changedFileName = fileName + ".zip";
        Path targetLocation = cbaArchivePath.resolve(changedFileName);
        File targetZipFile = new File(targetLocation.toString());

        try {
            targetZipFile.createNewFile();

        } catch (IOException e) {
            LOGGER.error("Could not able to create file {}", targetZipFile, e);
        }

        try (InputStream inputStream = zipFile.getInputStream(entry); OutputStream out = new FileOutputStream(
            targetZipFile)) {
            IOUtils.copy(inputStream, out);
        } catch (Exception e) {
            LOGGER.error("Failed to put zip file into target location {}", targetLocation, e);
        }
    }

    private Path getStorageDirectory(String path) {
        Path fileStorageLocation = Paths.get(path).toAbsolutePath().normalize();

        if (!Files.exists(fileStorageLocation)) {
            try {
                return Files.createDirectories(fileStorageLocation);
            } catch (IOException e) {
                LOGGER.error("Fail to create directory", e);
            }
        }
        return fileStorageLocation;
    }

    @Override
    public void saveBluePrintToCdsDatabase(ZipFile file) {
        //TODO
    }
}
