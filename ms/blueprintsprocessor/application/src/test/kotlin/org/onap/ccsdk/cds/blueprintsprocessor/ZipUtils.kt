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

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


fun zipFolder(baseDir: Path): ByteArray {
    val baos = ByteArrayOutputStream()
    ZipOutputStream(baos)
            // save processing time
            .apply { setLevel(Deflater.NO_COMPRESSION) }
            .use { zos ->
                Files.walkFileTree(baseDir, object : SimpleFileVisitor<Path>() {
                    @Throws(IOException::class)
                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                        zos.putNextEntry(ZipEntry(baseDir.relativize(file).toString()))
                        Files.copy(file, zos)
                        zos.closeEntry()
                        return FileVisitResult.CONTINUE
                    }

                    @Throws(IOException::class)
                    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                        zos.putNextEntry(ZipEntry(baseDir.relativize(dir).toString() + "/"))
                        zos.closeEntry()
                        return FileVisitResult.CONTINUE
                    }
                })
            }
    return baos.toByteArray()
}
