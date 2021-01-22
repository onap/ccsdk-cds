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

package org.onap.ccsdk.cds.controllerblueprints.core

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.slf4j.MDC
import kotlin.test.Test
import kotlin.test.assertEquals

class MDCContextTest {

    val log = logger(MDCContextTest::class)

    @Before
    fun setup() {
        MDC.clear()
    }

    @After
    fun tearDow() {
        MDC.clear()
    }

    @Test
    fun testContextCanBePassedBetweenCoroutines() {
        MDC.put(BlueprintConstants.ONAP_REQUEST_ID, "12345")
        runBlocking {
            GlobalScope.launch {
                assertEquals(null, MDC.get(BlueprintConstants.ONAP_REQUEST_ID))
            }
            launch(MDCContext()) {
                assertEquals(
                    "12345", MDC.get(BlueprintConstants.ONAP_REQUEST_ID),
                    "couldn't get request id"
                )

                MDC.put("client_id", "client-1")
                assertEquals("client-1", MDC.get("client_id"), "couldn't get client id")
            }
        }
    }
}
