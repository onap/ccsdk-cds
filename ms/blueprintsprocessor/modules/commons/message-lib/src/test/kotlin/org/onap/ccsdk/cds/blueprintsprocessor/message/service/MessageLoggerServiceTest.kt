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

package org.onap.ccsdk.cds.blueprintsprocessor.message.service

import io.mockk.every
import io.mockk.mockk
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.internals.RecordHeaders
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.cds.blueprintsprocessor.message.toMap
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.slf4j.MDC
import kotlin.test.assertEquals

class MessageLoggerServiceTest {

    @Test
    fun testMessagingHeaders() {
        val messageLoggerService = MessageLoggerService()
        val commonHeader = CommonHeader().apply {
            requestId = "1234"
            subRequestId = "1234-12"
            originatorId = "cds-test"
        }

        val consumerRecord = mockk<ConsumerRecord<*, *>>()
        every { consumerRecord.headers() } returns null
        every { consumerRecord.key() } returns "1234"
        every { consumerRecord.offset() } returns 12345
        every { consumerRecord.topic() } returns "sample-topic"
        every { consumerRecord.timestamp() } returns System.currentTimeMillis()
        messageLoggerService.messageConsuming(commonHeader, consumerRecord)
        assertEquals(commonHeader.requestId, MDC.get("RequestID"))
        assertEquals(commonHeader.subRequestId, MDC.get("InvocationID"))

        val mockHeaders = RecordHeaders()
        messageLoggerService.messageProducing(mockHeaders)
        val map = mockHeaders.toMap()
        assertEquals("1234-12", map[BlueprintConstants.ONAP_REQUEST_ID])

        messageLoggerService.messageConsumingExisting()
    }
}
