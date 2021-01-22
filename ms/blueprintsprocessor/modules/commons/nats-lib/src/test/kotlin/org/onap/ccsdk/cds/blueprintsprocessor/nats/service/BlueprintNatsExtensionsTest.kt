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

package org.onap.ccsdk.cds.blueprintsprocessor.nats.service

import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.nats.asJsonType
import org.onap.ccsdk.cds.blueprintsprocessor.nats.strData
import org.onap.ccsdk.cds.controllerblueprints.core.asByteArray
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType
import java.nio.charset.Charset
import kotlin.test.assertEquals

class BlueprintNatsExtensionsTest {

    @Test
    fun testMessageStrConversion() {
        val mockMessage = mockk<io.nats.client.Message>()
        every { mockMessage.data } returns "I am message".toByteArray(Charset.defaultCharset())

        val messageData = mockMessage.strData()
        assertEquals("I am message", messageData)
    }

    @Test
    fun testMessageJsonConversion() {
        val json = """{"name":"value"}"""

        val mockMessage = mockk<io.nats.client.Message>()
        every { mockMessage.data } returns json.jsonAsJsonType().asByteArray()

        val messageData = mockMessage.asJsonType().asJsonString()
        assertEquals(json, messageData)
    }
}
