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

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

class WebClientUtils {
    companion object {

        val log: Logger = LoggerFactory.getLogger(WebClientUtils::class.java)

        /**
         * Executes an outbound REST call and logs its start, its outcome and its duration.
         * Only the method and the URL without user info and query string are logged, because those can carry
         * credentials; headers and bodies are never logged.
         */
        fun <T : HttpResponse> logExecution(request: HttpUriRequest, execute: () -> T): T {
            val target = "method(${request.method}), url(${loggableUrl(request.uri)})"
            log.info("Rest request $target")
            val start = System.nanoTime()
            try {
                val response = execute()
                log.info(
                    "Rest response $target status(${response.statusLine.statusCode} - " +
                        "${response.statusLine.reasonPhrase}) in ${elapsedMillis(start)} ms"
                )
                return response
            } catch (e: Exception) {
                log.error("Rest request $target failed after ${elapsedMillis(start)} ms", e)
                throw e
            }
        }

        internal fun loggableUrl(uri: URI?): String {
            if (uri == null) return ""
            return try {
                URI(uri.scheme, null, uri.host, uri.port, uri.path, null, null).toString()
            } catch (e: Exception) {
                uri.rawPath.orEmpty()
            }
        }

        private fun elapsedMillis(start: Long) = (System.nanoTime() - start) / 1_000_000
    }
}
