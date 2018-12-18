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

package org.onap.ccsdk.apps.blueprintsprocessor.rest.service

import org.springframework.web.reactive.function.client.WebClient

interface BlueprintWebClientService {

    fun webClient(): WebClient

    fun <T> getResource(path: String, responseType: Class<T>): T

    fun <T> getResource(path: String, headers: Map<String, String>?, responseType: Class<T>): T

    fun <T> postResource(path: String, request: Any, responseType: Class<T>): T

    fun <T> postResource(path: String, headers: Map<String, String>?, request: Any, responseType: Class<T>): T

    fun <T> exchangeResource(methodType: String, path: String, request: Any, responseType: Class<T>): T

}

