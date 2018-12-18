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

package org.onap.ccsdk.apps.blueprintsprocessor.rest.utils

import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import reactor.core.publisher.Mono


class WebClientUtils {
    companion object {

        val log = LoggerFactory.getLogger(WebClientUtils::class.java)!!

        fun logRequest(): ExchangeFilterFunction {

            return ExchangeFilterFunction.ofRequestProcessor { clientRequest ->
                log.info("Rest request method(${clientRequest.method()}), url(${clientRequest.url()})")
                Mono.just(clientRequest)
            }
        }

        fun logResponse(): ExchangeFilterFunction {
            return ExchangeFilterFunction.ofResponseProcessor { clientResponse ->
                log.info("Response status(${clientResponse.statusCode()})")
                Mono.just(clientResponse)
            }
        }
    }
}