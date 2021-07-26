/*
 * Copyright © 2021 Bell Canada.
 *
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
 */

package org.onap.ccsdk.cds.blueprintsprocessor.message.utils

import io.micrometer.core.instrument.Tag
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants

import kotlin.test.assertEquals

class BlueprintMessageUtilsTest {

    @Test
    fun testKafkaMetricTag() {
        val expected = mutableListOf<Tag>(
            Tag.of(BluePrintConstants.METRIC_TAG_TOPIC, "my-topic")
        )
        val tags = BlueprintMessageUtils.kafkaMetricTag("my-topic")

        assertEquals(expected, tags)
    }

    @Test
    fun testGetHostnameSuffix() {
        mockkStatic(System::class)
        every { System.getenv("HOSTNAME") } returns "qwertyuiop"
        assertEquals("yuiop", BlueprintMessageUtils.getHostnameSuffix())
    }

    @Test
    fun testGetNullHostnameSuffix() {
        mockkStatic(System::class)
        every { System.getenv("HOSTNAME") } returns null
        assertEquals(5, BlueprintMessageUtils.getHostnameSuffix().length)
    }

    @Test
    fun testGetMessageLogData() {
        var header = CommonHeader().apply {
            requestId = "myrequestid"
            subRequestId = "mysubrequestid"
        }

        val input = ExecutionServiceInput().apply {
            actionIdentifiers = ActionIdentifiers().apply {
                blueprintName = "bpInput"
                blueprintVersion = "1.0.0-input"
                actionName = "bpActionInput"
            }
            commonHeader = header
        }
        val expectedOnInput = "requestID(myrequestid), subrequestID(mysubrequestid) CBA(bpInput/1.0.0-input/bpActionInput)"

        val output = ExecutionServiceInput().apply {
            actionIdentifiers = ActionIdentifiers().apply {
                blueprintName = "bpOutput"
                blueprintVersion = "1.0.0-output"
                actionName = "bpActionOutput"
            }
            commonHeader = header
        }
        val expectedOnOutput = "requestID(myrequestid), subrequestID(mysubrequestid) CBA(bpOutput/1.0.0-output/bpActionOutput)"

        val otherMessage = "some other message"
        val expectedOnOtherMessage = "message(some other message)"

        assertEquals(expectedOnInput, BlueprintMessageUtils.getMessageLogData(input))
        assertEquals(expectedOnOutput, BlueprintMessageUtils.getMessageLogData(output))
        assertEquals(expectedOnOtherMessage, BlueprintMessageUtils.getMessageLogData(otherMessage))
    }
}
