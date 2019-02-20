/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.controllerblueprints.core

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.helpers.MessageFormatter
import java.io.File
import java.io.InputStream
import kotlin.reflect.KClass

/**
 *
 *
 * @author Brinda Santh
 */

fun String.asJsonPrimitive(): TextNode {
    return TextNode(this)
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

fun MutableMap<String, *>.asJsonNode(): JsonNode {
    return JacksonUtils.jsonNodeFromObject(this)
}

fun format(message: String, vararg args: Any?): String {
    if (args != null && args.isNotEmpty()) {
        return MessageFormatter.arrayFormat(message, args).message
    }
    return message
}

fun <T : Any> MutableMap<String, *>.castOptionalValue(key: String, valueType: KClass<T>): T? {
    if (containsKey(key)) {
        return get(key) as? T
    } else {
        return null
    }
}

fun <T : Any> MutableMap<String, *>.castValue(key: String, valueType: KClass<T>): T {
    if (containsKey(key)) {
        return get(key) as T
    } else {
        throw BluePrintException("couldn't find the key $key")
    }
}

/**
 * Convert Json to map of json node, the root fields will be map keys
 */
fun JsonNode.rootFieldsToMap(): MutableMap<String, JsonNode> {
    if (this is ObjectNode) {
        val propertyMap: MutableMap<String, JsonNode> = hashMapOf()
        this.fields().forEach {
            propertyMap[it.key] = it.value
        }
        return propertyMap
    } else {
        throw BluePrintException("json node should be Object Node Type")
    }
}


fun MutableMap<String, JsonNode>.putJsonElement(key: String, value: Any) {
    when (value) {
        is JsonNode ->
            this[key] = value
        is String ->
            this[key] = TextNode(value)
        is Boolean ->
            this[key] = BooleanNode.valueOf(value)
        is Int ->
            this[key] = IntNode.valueOf(value.toInt())
        is Double ->
            this[key] = DoubleNode.valueOf(value.toDouble())
        else ->
            this[key] = JacksonUtils.jsonNodeFromObject(value)
    }
}

fun MutableMap<String, JsonNode>.getAsString(key: String): String {
    return this[key]?.asText() ?: throw BluePrintException("couldn't find value for key($key)")
}

fun MutableMap<String, JsonNode>.getAsBoolean(key: String): Boolean {
    return this[key]?.asBoolean() ?: throw BluePrintException("couldn't find value for key($key)")
}

fun MutableMap<String, JsonNode>.getAsInt(key: String): Int {
    return this[key]?.asInt() ?: throw BluePrintException("couldn't find value for key($key)")
}

fun MutableMap<String, JsonNode>.getAsDouble(key: String): Double {
    return this[key]?.asDouble() ?: throw BluePrintException("couldn't find value for key($key)")
}

// Checks

fun checkNotEmpty(value: String?): Boolean {
    return value != null && value.isNotBlank()
}

fun checkNotEmptyOrThrow(value: String?, message: String? = value.plus(" is null/empty ")): Boolean {
    val notEmpty = checkNotEmpty(value)
    if (!notEmpty) {
        throw BluePrintException(message!!)
    }
    return notEmpty
}

fun checkEqualsOrThrow(value1: String?, value2: String?, lazyMessage: () -> Any): Boolean {
    if (value1.equals(value2, ignoreCase = true)) {
        return true
    } else {
        throw BluePrintException(lazyMessage().toString())
    }
}

fun nullToEmpty(value: String?): String {
    return if (checkNotEmpty(value)) value!! else ""
}

fun returnNotEmptyOrThrow(value: String?, lazyMessage: () -> Any): String {
    if (checkNotEmpty(value)) {
        return value!!
    } else {
        throw IllegalStateException(lazyMessage().toString())
    }
}

fun InputStream.toFile(path: String): File {
    val file = File(path)
    file.outputStream().use { this.copyTo(it) }
    return file
}

