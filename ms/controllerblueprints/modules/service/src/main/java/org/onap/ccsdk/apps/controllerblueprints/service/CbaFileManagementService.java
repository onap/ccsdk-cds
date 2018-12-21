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
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintArchiveUtils;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintFileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * CbaFileManagementService.java Purpose: Provide Service processing CBA file management
 *
 * @author Steve Siani
 * @version 1.0
 */
@Service
public class CbaFileManagementService {
    private static EELFLogger log = EELFManager.getInstance().getLogger(CbaFileManagementService.class);

    @Value("${controllerblueprints.loadCbaExtension}")
    private String cbaExtension;

    private static final String CBA_FILE_NAME_PATTERN = "CBA_{0}_{1}";


    /**
     * cleanupSavedCBA: This method cleanup the Zip file and the unzip directory that was added.
     *
     * @param zipFileName zipFileName
     * @param cbaFileLocation cbaFileLocation
     * @return
     * @throws BluePrintException BluePrintException
     */
    public void cleanupSavedCBA(String zipFileName, Path cbaFileLocation) throws BluePrintException {

        String fileNameWithoutExtension = BluePrintFileUtils.Companion.stripFileExtension(zipFileName);

        //Delete the Zip file from the repository
        FileSystemUtils.deleteRecursively(BluePrintFileUtils.Companion.getBluePrintFile(zipFileName,cbaFileLocation));

        //Delete the CBA directory from the repository
        FileSystemUtils.deleteRecursively(BluePrintFileUtils.Companion.getBluePrintFile(fileNameWithoutExtension,cbaFileLocation));
    }

    /**
     * This is a saveCBAFile method
     * take a {@link FilePart}, transfer it to disk using a Flux of FilePart and return a {@link Mono} representing the CBA file name
     *
     * @param (filePart, targetDirectory) - the request part containing the file to be saved and the default directory where to save
     * @return a {@link Mono} String representing the result of the operation
     * @throws (BluePrintException, IOException) BluePrintException, IOException
     */
    public Mono<String> saveCBAFile(FilePart filePart, Path targetDirectory) throws BluePrintException, IOException {

        // Normalize file name
        final String fileName = StringUtils.cleanPath(filePart.filename());

        // Check if the file's extension is "CBA"
        if(!StringUtils.getFilenameExtension(fileName).equals(cbaExtension)) {
            throw new BluePrintException("Invalid file extension required " + cbaExtension);
        }

        // Change file name to match a pattern
        String changedFileName = BluePrintFileUtils.Companion.getCBAGeneratedFileName(fileName, this.CBA_FILE_NAME_PATTERN);

        // Copy file to the target location (Replacing existing file with the same name)
        Path targetLocation = targetDirectory.resolve(changedFileName);

        // if a file with the same name already exists in a repository, delete and recreate it
        File file = new File(targetLocation.toString());
        if (file.exists())
            file.delete();
        file.createNewFile();

        return filePart.transferTo(file).thenReturn(changedFileName);
    }

    /**
     * Decompress the file into the cbaFileLocation parameter
     * @param zipFileName name of the zipped file
     * @param cbaFileLocation path in which the zipped file will get decompressed
     * @return String the path in which the file is decompressed
     * @throws BluePrintException Exception in the process
     */
    public String decompressCBAFile(final String zipFileName, Path cbaFileLocation) throws BluePrintException {

        File file = BluePrintFileUtils.Companion.getBluePrintFile(zipFileName, cbaFileLocation);
        try {
            Path directoryPath = Files.createDirectories(cbaFileLocation.resolve(BluePrintFileUtils.Companion.stripFileExtension(zipFileName)));
            BluePrintArchiveUtils.Companion.deCompress(file, directoryPath.toString());
            return directoryPath.toString();

        } catch (BluePrintProcessorException | IOException ex) {
            throw new BluePrintException(" Fail to decompress " + zipFileName, ex);
        }

    }


}
