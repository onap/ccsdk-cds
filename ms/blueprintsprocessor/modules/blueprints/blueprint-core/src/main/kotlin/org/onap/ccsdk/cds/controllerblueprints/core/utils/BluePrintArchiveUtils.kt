/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
 * Modifications Copyright © 2019 Nordix Foundation.
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

package org.onap.ccsdk.cds.controllerblueprints.core.utils

import com.google.common.base.Predicates
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.Predicate
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class BluePrintArchiveUtils {

    companion object {
        private val log = LoggerFactory.getLogger(BluePrintArchiveUtils::class.java)

        /**
         * Create a new Zip from a root directory
         *
         * @param source the base directory
         * @param destination the output filename
         * @return True if OK
         */
        fun compress(source: File, destination: File): Boolean {
            try {
                if (!destination.parentFile.exists()) {
                    destination.parentFile.mkdirs()
                }
                destination.createNewFile()
                val ignoreZipFiles = Predicate<Path> { path -> !path.endsWith(".zip") && !path.endsWith(".ZIP") }
                FileOutputStream(destination).use { out ->
                    compressFolder(source.toPath(), out, pathFilter = ignoreZipFiles)
                }
            } catch (e: Exception) {
                log.error("Fail to compress folder($source) to path(${destination.path})", e)
                return false
            }
            return true
        }

        /**
         * In-memory compress an entire folder.
         */
        fun compressToBytes(baseDir: Path, compressionLevel: Int = Deflater.NO_COMPRESSION): ByteArray {
            return compressFolder(baseDir, ByteArrayOutputStream(), compressionLevel = compressionLevel)
                .toByteArray()
        }

        /**
         * Compress an entire folder.
         *
         * @param baseDir path of base folder to be packaged.
         * @param output the output stream
         * @param pathFilter filter to ignore files based on its path.
         * @param compressionLevel the wanted compression level.
         * @param fixedModificationTime to force every entry to have this modification time.
         * Useful for reproducible operations, like tests, for example.
         */
        private fun <T> compressFolder(
            baseDir: Path,
            output: T,
            pathFilter: Predicate<Path> = Predicates.alwaysTrue(),
            compressionLevel: Int = Deflater.DEFAULT_COMPRESSION,
            fixedModificationTime: Long? = null
        ): T
                where T : OutputStream {
            ZipOutputStream(output)
                .apply { setLevel(compressionLevel) }
                .use { zos ->
                    Files.walkFileTree(baseDir, object : SimpleFileVisitor<Path>() {
                        @Throws(IOException::class)
                        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                            if (pathFilter.test(file)) {
                                val zipEntry = ZipEntry(baseDir.relativize(file).toString())
                                fixedModificationTime?.let {
                                    zipEntry.time = it
                                }
                                zipEntry.time = 0
                                zos.putNextEntry(zipEntry)
                                Files.copy(file, zos)
                                zos.closeEntry()
                            }
                            return FileVisitResult.CONTINUE
                        }

                        @Throws(IOException::class)
                        override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                            val zipEntry = ZipEntry(baseDir.relativize(dir).toString() + "/")
                            fixedModificationTime?.let {
                                zipEntry.time = it
                            }
                            zos.putNextEntry(zipEntry)
                            zos.closeEntry()
                            return FileVisitResult.CONTINUE
                        }
                    })
                }
            return output
        }

        fun deCompress(zipFile: File, targetPath: String): File {
            val zip = ZipFile(zipFile, Charset.defaultCharset())
            val enumeration = zip.entries()
            while (enumeration.hasMoreElements()) {
                val entry = enumeration.nextElement()
                val destFilePath = File(targetPath, entry.name)
                destFilePath.parentFile.mkdirs()

                if (entry.isDirectory)
                    continue

                val bufferedIs = BufferedInputStream(zip.getInputStream(entry))
                bufferedIs.use {
                    destFilePath.outputStream().buffered(1024).use { bos ->
                        bufferedIs.copyTo(bos)
                    }
                }
            }

            val destinationDir = File(targetPath)
            check(destinationDir.isDirectory && destinationDir.exists()) {
                throw BluePrintProcessorException("failed to decompress blueprint(${zipFile.absolutePath}) to ($targetPath) ")
            }

            return destinationDir
        }
    }
}
