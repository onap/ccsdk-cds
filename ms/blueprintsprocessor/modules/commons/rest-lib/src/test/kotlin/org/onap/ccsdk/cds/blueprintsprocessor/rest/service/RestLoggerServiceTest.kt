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

package org.onap.ccsdk.cds.blueprintsprocessor.rest.service

import org.apache.http.message.BasicHeader
import org.junit.After
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.slf4j.MDC
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RestLoggerServiceTest {

    @After
    fun clearMdc() {
        MDC.clear()
    }

    @Test
    fun `httpInvoking adds the ONAP headers and keeps the supplied ones`() {
        MDC.put("InvocationID", "inbound-invocation-id")

        val headers = RestLoggerService.httpInvoking(arrayOf(BasicHeader("Accept", "application/json")))
            .associate { it.name to it.value }

        assertEquals("application/json", headers["Accept"])
        assertEquals("inbound-invocation-id", headers[BluePrintConstants.ONAP_REQUEST_ID])
        assertNotNull(headers[BluePrintConstants.ONAP_INVOCATION_ID])
        assertEquals(BluePrintConstants.APP_NAME, headers[BluePrintConstants.ONAP_PARTNER_NAME])
    }

    @Test
    fun `httpInvoking generates a request id when there is no inbound invocation id`() {
        val headers = RestLoggerService.httpInvoking(emptyArray()).associate { it.name to it.value }

        assertNotNull(headers[BluePrintConstants.ONAP_REQUEST_ID])
    }

    @Test
    fun `httpInvoking does not override a request id supplied by the caller`() {
        MDC.put("InvocationID", "inbound-invocation-id")

        val headers = RestLoggerService.httpInvoking(arrayOf(BasicHeader("x-onap-requestid", "caller-request-id")))

        assertEquals(1, headers.count { it.name.equals(BluePrintConstants.ONAP_REQUEST_ID, ignoreCase = true) })
        assertEquals("caller-request-id", headers.single { it.name == "x-onap-requestid" }.value)
    }
}
