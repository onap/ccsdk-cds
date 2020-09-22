/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *  Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.core.utils

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.Charset

class JacksonReactorUtils {
    companion object {

        private val log = LoggerFactory.getLogger(this::class.toString())

        suspend fun getContent(fileName: String): String {
            // log.info("Reading File($fileName)")
            return getContent(normalizedFile(fileName))
        }

        suspend fun getContent(file: File): String = withContext(Dispatchers.IO) {
            // log.info("Reading File(${file.absolutePath})")
            file.readText(Charsets.UTF_8)
        }

        suspend fun getClassPathFileContent(fileName: String): String = withContext(Dispatchers.IO) {
            // log.trace("Reading Classpath File($fileName)")
            IOUtils.toString(
                JacksonUtils::class.java.classLoader
                    .getResourceAsStream(fileName),
                Charset.defaultCharset()
            )
        }

        suspend fun <T> readValueFromFile(fileName: String, valueType: Class<T>): T? {
            val content: String = getContent(fileName)
            return JacksonUtils.readValue(content, valueType)
        }

        suspend fun <T> readValueFromClassPathFile(fileName: String, valueType: Class<T>): T? {
            val content: String = getClassPathFileContent(fileName)
            return JacksonUtils.readValue(content, valueType)
        }

        suspend fun jsonNodeFromClassPathFile(fileName: String): JsonNode {
            val content: String = getClassPathFileContent(fileName)
            return JacksonUtils.jsonNode(content)
        }

        suspend fun jsonNodeFromFile(fileName: String): JsonNode {
            val content: String = getContent(fileName)
            return JacksonUtils.jsonNode(content)
        }

        suspend fun <T> getListFromFile(fileName: String, valueType: Class<T>): List<T> {
            val content: String = getContent(fileName)
            return JacksonUtils.getListFromJson(content, valueType)
        }

        suspend fun <T> getListFromClassPathFile(fileName: String, valueType: Class<T>): List<T> {
            val content: String = getClassPathFileContent(fileName)
            return JacksonUtils.getListFromJson(content, valueType)
        }

        suspend fun <T> getMapFromFile(file: File, valueType: Class<T>): MutableMap<String, T> {
            val content: String = getContent(file)
            return JacksonUtils.getMapFromJson(content, valueType)
        }

        suspend fun <T> getMapFromClassPathFile(fileName: String, valueType: Class<T>): MutableMap<String, T> {
            val content: String = getClassPathFileContent(fileName)
            return JacksonUtils.getMapFromJson(content, valueType)
        }
    }
}
