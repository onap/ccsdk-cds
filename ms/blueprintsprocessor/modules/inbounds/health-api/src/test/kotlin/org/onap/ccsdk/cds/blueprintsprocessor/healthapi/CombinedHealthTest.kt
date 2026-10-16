/*
 * Copyright © 2026 Deutsche Telekom AG.
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

package org.onap.ccsdk.cds.blueprintsprocessor.healthapi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.controller.CombinedHealth
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.controller.CombinedMetrics
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ApplicationHealth
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.MetricsInfo
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.CombinedHealthService
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.CombinedMetricsService
import org.springframework.boot.actuate.health.Status
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * The handlers are invoked on a reactor thread of a single-threaded scheduler, and the checks only
 * complete once that scheduler runs another task, like a self-call that has to be authenticated.
 * A handler that runs the checks on the reactor thread deadlocks.
 */
class CombinedHealthTest {

    private val reactorScheduler = Schedulers.newParallel("health-test", 1)

    @After
    fun tearDown() {
        reactorScheduler.dispose()
    }

    @Test
    fun testCombinedHealthDoesNotBlockTheReactorThread() {
        val combinedHealthService = Mockito.mock(CombinedHealthService::class.java)
        Mockito.`when`(combinedHealthService.getCombinedHealthCheck()).thenAnswer {
            awaitTaskOnReactorScheduler()
            listOf(ApplicationHealth(Status.UP, emptyMap()))
        }
        val controller = CombinedHealth(combinedHealthService)

        val response = mono(Dispatchers.Unconfined) { controller.getSystemHealthCheckResponse() }
            .subscribeOn(reactorScheduler)
            .block(Duration.ofSeconds(10))

        assertEquals(Status.UP, response!!.body!![0]!!.status)
    }

    @Test
    fun testCombinedMetricsDoesNotBlockTheReactorThread() {
        val combinedMetricsService = Mockito.mock(CombinedMetricsService::class.java)
        Mockito.`when`(combinedMetricsService.metricsInfo).thenAnswer {
            awaitTaskOnReactorScheduler()
            MetricsInfo(emptyList())
        }
        val controller = CombinedMetrics(combinedMetricsService)

        val response = mono(Dispatchers.Unconfined) { controller.getMetricsHealthCheckResponse() }
            .subscribeOn(reactorScheduler)
            .block(Duration.ofSeconds(10))

        assertEquals(MetricsInfo(emptyList()), response!!.body)
    }

    private fun awaitTaskOnReactorScheduler() {
        val done = CompletableFuture<Unit>()
        reactorScheduler.schedule { done.complete(Unit) }
        done.get(5, TimeUnit.SECONDS)
    }
}
