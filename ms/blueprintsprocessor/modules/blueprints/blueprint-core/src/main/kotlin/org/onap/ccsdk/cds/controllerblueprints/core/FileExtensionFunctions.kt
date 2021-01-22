/*
 *  Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.controllerblueprints.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintArchiveUtils
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths

fun InputStream.toFile(path: String): File {
    val file = File(path)
    file.outputStream().use { this.copyTo(it) }
    return file
}

fun File.reCreateDirs(): File {
    if (this.exists()) {
        this.deleteRecursively()
    }
    // this.mkdirs()
    FileUtils.forceMkdir(this)
    check(this.exists()) {
        throw BlueprintException("failed to re create dir(${this.absolutePath})")
    }
    return this
}

fun File.compress(targetZipFileName: String): File {
    return this.compress(Paths.get(targetZipFileName).toFile())
}

/**
 * Compress the current Dir to the target zip file and return the target zip file
 */
fun File.compress(targetZipFile: File): File {
    BlueprintArchiveUtils.compress(this, targetZipFile)
    return targetZipFile
}

fun File.deCompress(targetFileName: String): File {
    return this.deCompress(Paths.get(targetFileName).toFile())
}

/**
 * De-Compress the current zip file to the target file and return the target file
 */
fun File.deCompress(targetFile: File): File {
    BlueprintArchiveUtils.deCompress(this, targetFile.path)
    return targetFile
}

fun deleteDir(path: String, vararg more: String?) {
    normalizedFile(path, *more).deleteRecursively()
}

fun checkFileExists(file: File, lazyMessage: () -> Any) {
    if (!file.exists()) {
        val message = lazyMessage()
        throw IllegalStateException(message.toString())
    }
}

fun normalizedFile(path: String, vararg more: String?): File {
    return Paths.get(path, *more).toFile().normalize()
}

fun normalizedPath(path: String, vararg more: String?): Path {
    return Paths.get(path, *more).toAbsolutePath().normalize()
}

fun normalizedPathName(path: String, vararg more: String?): String {
    return normalizedPath(path, *more).toString()
}

suspend fun File.reCreateNBDirs(): File = withContext(Dispatchers.IO) {
    reCreateDirs()
}

suspend fun deleteNBDir(path: String, vararg more: String?): Boolean = withContext(Dispatchers.IO) {
    normalizedFile(path, *more).deleteRecursively()
}

suspend fun File.readNBText(): String = withContext(Dispatchers.IO) {
    readText(Charset.defaultCharset())
}

suspend fun File.readNBLines(): List<String> = withContext(Dispatchers.IO) {
    readLines(Charset.defaultCharset())
}
