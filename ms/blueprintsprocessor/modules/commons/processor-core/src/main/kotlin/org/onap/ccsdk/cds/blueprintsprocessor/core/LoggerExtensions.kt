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

package org.onap.ccsdk.cds.blueprintsprocessor.core

import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.asCoroutineContext
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.MonoMDCCoroutine
import org.onap.ccsdk.cds.controllerblueprints.core.MDCContext
import reactor.core.publisher.Mono
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/** Used in Rest controller API methods to populate MDC context to nested coroutines from reactor web filter context. */
@UseExperimental(InternalCoroutinesApi::class)
fun <T> monoMdc(context: CoroutineContext = EmptyCoroutineContext,
                block: suspend CoroutineScope.() -> T?): Mono<T> = Mono.create { sink ->

    val reactorContext = (context[ReactorContext]?.context?.putAll(sink.currentContext())
            ?: sink.currentContext()).asCoroutineContext()
    /** Populate MDC context only if present in Reactor Context */
    val newContext = if (!reactorContext.context.isEmpty
            && reactorContext.context.hasKey(MDCContext)) {
        val mdcContext = reactorContext.context.get<MDCContext>(MDCContext)
        GlobalScope.newCoroutineContext(context + reactorContext + mdcContext)
    } else GlobalScope.newCoroutineContext(context + reactorContext)

    val coroutine = MonoMDCCoroutine(newContext, sink)
    sink.onDispose(coroutine)
    coroutine.start(CoroutineStart.DEFAULT, coroutine, block)
}