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
import com.fasterxml.jackson.databind.node.*
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.helpers.MessageFormatter
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

fun <T : Any?> T.asJsonType(): JsonNode {
    return if (this == null) {
        NullNode.instance
    } else {
        when (this) {
            is JsonNode ->
                this
            is String ->
                TextNode(this)
            is Boolean ->
                BooleanNode.valueOf(this)
            is Int ->
                IntNode.valueOf(this.toInt())
            is Double ->
                DoubleNode.valueOf(this.toDouble())
            else ->
                JacksonUtils.jsonNodeFromObject(this)
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
    if (containsKey(key)) {
        return get(key) as? T
    } else {
        return null
    }
}

fun <T : Any> Map<String, *>.castValue(key: String, valueType: KClass<T>): T {
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


fun nullToEmpty(value: String?): String {
    return if (isNotEmpty(value)) value!! else ""
}


