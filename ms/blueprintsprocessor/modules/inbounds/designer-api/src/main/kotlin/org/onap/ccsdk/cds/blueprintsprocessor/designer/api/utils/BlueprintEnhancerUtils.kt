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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.utils

import kotlinx.coroutines.reactive.awaitSingle
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipType
import org.onap.ccsdk.cds.controllerblueprints.core.deCompress
import org.onap.ccsdk.cds.controllerblueprints.core.deleteNBDir
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintRepoService
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.reCreateNBDirs
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintArchiveUtils
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import java.io.File
import java.nio.file.Paths

class BlueprintEnhancerUtils {
    companion object {

        val log = logger(BlueprintEnhancerUtils)

        fun populateDataTypes(
            bluePrintContext: BlueprintContext,
            bluePrintRepoService: BlueprintRepoService,
            dataTypeName: String
        ): DataType {
            val dataType = bluePrintContext.serviceTemplate.dataTypes?.get(dataTypeName)
                ?: bluePrintRepoService.getDataType(dataTypeName)
                ?: throw BlueprintException("couldn't get DataType($dataTypeName) from repo.")
            bluePrintContext.serviceTemplate.dataTypes?.put(dataTypeName, dataType)
            return dataType
        }

        fun populateRelationshipType(
            bluePrintContext: BlueprintContext,
            bluePrintRepoService: BlueprintRepoService,
            relationshipName: String
        ): RelationshipType {

            val relationshipType = bluePrintContext.serviceTemplate.relationshipTypes?.get(relationshipName)
                ?: bluePrintRepoService.getRelationshipType(relationshipName)
                ?: throw BlueprintException("couldn't get RelationshipType($relationshipName) from repo.")
            bluePrintContext.serviceTemplate.relationshipTypes?.put(relationshipName, relationshipType)
            return relationshipType
        }

        fun populateNodeType(
            bluePrintContext: BlueprintContext,
            bluePrintRepoService: BlueprintRepoService,
            nodeTypeName: String
        ): NodeType {

            val nodeType = bluePrintContext.serviceTemplate.nodeTypes?.get(nodeTypeName)
                ?: bluePrintRepoService.getNodeType(nodeTypeName)
                ?: throw BlueprintException("couldn't get NodeType($nodeTypeName) from repo.")
            bluePrintContext.serviceTemplate.nodeTypes?.put(nodeTypeName, nodeType)
            return nodeType
        }

        fun populateArtifactType(
            bluePrintContext: BlueprintContext,
            bluePrintRepoService: BlueprintRepoService,
            artifactTypeName: String
        ): ArtifactType {

            val artifactType = bluePrintContext.serviceTemplate.artifactTypes?.get(artifactTypeName)
                ?: bluePrintRepoService.getArtifactType(artifactTypeName)
                ?: throw BlueprintException("couldn't get ArtifactType($artifactTypeName) from repo.")
            bluePrintContext.serviceTemplate.artifactTypes?.put(artifactTypeName, artifactType)
            return artifactType
        }

        suspend fun byteArrayAsFile(byteArray: ByteArray, targetFile: File): File {
            // Recreate Folder
            targetFile.parentFile.reCreateNBDirs()
            targetFile.writeBytes(byteArray).apply {
                log.info("CBA file(${targetFile.absolutePath} written successfully")
            }
            return targetFile
        }

        suspend fun filePartAsFile(filePart: FilePart, targetFile: File): File {
            // Delete the Directory
            targetFile.parentFile.reCreateNBDirs()
            return filePart.transferTo(targetFile)
                .thenReturn(targetFile)
                .awaitSingle()
        }

        private suspend fun byteArrayAsArchiveFile(byteArray: ByteArray, archiveDir: String, enhanceDir: String): File {
            // Recreate the Base Directories
            normalizedFile(archiveDir).reCreateNBDirs()
            normalizedFile(enhanceDir).reCreateNBDirs()
            val archiveFile = normalizedFile(archiveDir, "cba.zip")
            // Copy the File Part to ZIP
            return byteArrayAsFile(byteArray, archiveFile)
        }

        private suspend fun filePartAsArchiveFile(filePart: FilePart, archiveDir: String, enhanceDir: String): File {
            // Recreate the Base Directories
            normalizedFile(archiveDir).reCreateNBDirs()
            normalizedFile(enhanceDir).reCreateNBDirs()
            val archiveFile = normalizedFile(archiveDir, "cba.zip")
            // Copy the File Part to ZIP
            return filePartAsFile(filePart, archiveFile)
        }

        /** copy the [byteArray] zip file to [archiveDir] and then decompress to [enhanceDir] */
        suspend fun copyByteArrayToEnhanceDir(byteArray: ByteArray, archiveDir: String, enhanceDir: String): File {
            val archiveFile = byteArrayAsArchiveFile(byteArray, archiveDir, enhanceDir)
            val deCompressFileName = normalizedPathName(enhanceDir)
            return archiveFile.deCompress(deCompressFileName)
        }

        /** copy the [filePart] zip file to [archiveDir] and then decompress to [enhanceDir] */
        suspend fun copyFilePartToEnhanceDir(filePart: FilePart, archiveDir: String, enhanceDir: String): File {
            val filePartFile = filePartAsArchiveFile(filePart, archiveDir, enhanceDir)
            val deCompressFileName = normalizedPathName(enhanceDir)
            return filePartFile.deCompress(deCompressFileName)
        }

        /** compress [enhanceDir] to [archiveDir] and return ByteArray */
        suspend fun compressEnhanceDirAndReturnByteArray(
            enhanceDir: String,
            archiveDir: String,
            outputFileName: String = "enhanced-cba.zip"
        ): ByteArray {
            val compressedFile = normalizedFile(archiveDir, outputFileName)
            BlueprintArchiveUtils.compress(Paths.get(enhanceDir).toFile(), compressedFile)
            return compressedFile.readBytes()
        }

        /** compress [enhanceDir] to [archiveDir] and return ResponseEntity */
        suspend fun compressEnhanceDirAndReturnFilePart(
            enhanceDir: String,
            archiveDir: String,
            outputFileName: String = "enhanced-cba.zip"
        ):
            ResponseEntity<Resource> {
                val compressedFile = normalizedFile(archiveDir, outputFileName)
                BlueprintArchiveUtils.compress(Paths.get(enhanceDir).toFile(), compressedFile)
                return prepareResourceEntity(compressedFile)
            }

        /** convert [file] to ResourceEntity */
        suspend fun prepareResourceEntity(file: File): ResponseEntity<Resource> {
            return prepareResourceEntity(file.name, file.readBytes())
        }

        /** convert [byteArray] to ResourceEntity with [fileName]*/
        fun prepareResourceEntity(fileName: String, byteArray: ByteArray): ResponseEntity<Resource> {
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/plain"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
                .body(ByteArrayResource(byteArray))
        }

        suspend fun cleanEnhancer(archiveLocation: String, enhancementLocation: String) {
            deleteNBDir(archiveLocation)
            deleteNBDir(enhancementLocation)
        }
    }
}
