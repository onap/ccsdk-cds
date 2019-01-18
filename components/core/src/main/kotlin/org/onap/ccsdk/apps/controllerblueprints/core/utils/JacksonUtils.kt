/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintTypes
import java.io.File
import java.nio.charset.Charset
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

/**
 *
 *
 * @author Brinda Santh
 */
class JacksonUtils {
    companion object {
        private val log: EELFLogger = EELFManager.getInstance().getLogger(this::class.toString())
        inline fun <reified T : Any> readValue(content: String): T =
                jacksonObjectMapper().readValue(content, T::class.java)

        fun <T> readValue(content: String, valueType: Class<T>): T? {
            return jacksonObjectMapper().readValue(content, valueType)
        }

        fun <T> readValue(node: JsonNode, valueType: Class<T>): T? {
            return jacksonObjectMapper().treeToValue(node, valueType)
        }

        fun removeJsonNullNode(node: JsonNode) {
            val it = node.iterator()
            while (it.hasNext()) {
                val child = it.next()
                if (child.isNull) {
                    it.remove()
                } else {
                    removeJsonNullNode(child)
                }
            }
        }

        fun getContent(fileName: String): String = runBlocking {
            async {
                try {
                    File(fileName).readText(Charsets.UTF_8)
                } catch (e: Exception) {
                    throw BluePrintException("couldn't get file ($fileName) content : ${e.message}")
                }
            }.await()
        }

        fun getClassPathFileContent(fileName: String): String {
            return runBlocking {
                withContext(Dispatchers.Default) {
                    IOUtils.toString(JacksonUtils::class.java.classLoader
                            .getResourceAsStream(fileName), Charset.defaultCharset())
                }
            }
        }

        fun <T> readValueFromFile(fileName: String, valueType: Class<T>): T? {
            val content: String = getContent(fileName)
            return readValue(content, valueType)
        }

        fun <T> readValueFromClassPathFile(fileName: String, valueType: Class<T>): T? {
            val content: String = getClassPathFileContent(fileName)
            return readValue(content, valueType)
        }

        fun jsonNodeFromObject(from: kotlin.Any): JsonNode {
            return jacksonObjectMapper().convertValue(from, JsonNode::class.java)
        }

        fun jsonNodeFromClassPathFile(fileName: String): JsonNode {
            val content: String = getClassPathFileContent(fileName)
            return jsonNode(content)
        }

        fun jsonNodeFromFile(fileName: String): JsonNode {
            val content: String = getContent(fileName)
            return jsonNode(content)
        }

        fun jsonNode(content: String): JsonNode {
            return jacksonObjectMapper().readTree(content)
        }

        fun getJson(any: kotlin.Any): String {
            return getJson(any, false)
        }

        fun getWrappedJson(wrapper: String, any: kotlin.Any, pretty: Boolean = false): String {
            val wrapperMap = hashMapOf<String, Any>()
            wrapperMap[wrapper] = any
            return getJson(wrapperMap, pretty)
        }

        fun getJson(any: kotlin.Any, pretty: Boolean = false): String {
            val objectMapper = jacksonObjectMapper()
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            if (pretty) {
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
            }
            return objectMapper.writeValueAsString(any)
        }

        fun getJsonNode(any: kotlin.Any?, pretty: Boolean = false): JsonNode {
            val objectMapper = jacksonObjectMapper()
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            if (pretty) {
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
            }
            return objectMapper.valueToTree(any)
        }

        fun <T> getListFromJsonNode(node: JsonNode, valueType: Class<T>): List<T>? {
            return getListFromJson(node.toString(), valueType)
        }

        fun <T> getListFromJson(content: String, valueType: Class<T>): List<T>? {
            val objectMapper = jacksonObjectMapper()
            val javaType = objectMapper.typeFactory.constructCollectionType(List::class.java, valueType)
            return objectMapper.readValue<List<T>>(content, javaType)
        }

        fun <T> getListFromFile(fileName: String, valueType: Class<T>): List<T>? {
            val content: String = getContent(fileName)
            return getListFromJson(content, valueType)
        }

        fun <T> getListFromClassPathFile(fileName: String, valueType: Class<T>): List<T>? {
            val content: String = getClassPathFileContent(fileName)
            return getListFromJson(content, valueType)
        }

        fun <T> getMapFromJson(content: String, valueType: Class<T>): MutableMap<String, T>? {
            val objectMapper = jacksonObjectMapper()
            val typeRef = object : TypeReference<MutableMap<String, T>>() {}
            return objectMapper.readValue(content, typeRef)
        }

        fun <T> getMapFromFile(fileName: String, valueType: Class<T>): MutableMap<String, T>? {
            val content: String = getContent(fileName)
            return getMapFromJson(content, valueType)
        }

        fun <T> getInstanceFromMap(properties: MutableMap<String, JsonNode>, classType: Class<T>): T {
            return readValue(getJson(properties), classType)
                    ?: throw BluePrintProcessorException("failed to transform content ($properties) to type ($classType)")
        }

        fun checkJsonNodeValueOfType(type: String, jsonNode: JsonNode): Boolean {
            if (BluePrintTypes.validPrimitiveTypes().contains(type.toLowerCase())) {
                return checkJsonNodeValueOfPrimitiveType(type, jsonNode)
            } else if (BluePrintTypes.validCollectionTypes().contains(type)) {
                return checkJsonNodeValueOfCollectionType(type, jsonNode)
            }
            return false
        }

        fun checkIfPrimitiveType(primitiveType: String): Boolean {
            return when (primitiveType.toLowerCase()) {
                BluePrintConstants.DATA_TYPE_STRING -> true
                BluePrintConstants.DATA_TYPE_BOOLEAN -> true
                BluePrintConstants.DATA_TYPE_INTEGER -> true
                BluePrintConstants.DATA_TYPE_FLOAT -> true
                BluePrintConstants.DATA_TYPE_DOUBLE -> true
                BluePrintConstants.DATA_TYPE_TIMESTAMP -> true
                else -> false
            }
        }

        fun checkJsonNodeValueOfPrimitiveType(primitiveType: String, jsonNode: JsonNode): Boolean {
            return when (primitiveType.toLowerCase()) {
                BluePrintConstants.DATA_TYPE_STRING -> jsonNode.isTextual
                BluePrintConstants.DATA_TYPE_BOOLEAN -> jsonNode.isBoolean
                BluePrintConstants.DATA_TYPE_INTEGER -> jsonNode.isInt
                BluePrintConstants.DATA_TYPE_FLOAT -> jsonNode.isDouble
                BluePrintConstants.DATA_TYPE_DOUBLE -> jsonNode.isDouble
                BluePrintConstants.DATA_TYPE_TIMESTAMP -> jsonNode.isTextual
                else -> false
            }
        }

        fun checkJsonNodeValueOfCollectionType(type: String, jsonNode: JsonNode): Boolean {
            return when (type.toLowerCase()) {
                BluePrintConstants.DATA_TYPE_LIST -> jsonNode.isArray
                BluePrintConstants.DATA_TYPE_MAP -> jsonNode.isContainerNode
                else -> false
            }
        }

        fun populatePrimitiveValues(key: String, value: Any, primitiveType: String, objectNode: ObjectNode) {
            when (primitiveType.toLowerCase()) {
                BluePrintConstants.DATA_TYPE_BOOLEAN -> objectNode.put(key, value as Boolean)
                BluePrintConstants.DATA_TYPE_INTEGER -> objectNode.put(key, value as Int)
                BluePrintConstants.DATA_TYPE_FLOAT -> objectNode.put(key, value as Float)
                BluePrintConstants.DATA_TYPE_DOUBLE -> objectNode.put(key, value as Double)
                BluePrintConstants.DATA_TYPE_TIMESTAMP -> objectNode.put(key, value as String)
                else -> objectNode.put(key, value as String)
            }
        }

        fun populatePrimitiveValues(value: Any, primitiveType: String, arrayNode: ArrayNode) {
            when (primitiveType.toLowerCase()) {
                BluePrintConstants.DATA_TYPE_BOOLEAN -> arrayNode.add(value as Boolean)
                BluePrintConstants.DATA_TYPE_INTEGER -> arrayNode.add(value as Int)
                BluePrintConstants.DATA_TYPE_FLOAT -> arrayNode.add(value as Float)
                BluePrintConstants.DATA_TYPE_DOUBLE -> arrayNode.add(value as Double)
                BluePrintConstants.DATA_TYPE_TIMESTAMP -> arrayNode.add(value as String)
                else -> arrayNode.add(value as String)
            }
        }

        fun populatePrimitiveDefaultValues(key: String, primitiveType: String, objectNode: ObjectNode) {
            when (primitiveType.toLowerCase()) {
                BluePrintConstants.DATA_TYPE_BOOLEAN -> objectNode.put(key, false)
                BluePrintConstants.DATA_TYPE_INTEGER -> objectNode.put(key, 0)
                BluePrintConstants.DATA_TYPE_FLOAT -> objectNode.put(key, 0.0)
                BluePrintConstants.DATA_TYPE_DOUBLE -> objectNode.put(key, 0.0)
                else -> objectNode.put(key, "")
            }
        }

        fun populatePrimitiveDefaultValuesForArrayNode(primitiveType: String, arrayNode: ArrayNode) {
            when (primitiveType.toLowerCase()) {
                BluePrintConstants.DATA_TYPE_BOOLEAN -> arrayNode.add(false)
                BluePrintConstants.DATA_TYPE_INTEGER -> arrayNode.add(0)
                BluePrintConstants.DATA_TYPE_FLOAT -> arrayNode.add(0.0)
                BluePrintConstants.DATA_TYPE_DOUBLE -> arrayNode.add(0.0)
                else -> arrayNode.add("")
            }
        }

        fun populateJsonNodeValues(key: String, nodeValue: JsonNode?, type: String, objectNode: ObjectNode) {
            if (nodeValue == null || nodeValue is NullNode) {
                objectNode.set(key, nodeValue)
            } else if (BluePrintTypes.validPrimitiveTypes().contains(type)) {
                populatePrimitiveValues(key, nodeValue, type, objectNode)
            } else {
                objectNode.set(key, nodeValue)
            }
        }

        fun convertPrimitiveResourceValue(type: String, value: String): JsonNode {
            return when (type.toLowerCase()) {
                BluePrintConstants.DATA_TYPE_BOOLEAN -> jsonNodeFromObject(java.lang.Boolean.valueOf(value))
                BluePrintConstants.DATA_TYPE_INTEGER -> jsonNodeFromObject(Integer.valueOf(value))
                BluePrintConstants.DATA_TYPE_FLOAT -> jsonNodeFromObject(java.lang.Float.valueOf(value))
                BluePrintConstants.DATA_TYPE_DOUBLE -> jsonNodeFromObject(java.lang.Double.valueOf(value))
                else -> getJsonNode(value)
            }
        }

    }
}