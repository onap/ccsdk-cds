/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.core

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DoubleNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.apache.commons.lang3.ObjectUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JsonParserUtils
import org.slf4j.LoggerFactory
import org.slf4j.helpers.MessageFormatter
import java.util.UUID
import kotlin.reflect.KClass

/**
 *
 *
 * @author Brinda Santh
 */

fun <T : Any> logger(clazz: T) = LoggerFactory.getLogger(clazz.javaClass)!!

fun <T : KClass<*>> logger(clazz: T) = LoggerFactory.getLogger(clazz.java)!!

fun <T : Any> T?.defaultToEmpty(): String {
    return this?.toString() ?: ""
}

fun <T : Any> T?.defaultToUUID(): String {
    return this?.toString() ?: UUID.randomUUID().toString()
}

fun <T : Any> T.bpClone(): T {
    return ObjectUtils.clone(this)
}

fun String.splitCommaAsList(): List<String> {
    return this.split(",").map { it.trim() }.toList()
}

fun String.isJson(): Boolean {
    return (
        (this.trim().startsWith("{") && this.trim().endsWith("}")) ||
            (this.trim().startsWith("[") && this.trim().endsWith("]"))
        )
}

fun Any.asJsonString(intend: Boolean? = false): String {
    return JacksonUtils.getJson(this, intend!!)
}

fun String.asJsonPrimitive(): TextNode {
    return TextNode(this)
}

inline fun <reified T : Any> String.jsonAsType(): T {
    return JacksonUtils.readValue<T>(this.trim())
}

// If you know the string is json content, then use the function directly
fun String.jsonAsJsonType(): JsonNode {
    return JacksonUtils.jsonNode(this.trim())
}

fun Boolean.asJsonPrimitive(): BooleanNode {
    return BooleanNode.valueOf(this)
}

fun Int.asJsonPrimitive(): IntNode {
    return IntNode.valueOf(this)
}

fun Double.asJsonPrimitive(): DoubleNode {
    return DoubleNode.valueOf(this)
}

/**
 * Utility to convert Primitive object to Json Type Primitive.
 */
fun <T : Any?> T.asJsonPrimitive(): JsonNode {
    return if (this == null || this is MissingNode || this is NullNode) {
        NullNode.instance
    } else {
        when (this) {
            is String ->
                this.asJsonPrimitive()
            is Boolean ->
                this.asJsonPrimitive()
            is Int ->
                this.asJsonPrimitive()
            is Double ->
                this.asJsonPrimitive()
            else ->
                throw BluePrintException("$this type is not supported")
        }
    }
}

/** Based on Blueprint DataType Convert string value to JsonNode Type **/
fun String.asJsonType(bpDataType: String): JsonNode {
    return when (bpDataType.toLowerCase()) {
        BluePrintConstants.DATA_TYPE_STRING -> this.asJsonPrimitive()
        BluePrintConstants.DATA_TYPE_BOOLEAN -> this.toBoolean().asJsonPrimitive()
        BluePrintConstants.DATA_TYPE_INTEGER -> this.toInt().asJsonPrimitive()
        BluePrintConstants.DATA_TYPE_FLOAT -> this.toFloat().asJsonPrimitive()
        BluePrintConstants.DATA_TYPE_DOUBLE -> this.toDouble().asJsonPrimitive()
        // For List, Map and Complex Types.
        else -> this.jsonAsJsonType()
    }
}

/**
 * Utility to convert Complex or Primitive object or ByteArray to Json Type.
 */
fun <T : Any?> T.asJsonType(): JsonNode {
    return if (this == null || this is MissingNode || this is NullNode) {
        NullNode.instance
    } else {
        when (this) {
            is JsonNode -> this
            is ByteArray -> JacksonUtils.objectMapper.reader().readTree(this.inputStream())
            is String -> {
                if (this.isJson())
                    this.jsonAsJsonType()
                else
                    TextNode(this)
            }
            is Boolean -> BooleanNode.valueOf(this)
            is Int -> IntNode.valueOf(this.toInt())
            is Double -> DoubleNode.valueOf(this.toDouble())
            else -> JacksonUtils.jsonNodeFromObject(this)
        }
    }
}

fun Map<String, *>.asJsonNode(): JsonNode {
    return JacksonUtils.jsonNodeFromObject(this)
}

fun Map<String, *>.asObjectNode(): ObjectNode {
    return JacksonUtils.objectNodeFromObject(this)
}

fun format(message: String, vararg args: Any?): String {
    if (args != null && args.isNotEmpty()) {
        return MessageFormatter.arrayFormat(message, args).message
    }
    return message
}

fun <T : Any> Map<String, *>.castOptionalValue(key: String, valueType: KClass<T>): T? {
    return if (containsKey(key)) {
        get(key) as? T
    } else {
        null
    }
}

fun <T : Any> Map<String, *>.castValue(key: String, valueType: KClass<T>): T {
    if (containsKey(key)) {
        return get(key) as T
    } else {
        throw BluePrintException("couldn't find the key $key")
    }
}

fun ArrayNode.asListOfString(): List<String> {
    return JacksonUtils.getListFromJsonNode(this, String::class.java)
}

fun JsonNode.asByteArray(): ByteArray {
    val writer = JacksonUtils.objectMapper.writer()
    return writer.writeValueAsBytes(this)
}

fun <T> JsonNode.asType(clazzType: Class<T>): T {
    return JacksonUtils.readValue(this, clazzType)
        ?: throw BluePrintException("couldn't convert JsonNode of type $clazzType")
}

fun JsonNode.asListOfString(): List<String> {
    check(this is ArrayNode) { "JsonNode is not of type ArrayNode" }
    return this.asListOfString()
}

fun <T : JsonNode> T?.returnNullIfMissing(): JsonNode? {
    return if (this == null || this is NullNode || this is MissingNode) {
        null
    } else this
}

fun <T : JsonNode> T?.isNullOrMissing(): Boolean {
    return this == null || this is NullNode || this is MissingNode
}

/**
 * Convert Json to map of json node, the root fields will be map keys
 */
fun JsonNode.rootFieldsToMap(): MutableMap<String, JsonNode> {
    if (this is ObjectNode) {
        val propertyMap: MutableMap<String, JsonNode> = linkedMapOf()
        this.fields().forEach {
            propertyMap[it.key] = it.value
        }
        return propertyMap
    } else {
        throw BluePrintException("json node should be Object Node Type")
    }
}

fun JsonNode.removeNullNode() {
    val it = this.iterator()
    while (it.hasNext()) {
        val child = it.next()
        if (child.isNull) {
            it.remove()
        } else {
            child.removeNullNode()
        }
    }
}

fun MutableMap<String, JsonNode>.putJsonElement(key: String, value: Any) {
    val convertedValue = value.asJsonType()
    this[key] = convertedValue
}

fun Map<String, JsonNode>.getAsString(key: String): String {
    return this[key]?.asText() ?: throw BluePrintException("couldn't find value for key($key)")
}

fun Map<String, JsonNode>.getAsBoolean(key: String): Boolean {
    return this[key]?.asBoolean() ?: throw BluePrintException("couldn't find value for key($key)")
}

fun Map<String, JsonNode>.getAsInt(key: String): Int {
    return this[key]?.asInt() ?: throw BluePrintException("couldn't find value for key($key)")
}

fun Map<String, JsonNode>.getAsDouble(key: String): Double {
    return this[key]?.asDouble() ?: throw BluePrintException("couldn't find value for key($key)")
}

fun Map<String, JsonNode>.getOptionalAsString(key: String): String? {
    return if (this.containsKey(key)) this[key]!!.asText() else null
}

fun Map<String, JsonNode>.getOptionalAsBoolean(key: String): Boolean? {
    return if (this.containsKey(key)) this[key]!!.asBoolean() else null
}

fun Map<String, JsonNode>.getOptionalAsInt(key: String): Int? {
    return if (this.containsKey(key)) this[key]!!.asInt() else null
}

fun Map<String, JsonNode>.getOptionalAsDouble(key: String): Double? {
    return if (this.containsKey(key)) this[key]!!.asDouble() else null
}

// Checks

inline fun checkEquals(value1: String?, value2: String?, lazyMessage: () -> Any): Boolean {
    if (value1.equals(value2, ignoreCase = true)) {
        return true
    } else {
        throw BluePrintException(lazyMessage().toString())
    }
}

inline fun checkNotEmpty(value: String?, lazyMessage: () -> Any): String {
    if (value == null || value.isEmpty()) {
        val message = lazyMessage()
        throw IllegalStateException(message.toString())
    } else {
        return value
    }
}

inline fun checkNotBlank(value: String?, lazyMessage: () -> Any): String {
    if (value == null || value.isBlank()) {
        val message = lazyMessage()
        throw IllegalStateException(message.toString())
    } else {
        return value
    }
}

fun isNotEmpty(value: String?): Boolean {
    return value != null && value.isNotEmpty()
}

fun isNotBlank(value: String?): Boolean {
    return value != null && value.isNotBlank()
}

fun <T : String> T?.emptyTONull(): String? {
    return if (this == null || this.isEmpty()) null else this
}

fun nullToEmpty(value: String?): String {
    return if (isNotEmpty(value)) value!! else ""
}

inline fun <reified T : JsonNode> T.isComplexType(): Boolean {
    return this is ObjectNode || this is ArrayNode
}

// Json Parsing Extensions
fun JsonNode.jsonPathParse(expression: String): JsonNode {
    check(this.isComplexType()) { "$this is not complex or array node to apply expression" }
    return JsonParserUtils.parse(this, expression)
}

// Json Path Extensions
fun JsonNode.jsonPaths(expression: String): List<String> {
    check(this.isComplexType()) { "$this is not complex or array node to apply expression" }
    return JsonParserUtils.paths(this, expression)
}
