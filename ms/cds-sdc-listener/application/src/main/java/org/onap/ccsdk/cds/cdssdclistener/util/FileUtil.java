/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package org.onap.ccsdk.cds.cdssdclistener.util;

import static java.nio.file.Files.walk;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    private FileUtil() {

    }

    /**
     * Delete the file.
     *
     * @param file - Given file
     * @param path - Given path
     */
    public static void deleteFile(File file, String path) {
        boolean value = file.delete();
        if (!value) {
            LOGGER.error("Failed to delete the file {} at the location {}", file, path);
        }
    }

    /**
     * Extract files from the given path
     *
     * @param path where files reside.
     * @return list of files.
     */
    public static Optional<List<File>> getFilesFromDisk(Path path) {
        try (Stream<Path> fileTree = walk(path)) {
            // Get the list of files from the path
            return Optional.of(fileTree.filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList()));
        } catch (IOException e) {
            LOGGER.error("Failed to find the file due to", e);
        }
        return Optional.empty();
    }
}
