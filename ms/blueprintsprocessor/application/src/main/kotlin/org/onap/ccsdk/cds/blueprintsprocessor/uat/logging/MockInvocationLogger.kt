/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.cds.blueprintsprocessor.uat.logging

import org.mockito.listeners.InvocationListener
import org.mockito.listeners.MethodInvocationReport
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import java.util.concurrent.atomic.AtomicInteger

/**
 * Logs all Mockito's mock/spy invocations.
 *
 * Used for debugging interactions with a mock.
 */
class MockInvocationLogger(private val marker: Marker) : InvocationListener {

    private val mockInvocationsCounter = AtomicInteger()

    override fun reportInvocation(report: MethodInvocationReport) {
        val sb = StringBuilder()
        sb.appendln("Method invocation #${mockInvocationsCounter.incrementAndGet()} on mock/spy")
        report.locationOfStubbing?.let { location ->
            sb.append(INDENT).append("stubbed ").appendln(location)
        }
        sb.appendln(report.invocation)
        sb.append(INDENT).append("invoked ").appendln(report.invocation.location)
        if (report.threwException()) {
            sb.append(INDENT).append("has thrown -> ").append(report.throwable.javaClass.name)
            report.throwable.message?.let { message ->
                sb.append(" with message ").append(message)
            }
            sb.appendln()
        } else {
            sb.append(INDENT).append("has returned -> \"").append(report.returnedValue).append('"')
            report.returnedValue?.let { value ->
                sb.append(" (").append(value.javaClass.name).append(')')
            }
            sb.appendln()
        }
        log.info(marker, sb.toString())
    }

    companion object {

        private const val INDENT = "    "
        private val log = LoggerFactory.getLogger(MockInvocationLogger::class.java)
    }
}
