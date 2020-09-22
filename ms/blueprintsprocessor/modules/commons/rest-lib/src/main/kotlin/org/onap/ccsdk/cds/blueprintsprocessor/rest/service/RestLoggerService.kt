/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
 * Modifications Copyright © 2020 Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.rest.service

import kotlinx.coroutines.AbstractCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.handleCoroutineException
import kotlinx.coroutines.newCoroutineContext
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.asCoroutineContext
import kotlinx.coroutines.withContext
import org.apache.http.message.BasicHeader
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants.ONAP_INVOCATION_ID
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants.ONAP_ORIGINATOR_ID
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants.ONAP_PARTNER_NAME
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants.ONAP_REQUEST_ID
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants.ONAP_SUBREQUEST_ID
import org.onap.ccsdk.cds.controllerblueprints.core.MDCContext
import org.onap.ccsdk.cds.controllerblueprints.core.defaultToEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.defaultToUUID
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.slf4j.MDC
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import reactor.core.Disposable
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.net.InetAddress
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class RestLoggerService {
    private val log = logger(RestLoggerService::class)

    companion object {
        /** Used before invoking any REST outbound request, Inbound Invocation ID is used as request Id
         * for outbound Request, If invocation Id is missing then default Request Id will be generated.
         */
        fun httpInvoking(headers: Array<BasicHeader>) {
            headers.plusElement(BasicHeader(ONAP_REQUEST_ID, MDC.get("InvocationID").defaultToUUID()))
            headers.plusElement(BasicHeader(ONAP_INVOCATION_ID, UUID.randomUUID().toString()))
            headers.plusElement(BasicHeader(ONAP_PARTNER_NAME, BluePrintConstants.APP_NAME))
        }
    }

    fun entering(request: ServerHttpRequest) {
        val localhost = InetAddress.getLocalHost()
        val headers = request.headers
        val requestID = headers.getFirst(ONAP_REQUEST_ID).defaultToUUID()
        val subrequestID = headers.getFirst(ONAP_SUBREQUEST_ID).defaultToEmpty()
        val originatorID = headers.getFirst(ONAP_ORIGINATOR_ID).defaultToEmpty()
        val invocationID = headers.getFirst(ONAP_INVOCATION_ID).defaultToUUID()
        val partnerName = headers.getFirst(ONAP_PARTNER_NAME).defaultToEmpty()
        MDC.put("InvokeTimestamp", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT))
        MDC.put("RequestID", requestID)
        MDC.put("SubRequestID", subrequestID)
        MDC.put("OriginatorID", originatorID)
        MDC.put("InvocationID", invocationID)
        MDC.put("PartnerName", partnerName)
        MDC.put("ClientIPAddress", request.remoteAddress?.address?.hostAddress.defaultToEmpty())
        MDC.put("ServerFQDN", localhost.hostName.defaultToEmpty())
        if (MDC.get("ServiceName") == null || MDC.get("ServiceName").equals("", ignoreCase = true)) {
            MDC.put("ServiceName", request.uri.path)
        }
    }

    fun exiting(request: ServerHttpRequest, response: ServerHttpResponse) {
        try {
            val reqHeaders = request.headers
            val resHeaders = response.headers
            resHeaders[ONAP_REQUEST_ID] = MDC.get("RequestID")
            resHeaders[ONAP_SUBREQUEST_ID] = MDC.get("SubRequestID")
            resHeaders[ONAP_ORIGINATOR_ID] = MDC.get("OriginatorID")
            resHeaders[ONAP_INVOCATION_ID] = MDC.get("InvocationID")
            resHeaders[ONAP_PARTNER_NAME] = BluePrintConstants.APP_NAME
        } catch (e: Exception) {
            log.warn("couldn't set response headers", e)
        } finally {
            MDC.clear()
        }
    }
}

/** Used in Rest controller API methods to populate MDC context to nested coroutines from reactor web filter context. */
suspend fun <T> mdcWebCoroutineScope(
    block: suspend CoroutineScope.() -> T
) = coroutineScope {
    val reactorContext = this.coroutineContext[ReactorContext]
    /** Populate MDC context only if present in Reactor Context */
    val newContext = if (reactorContext != null &&
        !reactorContext.context.isEmpty &&
        reactorContext.context.hasKey(MDCContext)
    ) {
        val mdcContext = reactorContext.context.get<MDCContext>(MDCContext)
        if (mdcContext != null)
            newCoroutineContext(this.coroutineContext + reactorContext + mdcContext)
        else
            newCoroutineContext(this.coroutineContext + reactorContext)
    } else this.coroutineContext
    // Execute the block with new and old context
    withContext(newContext) {
        block()
    }
}

@Deprecated(
    message = "Now CDS supports Coruoutin rest controller",
    replaceWith = ReplaceWith("mdcWebCoroutineScope")
)
/** Used in Rest controller API methods to populate MDC context to nested coroutines from reactor web filter context. */
@UseExperimental(InternalCoroutinesApi::class)
fun <T> monoMdc(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T?
): Mono<T> = Mono.create { sink ->

    val reactorContext = (
        context[ReactorContext]?.context?.putAll(sink.currentContext())
            ?: sink.currentContext()
        ).asCoroutineContext()
    /** Populate MDC context only if present in Reactor Context */
    val newContext = if (!reactorContext.context.isEmpty &&
        reactorContext.context.hasKey(MDCContext)
    ) {
        val mdcContext = reactorContext.context.get<MDCContext>(MDCContext)
        GlobalScope.newCoroutineContext(context + reactorContext + mdcContext)
    } else GlobalScope.newCoroutineContext(context + reactorContext)

    val coroutine = MonoMDCCoroutine(newContext, sink)
    sink.onDispose(coroutine)
    coroutine.start(CoroutineStart.DEFAULT, coroutine, block)
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
