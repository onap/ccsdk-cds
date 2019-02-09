/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
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

package org.onap.ccsdk.apps.controllerblueprints.core.utils

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.IOUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.Charset
import java.util.zip.ZipFile

class BluePrintArchiveUtils {

    companion object {
        private val log = LoggerFactory.getLogger(BluePrintArchiveUtils::class.java)

        /**
         * Create a new Zip from a root directory
         *
         * @param source the base directory
         * @param destination the output filename
         * @param absolute store absolute filepath (from directory) or only filename
         * @return True if OK
         */
        fun compress(source: File, destination: File, absolute: Boolean): Boolean {
            try {
                destination.createNewFile()
                ZipArchiveOutputStream(destination).use {
                    recurseFiles(source, source, it, absolute)
                }
            } catch (e: Exception) {
                log.error("Fail to compress folder($source) to path(${destination.path}", e)
                return false
            }
            return true
        }

        /**
         * Recursive traversal to add files
         *
         * @param root
         * @param file
         * @param zaos
         * @param absolute
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun recurseFiles(root: File, file: File, zaos: ZipArchiveOutputStream,
                                 absolute: Boolean) {
            if (file.isDirectory) {
                // recursive call
                val files = file.listFiles()
                for (fileChild in files!!) {
                    recurseFiles(root, fileChild, zaos, absolute)
                }
            } else if (!file.name.endsWith(".zip") && !file.name.endsWith(".ZIP")) {
                val filename = if (absolute) {
                    file.absolutePath.substring(root.absolutePath.length)
                } else {
                    file.name
                }
                val zae = ZipArchiveEntry(filename)
                zae.size = file.length()
                zaos.putArchiveEntry(zae)
                FileInputStream(file).use {
                    IOUtils.copy(it, zaos)
                    it.close()
                }
                zaos.closeArchiveEntry()
            }
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