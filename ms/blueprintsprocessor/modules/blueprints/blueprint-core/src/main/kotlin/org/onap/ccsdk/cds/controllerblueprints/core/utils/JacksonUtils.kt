/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018-2019 IBM.
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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DoubleNode
import com.fasterxml.jackson.databind.node.FloatNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.readNBText
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset

/**
 *
 *
 * @author Brinda Santh
 */
class JacksonUtils {

    companion object {

        val objectMapper = jacksonObjectMapper()

        inline fun <reified T : Any> readValue(content: String): T =
            objectMapper.readValue(content, T::class.java)

        inline fun <reified T : Any> readValue(stream: InputStream): T =
            objectMapper.readValue(stream, T::class.java)

        fun <T> readValue(content: String, valueType: Class<T>): T? {
            return objectMapper.readValue(content, valueType)
        }

        fun <T> readValue(stream: InputStream, valueType: Class<T>): T? {
            return objectMapper.readValue(stream, valueType)
        }

        fun <T> readValue(node: JsonNode, valueType: Class<T>): T? {
            return objectMapper.treeToValue(node, valueType)
        }

        fun getContent(fileName: String): String = runBlocking {
            try {
                normalizedFile(fileName).readNBText()
            } catch (e: Exception) {
                throw BlueprintException("couldn't get file ($fileName) content : ${e.message}")
            }
        }

        fun getClassPathFileContent(fileName: String): String {
            return runBlocking {
                withContext(Dispatchers.Default) {
                    IOUtils.toString(
                        JacksonUtils::class.java.classLoader
                            .getResourceAsStream(fileName),
                        Charset.defaultCharset()
                    )
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

        fun objectNodeFromObject(from: kotlin.Any): ObjectNode {
            return objectMapper.convertValue(from, ObjectNode::class.java)
        }

        fun jsonNodeFromObject(from: kotlin.Any): JsonNode {
            return objectMapper.convertValue(from, JsonNode::class.java)
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

        fun getJson(any: kotlin.Any, pretty: Boolean = false, includeNull: Boolean = false): String {
            val objectMapper = jacksonObjectMapper()
            if (includeNull) {
                objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS)
            } else {
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            }
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

        fun <T> getListFromJsonNode(node: JsonNode, valueType: Class<T>): List<T> {
            return getListFromJson(node.toString(), valueType)
        }

        fun <T> getListFromJson(content: String, valueType: Class<T>): List<T> {
            val objectMapper = jacksonObjectMapper()
            val javaType = objectMapper.typeFactory.constructCollectionType(List::class.java, valueType)
            return objectMapper.readValue<List<T>>(content, javaType)
        }

        fun <T> getListFromFile(fileName: String, valueType: Class<T>): List<T> {
            val content: String = getContent(fileName)
            return getListFromJson(content, valueType)
        }

        fun <T> getListFromClassPathFile(fileName: String, valueType: Class<T>): List<T> {
            val content: String = getClassPathFileContent(fileName)
            return getListFromJson(content, valueType)
        }

        fun <T> getMapFromJson(content: String, valueType: Class<T>): MutableMap<String, T> {
            val objectMapper = jacksonObjectMapper()
            val mapType = objectMapper.typeFactory.constructMapType(Map::class.java, String::class.java, valueType)
            return objectMapper.readValue(content, mapType)
        }

        fun <T> getMapFromFile(file: File, valueType: Class<T>): MutableMap<String, T> = runBlocking {
            val content: String = file.readNBText()
            getMapFromJson(content, valueType)
        }

        fun <T> getMapFromFile(fileName: String, valueType: Class<T>): MutableMap<String, T> = getMapFromFile(File(fileName), valueType)

        fun <T> getInstanceFromMap(properties: MutableMap<String, JsonNode>, classType: Class<T>): T {
            return readValue(getJson(properties), classType)
                ?: throw BlueprintProcessorException("failed to transform content ($properties) to type ($classType)")
        }

        fun checkJsonNodeValueOfType(type: String, jsonNode: JsonNode): Boolean {
            if (BlueprintTypes.validPrimitiveTypes().contains(type.toLowerCase())) {
                return checkJsonNodeValueOfPrimitiveType(type, jsonNode)
            } else if (BlueprintTypes.validCollectionTypes().contains(type)) {
                return checkJsonNodeValueOfCollectionType(type, jsonNode)
            }
            return false
        }

        fun checkIfPrimitiveType(primitiveType: String): Boolean {
            return when (primitiveType.toLowerCase()) {
                BlueprintConstants.DATA_TYPE_STRING -> true
                BlueprintConstants.DATA_TYPE_BOOLEAN -> true
                BlueprintConstants.DATA_TYPE_INTEGER -> true
                BlueprintConstants.DATA_TYPE_FLOAT -> true
                BlueprintConstants.DATA_TYPE_DOUBLE -> true
                BlueprintConstants.DATA_TYPE_TIMESTAMP -> true
                else -> false
            }
        }

        fun checkJsonNodeValueOfPrimitiveType(primitiveType: String, jsonNode: JsonNode): Boolean {
            return when (primitiveType.toLowerCase()) {
                BlueprintConstants.DATA_TYPE_STRING -> jsonNode.isTextual
                BlueprintConstants.DATA_TYPE_BOOLEAN -> jsonNode.isBoolean
                BlueprintConstants.DATA_TYPE_INTEGER -> jsonNode.isInt
                BlueprintConstants.DATA_TYPE_FLOAT -> jsonNode.isDouble
                BlueprintConstants.DATA_TYPE_DOUBLE -> jsonNode.isDouble
                BlueprintConstants.DATA_TYPE_TIMESTAMP -> jsonNode.isTextual
                else -> false
            }
        }

        fun checkJsonNodeValueOfCollectionType(type: String, jsonNode: JsonNode): Boolean {
            return when (type.toLowerCase()) {
                BlueprintConstants.DATA_TYPE_LIST -> jsonNode.isArray
                else -> false
            }
        }

        fun getValue(value: JsonNode): Any {
            return when (value) {
                is BooleanNode -> value.booleanValue()
                is IntNode -> value.intValue()
                is FloatNode -> value.floatValue()
                is DoubleNode -> value.doubleValue()
                is TextNode -> value.textValue()
                else -> value
            }
        }

        fun getValue(value: Any, type: String): Any {
            return when (type.toLowerCase()) {
                BlueprintConstants.DATA_TYPE_BOOLEAN -> (value as BooleanNode).booleanValue()
                BlueprintConstants.DATA_TYPE_INTEGER -> (value as IntNode).intValue()
                BlueprintConstants.DATA_TYPE_FLOAT -> (value as FloatNode).floatValue()
                BlueprintConstants.DATA_TYPE_DOUBLE -> (value as DoubleNode).doubleValue()
                BlueprintConstants.DATA_TYPE_STRING -> (value as TextNode).textValue()
                else -> (value as JsonNode)
            }
        }

        fun populatePrimitiveValues(key: String, value: JsonNode, primitiveType: String, objectNode: ObjectNode) {
            when (primitiveType.toLowerCase()) {
                BlueprintConstants.DATA_TYPE_BOOLEAN,
                BlueprintConstants.DATA_TYPE_INTEGER,
                BlueprintConstants.DATA_TYPE_FLOAT,
                BlueprintConstants.DATA_TYPE_DOUBLE,
                BlueprintConstants.DATA_TYPE_TIMESTAMP,
                BlueprintConstants.DATA_TYPE_STRING,
                BlueprintConstants.DATA_TYPE_NULL ->
                    objectNode.set(key, value)
                else -> throw BlueprintException("populatePrimitiveValues expected only primitive values! Received: ($value)")
            }
        }

        fun populatePrimitiveValues(value: JsonNode, primitiveType: String, arrayNode: ArrayNode) {
            when (primitiveType.toLowerCase()) {
                BlueprintConstants.DATA_TYPE_BOOLEAN,
                BlueprintConstants.DATA_TYPE_INTEGER,
                BlueprintConstants.DATA_TYPE_FLOAT,
                BlueprintConstants.DATA_TYPE_DOUBLE,
                BlueprintConstants.DATA_TYPE_TIMESTAMP,
                BlueprintConstants.DATA_TYPE_STRING,
                BlueprintConstants.DATA_TYPE_NULL ->
                    arrayNode.add(value)
                else -> throw BlueprintException("populatePrimitiveValues expected only primitive values! Received: ($value)")
            }
        }

        fun populatePrimitiveDefaultValues(key: String, primitiveType: String, objectNode: ObjectNode) {
            val defaultValue = getDefaultValueOfPrimitiveAsJsonNode(primitiveType)
                ?: throw BlueprintException("populatePrimitiveDefaultValues expected only primitive values! Received type ($primitiveType)")
            objectNode.set<JsonNode>(key, defaultValue)
        }

        fun populatePrimitiveDefaultValuesForArrayNode(primitiveType: String, arrayNode: ArrayNode) {
            val defaultValue = getDefaultValueOfPrimitiveAsJsonNode(primitiveType)
                ?: throw BlueprintException("populatePrimitiveDefaultValuesForArrayNode expected only primitive values! Received type ($primitiveType)")
            arrayNode.add(defaultValue)
        }

        private fun getDefaultValueOfPrimitiveAsJsonNode(primitiveType: String): JsonNode? {
            return when (primitiveType.toLowerCase()) {
                BlueprintConstants.DATA_TYPE_BOOLEAN -> BooleanNode.valueOf(false)
                BlueprintConstants.DATA_TYPE_INTEGER -> IntNode.valueOf(0)
                BlueprintConstants.DATA_TYPE_FLOAT -> FloatNode.valueOf(0.0f)
                BlueprintConstants.DATA_TYPE_DOUBLE -> DoubleNode.valueOf(0.0)
                BlueprintConstants.DATA_TYPE_STRING -> MissingNode.getInstance()
                else -> null
            }
        }

        fun populateJsonNodeValues(key: String, nodeValue: JsonNode?, type: String, objectNode: ObjectNode) {
            if (nodeValue == null || nodeValue is NullNode) {
                objectNode.set<JsonNode>(key, nodeValue)
            } else if (BlueprintTypes.validPrimitiveTypes().contains(type)) {
                populatePrimitiveValues(key, nodeValue, type, objectNode)
            } else {
                objectNode.set<JsonNode>(key, nodeValue)
            }
        }

        fun convertPrimitiveResourceValue(type: String, value: String): JsonNode {
            return when (type.toLowerCase()) {
                BlueprintConstants.DATA_TYPE_BOOLEAN -> jsonNodeFromObject(value.toBoolean())
                BlueprintConstants.DATA_TYPE_INTEGER -> jsonNodeFromObject(value.toInt())
                BlueprintConstants.DATA_TYPE_FLOAT -> jsonNodeFromObject(value.toFloat())
                BlueprintConstants.DATA_TYPE_DOUBLE -> jsonNodeFromObject(value.toDouble())
                BlueprintConstants.DATA_TYPE_STRING -> jsonNodeFromObject(value)
                else -> getJsonNode(value)
            }
        }
    }
}
