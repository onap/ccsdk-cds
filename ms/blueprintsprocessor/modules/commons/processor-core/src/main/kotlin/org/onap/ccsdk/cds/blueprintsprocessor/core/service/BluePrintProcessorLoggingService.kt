/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.core.service

import kotlinx.coroutines.AbstractCoroutine
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.handleCoroutineException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.slf4j.MDC
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.Disposable
import reactor.core.publisher.MonoSink
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.coroutines.CoroutineContext

class LoggingService {
    private val log = logger(LoggingService::class)

    companion object {
        const val ONAP_REQUEST_ID = "X-ONAP-RequestID"
        const val ONAP_INVOCATION_ID = "X-ONAP-InvocationID"
        const val ONAP_PARTNER_NAME = "X-ONAP-PartnerName"
    }

    fun entering(request: ServerHttpRequest) {
        val headers = request.headers
        val requestID = defaultToUUID(headers.getFirst(ONAP_REQUEST_ID))
        val invocationID = defaultToUUID(headers.getFirst(ONAP_INVOCATION_ID))
        val partnerName = defaultToEmpty(headers.getFirst(ONAP_PARTNER_NAME))
        MDC.put("InvokeTimestamp", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT))
        MDC.put("RequestID", requestID)
        MDC.put("InvocationID", invocationID)
        MDC.put("PartnerName", partnerName)
        MDC.put("ClientIPAddress", defaultToEmpty(request.remoteAddress?.address?.hostAddress))
        MDC.put("ServerFQDN", defaultToEmpty(request.remoteAddress?.hostString))
        if (MDC.get("ServiceName") == null || MDC.get("ServiceName").equals("", ignoreCase = true)) {
            MDC.put("ServiceName", request.uri.path)
        }
    }

    fun exiting(request: ServerHttpRequest, response: ServerHttpResponse) {
        try {
            val reqHeaders = request.headers
            val resHeaders = response.headers
            resHeaders[ONAP_REQUEST_ID] = MDC.get("RequestID")
            resHeaders[ONAP_INVOCATION_ID] = MDC.get("InvocationID")
        } catch (e: Exception) {
            log.warn("couldn't set response headers", e)
        } finally {
            MDC.clear()
        }
    }

    private fun defaultToEmpty(input: Any?): String {
        return input?.toString() ?: ""
    }

    private fun defaultToUUID(input: String?): String {
        return input ?: UUID.randomUUID().toString()
    }
}


@InternalCoroutinesApi
class MonoMDCCoroutine<in T>(
        parentContext: CoroutineContext,
        private val sink: MonoSink<T>
) : AbstractCoroutine<T>(parentContext, true), Disposable {
    private var disposed = false

    override fun onCompleted(value: T) {
        if (!disposed) {
            if (value == null) sink.success() else sink.success(value)
        }
    }

    override fun onCancelled(cause: Throwable, handled: Boolean) {
        if (!disposed) {
            sink.error(cause)
        } else if (!handled) {
            handleCoroutineException(context, cause)
        }
    }

    override fun dispose() {
        disposed = true
        cancel()
    }

    override fun isDisposed(): Boolean = disposed
}
