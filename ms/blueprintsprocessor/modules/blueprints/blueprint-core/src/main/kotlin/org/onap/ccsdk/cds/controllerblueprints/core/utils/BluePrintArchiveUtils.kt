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
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.ArchiveOutputStream
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.Enumeration
import java.util.function.Predicate
import java.util.zip.Deflater

enum class ArchiveType {
    TarGz,
    Zip
}

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
        fun compress(source: File, destination: File, archiveType: ArchiveType = ArchiveType.Zip): Boolean {
            try {
                if (!destination.parentFile.exists()) {
                    destination.parentFile.mkdirs()
                }
                destination.createNewFile()
                val ignoreZipFiles = Predicate<Path> { path -> !path.endsWith(".zip") && !path.endsWith(".ZIP") }
                FileOutputStream(destination).use { out ->
                    compressFolder(source.toPath(), out, archiveType, pathFilter = ignoreZipFiles)
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
        fun compressToBytes(
            baseDir: Path,
            archiveType: ArchiveType = ArchiveType.Zip,
            compressionLevel: Int = Deflater.NO_COMPRESSION
        ): ByteArray {
            return compressFolder(baseDir, ByteArrayOutputStream(), archiveType, compressionLevel = compressionLevel)
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
            archiveType: ArchiveType,
            pathFilter: Predicate<Path> = Predicates.alwaysTrue(),
            compressionLevel: Int = Deflater.DEFAULT_COMPRESSION,
            fixedModificationTime: Long? = null
        ): T
            where T : OutputStream {
            val stream: ArchiveOutputStream = if (archiveType == ArchiveType.Zip)
                ZipArchiveOutputStream(output).apply { setLevel(compressionLevel) }
            else
                TarArchiveOutputStream(GzipCompressorOutputStream(output))
            stream
                .use { aos ->
                    Files.walkFileTree(
                        baseDir,
                        object : SimpleFileVisitor<Path>() {
                            @Throws(IOException::class)
                            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                                if (pathFilter.test(file)) {
                                    var archiveEntry: ArchiveEntry = aos.createArchiveEntry(
                                        file.toFile(),
                                        baseDir.relativize(file).toString()
                                    )
                                    if (archiveType == ArchiveType.Zip) {
                                        val entry = archiveEntry as ZipArchiveEntry
                                        fixedModificationTime?.let {
                                            entry.time = it
                                        }
                                        entry.time = 0
                                    }
                                    aos.putArchiveEntry(archiveEntry)
                                    Files.copy(file, aos)
                                    aos.closeArchiveEntry()
                                }
                                return FileVisitResult.CONTINUE
                            }

                            @Throws(IOException::class)
                            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                                var archiveEntry: ArchiveEntry?
                                if (archiveType == ArchiveType.Zip) {
                                    val entry = ZipArchiveEntry(baseDir.relativize(dir).toString() + "/")
                                    fixedModificationTime?.let {
                                        entry.time = it
                                    }
                                    archiveEntry = entry
                                } else
                                    archiveEntry = TarArchiveEntry(baseDir.relativize(dir).toString() + "/")
                                aos.putArchiveEntry(archiveEntry)
                                aos.closeArchiveEntry()
                                return FileVisitResult.CONTINUE
                            }
                        }
                    )
                }
            return output
        }

        private fun getDefaultEncoding(): String? {
            val bytes = byteArrayOf('D'.toByte())
            val inputStream: InputStream = ByteArrayInputStream(bytes)
            val reader = InputStreamReader(inputStream)
            return reader.encoding
        }

        fun deCompress(archiveFile: File, targetPath: String, archiveType: ArchiveType = ArchiveType.Zip): File {
            var enumeration: ArchiveEnumerator? = null
            if (archiveType == ArchiveType.Zip) {
                val zipArchive = ZipFile(archiveFile, getDefaultEncoding())
                enumeration = ArchiveEnumerator(zipArchive)
            } else { // Tar Gz
                var tarGzArchiveIs: InputStream = BufferedInputStream(archiveFile.inputStream())
                tarGzArchiveIs = GzipCompressorInputStream(tarGzArchiveIs)
                val tarGzArchive: ArchiveInputStream = TarArchiveInputStream(tarGzArchiveIs)
                enumeration = ArchiveEnumerator(tarGzArchive)
            }

            enumeration.use {
                while (enumeration!!.hasMoreElements()) {
                    val entry: ArchiveEntry? = enumeration.nextElement()
                    val destFilePath = File(targetPath, entry!!.name)
                    destFilePath.parentFile.mkdirs()

                    if (entry!!.isDirectory)
                        continue

                    val bufferedIs = BufferedInputStream(enumeration.getInputStream(entry))
                    destFilePath.outputStream().buffered(1024).use { bos ->
                        bufferedIs.copyTo(bos)
                    }

                    if (!enumeration.getHasSharedEntryInputStream())
                        bufferedIs.close()
                }
            }

            val destinationDir = File(targetPath)
            check(destinationDir.isDirectory && destinationDir.exists()) {
                throw BluePrintProcessorException("failed to decompress blueprint(${archiveFile.absolutePath}) to ($targetPath) ")
            }

            return destinationDir
        }
    }

    class ArchiveEnumerator : Enumeration<ArchiveEntry>, Closeable {

        private val zipArchive: ZipFile?
        private val zipEnumeration: Enumeration<ZipArchiveEntry>?
        private val archiveStream: ArchiveInputStream?
        private var nextEntry: ArchiveEntry? = null
        private val hasSharedEntryInputStream: Boolean

        constructor(zipFile: ZipFile) {
            zipArchive = zipFile
            zipEnumeration = zipFile.entries
            archiveStream = null
            hasSharedEntryInputStream = false
        }

        constructor(archiveStream: ArchiveInputStream) {
            this.archiveStream = archiveStream
            zipArchive = null
            zipEnumeration = null
            hasSharedEntryInputStream = true
        }

        fun getHasSharedEntryInputStream(): Boolean {
            return hasSharedEntryInputStream
        }

        fun getInputStream(entry: ArchiveEntry): InputStream? {
            return if (zipArchive != null)
                zipArchive?.getInputStream(entry as ZipArchiveEntry?)
            else
                archiveStream
        }

        override fun hasMoreElements(): Boolean {
            if (zipEnumeration != null)
                return zipEnumeration?.hasMoreElements()
            else if (archiveStream != null) {
                nextEntry = archiveStream.nextEntry
                if (nextEntry != null && !archiveStream.canReadEntryData(nextEntry))
                    return hasMoreElements()
                return nextEntry != null
            }
            return false
        }

        override fun nextElement(): ArchiveEntry? {
            if (zipEnumeration != null)
                nextEntry = zipEnumeration.nextElement()
            else if (archiveStream != null) {
                if (nextEntry == null)
                    nextEntry = archiveStream.nextEntry
            }
            return nextEntry
        }

        override fun close() {
            if (zipArchive != null)
                zipArchive.close()
            else archiveStream?.close()
        }
    }
}
