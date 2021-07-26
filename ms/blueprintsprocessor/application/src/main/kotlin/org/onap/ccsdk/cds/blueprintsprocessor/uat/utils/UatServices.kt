/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.cds.blueprintsprocessor.uat.utils

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.LogColor.COLOR_SERVICES
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.LogColor.resetContextColor
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.LogColor.setContextColor
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants.UAT_SPECIFICATION_FILE
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.io.File
import java.util.zip.ZipFile

/**
 * Supporting services to help creating UAT specifications.
 *
 * @author Eliezio Oliveira
 */
@RestController
@RequestMapping("/api/v1/uat")
@Profile("uat")
open class UatServices(private val uatExecutor: UatExecutor, private val mapper: ObjectMapper) {

    @PostMapping("/verify", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('USER')")
    @Suppress("BlockingMethodInNonBlockingContext")
    open fun verify(@RequestPart("cba") cbaFile: FilePart) = runBlocking {
        setContextColor(COLOR_SERVICES)
        val tempFile = createTempFile()
        try {
            cbaFile.transferTo(tempFile)
            val uatSpec = readZipEntryAsText(tempFile, UAT_SPECIFICATION_FILE)
            val cbaBytes = tempFile.readBytes()
            uatExecutor.execute(uatSpec, cbaBytes)
        } catch (e: AssertionError) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        } catch (t: Throwable) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, t.message, t)
        } finally {
            tempFile.delete()
            resetContextColor()
        }
    }

    @PostMapping("/spy", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = ["text/vnd.yaml"])
    @PreAuthorize("hasRole('USER')")
    @Suppress("BlockingMethodInNonBlockingContext")
    open fun spy(
        @RequestPart("cba") cbaFile: FilePart,
        @RequestPart("uat", required = false) uatFile: FilePart?
    ): String = runBlocking {
        val tempFile = createTempFile()
        setContextColor(COLOR_SERVICES)
        try {
            cbaFile.transferTo(tempFile)
            val uatSpec = when {
                uatFile != null -> uatFile.readText()
                else -> readZipEntryAsText(tempFile, UAT_SPECIFICATION_FILE)
            }
            val uat = UatDefinition.load(mapper, uatSpec)
            val cbaBytes = tempFile.readBytes()
            val updatedUat = uatExecutor.execute(uat, cbaBytes)
            return@runBlocking updatedUat.dump(
                mapper,
                FIELDS_TO_EXCLUDE
            )
        } catch (t: Throwable) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, t.message, t)
        } finally {
            tempFile.delete()
            resetContextColor()
        }
    }

    private fun FilePart.readText(): String {
        val tempFile = createTempFile()
        try {
            transferTo(tempFile).block()
            return tempFile.readText()
        } finally {
            tempFile.delete()
        }
    }

    @Suppress("SameParameterValue")
    private fun readZipEntryAsText(file: File, entryName: String): String {
        return ZipFile(file).use { zipFile -> zipFile.readEntryAsText(entryName) }
    }

    private fun ZipFile.readEntryAsText(entryName: String): String {
        val zipEntry = getEntry(entryName)
        return getInputStream(zipEntry).readBytes().toString(Charsets.UTF_8)
    }

    companion object {

        // Fields that can be safely ignored from BPP response, and can be omitted on the UAT specification.
        private val FIELDS_TO_EXCLUDE = listOf("timestamp")
    }
}
