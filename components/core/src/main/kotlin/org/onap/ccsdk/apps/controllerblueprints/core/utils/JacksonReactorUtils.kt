/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.controllerblueprints.core.utils

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

object JacksonReactorUtils {
    private val log: EELFLogger = EELFManager.getInstance().getLogger(this::class.toString())

    @JvmStatic
    fun getContent(fileName: String): Mono<String> {
        return JacksonUtils.getContent(fileName).toMono()
    }

    @JvmStatic
    fun getClassPathFileContent(fileName: String): Mono<String> {
        return JacksonUtils.getClassPathFileContent(fileName).toMono()
    }

    @JvmStatic
    fun <T> readValue(content: String, valueType: Class<T>): Mono<T> {
        return Mono.just(jacksonObjectMapper().readValue(content, valueType))
    }

    @JvmStatic
    fun jsonNode(content: String): Mono<JsonNode> {
        return Mono.just(jacksonObjectMapper().readTree(content))
    }

    @JvmStatic
    fun getJson(any: kotlin.Any, pretty: Boolean = false): Mono<String> {
        return Mono.just(JacksonUtils.getJson(any, pretty))
    }

    @JvmStatic
    fun <T> getListFromJson(content: String, valueType: Class<T>): Mono<List<T>> {
        val objectMapper = jacksonObjectMapper()
        val javaType = objectMapper.typeFactory.constructCollectionType(List::class.java, valueType)
        return objectMapper.readValue<List<T>>(content, javaType).toMono()
    }

    @JvmStatic
    fun <T> readValueFromFile(fileName: String, valueType: Class<T>): Mono<T> {
        return getContent(fileName)
                .flatMap { content ->
                    readValue(content, valueType)
                }
    }

    @JvmStatic
    fun <T> readValueFromClassPathFile(fileName: String, valueType: Class<T>): Mono<T> {
        return getClassPathFileContent(fileName)
                .flatMap { content ->
                    readValue(content, valueType)
                }
    }

    @JvmStatic
    fun jsonNodeFromFile(fileName: String): Mono<JsonNode> {
        return getContent(fileName)
                .flatMap { content ->
                    jsonNode(content)
                }
    }

    @JvmStatic
    fun jsonNodeFromClassPathFile(fileName: String): Mono<JsonNode> {
        return getClassPathFileContent(fileName)
                .flatMap { content ->
                    jsonNode(content)
                }
    }

    @JvmStatic
    fun <T> getListFromFile(fileName: String, valueType: Class<T>): Mono<List<T>> {
        return getContent(fileName)
                .flatMap { content ->
                    getListFromJson(content, valueType)
                }
    }

    @JvmStatic
    fun <T> getListFromClassPathFile(fileName: String, valueType: Class<T>): Mono<List<T>> {
        return getClassPathFileContent(fileName)
                .flatMap { content ->
                    getListFromJson(content, valueType)
                }
    }
}