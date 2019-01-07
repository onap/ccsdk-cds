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
import java.io.*
import java.nio.charset.Charset
import java.util.zip.ZipFile

class BluePrintArchiveUtils {

    companion object {

        fun getFileContent(fileName: String): String = runBlocking {
            async {
                try {
                    File(fileName).readText(Charsets.UTF_8)
                } catch (e: Exception) {
                    throw BluePrintException("couldn't find file($fileName)")
                }
            }.await()
        }

        fun compress(source: String, destination: String, absolute: Boolean): Boolean {
            val rootDir = File(source)
            val saveFile = File(destination)
            return compress(rootDir, saveFile, absolute)
        }

        /**
         * Create a new Zip from a root directory
         *
         * @param directory the base directory
         * @param filename the output filename
         * @param absolute store absolute filepath (from directory) or only filename
         * @return True if OK
         */
        fun compress(source: File, destination: File, absolute: Boolean): Boolean {
            // recursive call
            val zaos: ZipArchiveOutputStream
            try {
                zaos = ZipArchiveOutputStream(FileOutputStream(destination))
            } catch (e: FileNotFoundException) {
                return false
            }

            try {
                recurseFiles(source, source, zaos, absolute)
            } catch (e2: IOException) {
                try {
                    zaos.close()
                } catch (e: IOException) {
                    // ignore
                }

                return false
            }

            try {
                zaos.finish()
            } catch (e1: IOException) {
                // ignore
            }

            try {
                zaos.flush()
            } catch (e: IOException) {
                // ignore
            }

            try {
                zaos.close()
            } catch (e: IOException) {
                // ignore
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
                for (file2 in files!!) {
                    recurseFiles(root, file2, zaos, absolute)
                }
            } else if (!file.name.endsWith(".zip") && !file.name.endsWith(".ZIP")) {
                var filename: String? = null
                if (absolute) {
                    filename = file.absolutePath.substring(root.absolutePath.length)
                } else {
                    filename = file.name
                }
                val zae = ZipArchiveEntry(filename)
                zae.setSize(file.length())
                zaos.putArchiveEntry(zae)
                val fis = FileInputStream(file)
                IOUtils.copy(fis, zaos)
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

        /**
         * Get the first item in directory
         *
         * @param zipFile
         * @return string
         */
        fun getFirstItemInDirectory(dir: File): String {
            return dir.walk().map { it.name }.elementAt(1)
        }
    }

}