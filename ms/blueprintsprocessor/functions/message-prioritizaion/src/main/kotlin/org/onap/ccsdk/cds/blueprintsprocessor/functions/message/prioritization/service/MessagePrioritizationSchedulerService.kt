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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.PrioritizationConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.springframework.stereotype.Service

@Service
open class MessagePrioritizationSchedulerService(
    private val messagePrioritizationService: MessagePrioritizationService
) {
    private val log = logger(MessagePrioritizationSchedulerService::class)

    @Volatile
    var keepGoing = true

    /** This is sample scheduler implementation used during starting application with configuration.
     @EventListener(ApplicationReadyEvent::class)
     open fun init() = runBlocking {
     log.info("Starting PrioritizationListeners...")
     startScheduling(MessagePrioritizationSample.samplePrioritizationConfiguration())
     }
     */

    open suspend fun startScheduling() {
        val prioritizationConfiguration = messagePrioritizationService.getConfiguration()

        log.info("Starting Prioritization Scheduler Service...")
        GlobalScope.launch {
            expiryScheduler(prioritizationConfiguration)
        }
        GlobalScope.launch {
            cleanUpScheduler(prioritizationConfiguration)
        }
    }

    open suspend fun shutdownScheduling() {
        keepGoing = false
        val prioritizationConfiguration = messagePrioritizationService.getConfiguration()
        delay(prioritizationConfiguration.shutDownConfiguration.waitMill)
    }

    private suspend fun expiryScheduler(
        prioritizationConfiguration: PrioritizationConfiguration
    ) {
        val expiryConfiguration = prioritizationConfiguration.expiryConfiguration
        log.info("Initializing prioritization expiry scheduler frequency(${expiryConfiguration.frequencyMilli})mSec")
        withContext(Dispatchers.Default) {
            while (keepGoing) {
                try {
                    messagePrioritizationService.updateExpiredMessages()
                    delay(expiryConfiguration.frequencyMilli)
                } catch (e: Exception) {
                    log.error("failed in prioritization expiry scheduler", e)
                }
            }
        }
    }

    private suspend fun cleanUpScheduler(
        prioritizationConfiguration: PrioritizationConfiguration
    ) {
        val cleanConfiguration = prioritizationConfiguration.cleanConfiguration
        log.info("Initializing prioritization clean scheduler frequency(${cleanConfiguration.frequencyMilli})mSec")
        withContext(Dispatchers.Default) {
            while (keepGoing) {
                try {
                    messagePrioritizationService.cleanExpiredMessage()
                    delay(cleanConfiguration.frequencyMilli)
                } catch (e: Exception) {
                    log.error("failed in prioritization clean scheduler", e)
                }
            }
        }
    }
}
