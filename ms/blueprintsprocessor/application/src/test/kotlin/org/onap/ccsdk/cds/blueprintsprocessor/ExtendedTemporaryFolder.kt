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
package org.onap.ccsdk.cds.blueprintsprocessor

import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import javax.annotation.PreDestroy

class ExtendedTemporaryFolder {
    private val tempFolder = TemporaryFolder()

    init {
        tempFolder.create()
    }

    @PreDestroy
    fun delete() = tempFolder.delete()

    /**
     * A delegate to org.junit.rules.TemporaryFolder.TemporaryFolder.newFolder(String).
     */
    fun newFolder(folder: String): File = tempFolder.newFolder(folder)

    /**
     * Delete all files under the root temporary folder recursively. The folders are preserved.
     */
    fun deleteAllFiles() {
        Files.walkFileTree(tempFolder.root.toPath(), object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                file?.toFile()?.delete()
                return FileVisitResult.CONTINUE
            }
        })
    }
}
