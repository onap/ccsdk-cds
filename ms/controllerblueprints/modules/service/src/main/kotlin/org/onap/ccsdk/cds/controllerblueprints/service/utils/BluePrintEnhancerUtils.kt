/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
 * Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.service.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.data.*
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintRepoService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintArchiveUtils
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.util.StringUtils
import reactor.core.publisher.Mono
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*


class BluePrintEnhancerUtils {
    companion object {

        fun populateDataTypes(bluePrintContext: BluePrintContext, bluePrintRepoService: BluePrintRepoService,
                              dataTypeName: String): DataType {
            val dataType = bluePrintContext.serviceTemplate.dataTypes?.get(dataTypeName)
                    ?: bluePrintRepoService.getDataType(dataTypeName)
                    ?: throw BluePrintException("couldn't get DataType($dataTypeName) from repo.")
            bluePrintContext.serviceTemplate.dataTypes?.put(dataTypeName, dataType)
            return dataType
        }

        fun populateRelationshipType(bluePrintContext: BluePrintContext, bluePrintRepoService: BluePrintRepoService,
                                     relationshipName: String): RelationshipType {

            val relationshipType = bluePrintContext.serviceTemplate.relationshipTypes?.get(relationshipName)
                    ?: bluePrintRepoService.getRelationshipType(relationshipName)
                    ?: throw BluePrintException("couldn't get RelationshipType($relationshipName) from repo.")
            bluePrintContext.serviceTemplate.relationshipTypes?.put(relationshipName, relationshipType)
            return relationshipType
        }


        fun populateNodeType(bluePrintContext: BluePrintContext, bluePrintRepoService: BluePrintRepoService,
                             nodeTypeName: String): NodeType {

            val nodeType = bluePrintContext.serviceTemplate.nodeTypes?.get(nodeTypeName)
                    ?: bluePrintRepoService.getNodeType(nodeTypeName)
                    ?: throw BluePrintException("couldn't get NodeType($nodeTypeName) from repo.")
            bluePrintContext.serviceTemplate.nodeTypes?.put(nodeTypeName, nodeType)
            return nodeType
        }

        fun populateArtifactType(bluePrintContext: BluePrintContext, bluePrintRepoService: BluePrintRepoService,
                                 artifactTypeName: String): ArtifactType {

            val artifactType = bluePrintContext.serviceTemplate.artifactTypes?.get(artifactTypeName)
                    ?: bluePrintRepoService.getArtifactType(artifactTypeName)
                    ?: throw BluePrintException("couldn't get ArtifactType($artifactTypeName) from repo.")
            bluePrintContext.serviceTemplate.artifactTypes?.put(artifactTypeName, artifactType)
            return artifactType
        }

        private suspend fun copyFromFilePart(filePart: FilePart, targetFile: File): File {
            // Delete the Directory
            targetFile.deleteRecursively()
            return filePart.transferTo(targetFile)
                    .thenReturn(targetFile)
                    .awaitSingle()
        }

        suspend fun extractCompressFilePart(filePart: FilePart, archiveDir: String, enhanceDir: String): File {
            //Recreate the Base Directories
            normalizedFile(archiveDir).reCreateDirs()
            normalizedFile(enhanceDir).reCreateDirs()
            val filePartFile = normalizedFile(archiveDir, "cba.zip")
            // Copy the File Part to ZIP
            return copyFromFilePart(filePart, filePartFile)
        }

        suspend fun decompressFilePart(filePart: FilePart, archiveDir: String, enhanceDir: String): File {
            val filePartFile = extractCompressFilePart(filePart, archiveDir, enhanceDir)
            val deCompressFileName = normalizedPathName(enhanceDir)
            return filePartFile.deCompress(deCompressFileName)
        }

        suspend fun compressToFilePart(enhanceDir: String, archiveDir: String): ResponseEntity<Resource> {
            val compressedFile = normalizedFile(archiveDir, "enhanced-cba.zip")
            BluePrintArchiveUtils.compress(Paths.get(enhanceDir).toFile(), compressedFile, true)
            return prepareResourceEntity(compressedFile.name, compressedFile.readBytes())
        }

        suspend fun prepareResourceEntity(fileName: String, file: ByteArray): ResponseEntity<Resource> {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/plain"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
                    .body(ByteArrayResource(file))
        }

        suspend fun cleanEnhancer(archiveLocation: String, enhancementLocation: String) = withContext(Dispatchers.Default) {
            deleteDir(archiveLocation)
            deleteDir(enhancementLocation)
        }

        /**
         * This is a saveCBAFile method
         * take a [FilePart], transfer it to disk using a Flux of FilePart and return a [Mono] representing the CBA file name
         *
         * @param (filePart, targetDirectory) - the request part containing the file to be saved and the default directory where to save
         * @return a [Mono] String representing the result of the operation
         * @throws (BluePrintException, IOException) BluePrintException, IOException
         */
        @Throws(BluePrintException::class, IOException::class)
        fun saveCBAFile(filePart: FilePart, targetDirectory: Path): Mono<String> {

            // Normalize file name
            val fileName = StringUtils.cleanPath(filePart.filename())

            // Check if the file's extension is "CBA"
            if (StringUtils.getFilenameExtension(fileName) != "zip") {
                throw BluePrintException(ErrorCode.INVALID_FILE_EXTENSION.value, "Invalid file extension required ZIP")
            }

            // Change file name to match a pattern
            val changedFileName = UUID.randomUUID().toString() + ".zip"
            //String changedFileName = BluePrintFileUtils.Companion.getCBAGeneratedFileName(fileName, this.CBA_FILE_NAME_PATTERN);

            // Copy file to the target location (Replacing existing file with the same name)
            val targetLocation = targetDirectory.resolve(changedFileName)

            // if a file with the same name already exists in a repository, delete and recreate it
            val file = File(targetLocation.toString())
            if (file.exists())
                file.delete()
            file.createNewFile()

            return filePart.transferTo(file).thenReturn(changedFileName)
        }
    }
}
