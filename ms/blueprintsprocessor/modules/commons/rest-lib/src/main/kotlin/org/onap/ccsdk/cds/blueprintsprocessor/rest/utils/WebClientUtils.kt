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

package org.onap.ccsdk.cds.blueprintsprocessor.rest.utils

import org.apache.http.HttpRequestInterceptor
import org.apache.http.HttpResponseInterceptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WebClientUtils {
    companion object {

        val log: Logger = LoggerFactory.getLogger(WebClientUtils::class.java)

        fun logRequest(): HttpRequestInterceptor =
            HttpRequestInterceptor { request, _ -> log.info("Rest request method(${request?.requestLine?.method}), url(${request?.requestLine?.uri})") }

        fun logResponse(): HttpResponseInterceptor =
            HttpResponseInterceptor { response, _ -> log.info("Response status(${response.statusLine.statusCode} - ${response.statusLine.reasonPhrase})") }
    }
}