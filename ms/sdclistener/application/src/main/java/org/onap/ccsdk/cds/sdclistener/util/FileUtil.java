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

package org.onap.ccsdk.cds.sdclistener.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.walk;

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
     *
     * @return list of files.
     */
    public static List<File> getFilesFromDisk(Path path) {

        try (Stream<Path> fileTree = walk(path)) {
            // Get the list of files from the path
            return fileTree.filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Failed to find the file due to", e);
        }
        return new ArrayList<>();
    }

}
