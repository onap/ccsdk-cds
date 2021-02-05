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

package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.utils

import io.micrometer.core.instrument.Tag
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Status
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class BlueprintProcessingUtilsTest {

    @Test
    fun `valid Http status codes should be produced for valid parameters`() {
        val httpStatusCode200 = determineHttpStatusCode(200)
        assertEquals(HttpStatus.OK, httpStatusCode200)

        val httpStatusCode500 = determineHttpStatusCode(500)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, httpStatusCode500)
    }

    @Test
    fun `Http status code 500 should be produced for any invalid parameter`() {
        val nonExistentHttpStatusCode = determineHttpStatusCode(999999)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, nonExistentHttpStatusCode)
    }

    @Test
    fun testCbaMetricExecutionInputTags() {
        val executionServiceInput = ExecutionServiceInput().apply {
            actionIdentifiers = ActionIdentifiers().apply {
                blueprintName = "bpName"
                blueprintVersion = "1.0.0"
                actionName = "bpAction"
            }
        }

        val expectedTags = mutableListOf(
            Tag.of(BlueprintConstants.METRIC_TAG_BP_NAME, executionServiceInput.actionIdentifiers.blueprintName),
            Tag.of(BlueprintConstants.METRIC_TAG_BP_VERSION, executionServiceInput.actionIdentifiers.blueprintVersion),
            Tag.of(BlueprintConstants.METRIC_TAG_BP_ACTION, executionServiceInput.actionIdentifiers.actionName)
        )

        val metricTag = cbaMetricTags(executionServiceInput)

        assertEquals(expectedTags, metricTag)
    }

    @Test
    fun testCbaMetricExecutionOutputTags() {
        val executionServiceOutput = ExecutionServiceOutput().apply {
            actionIdentifiers = ActionIdentifiers().apply {
                blueprintName = "bpName"
                blueprintVersion = "1.0.0"
                actionName = "bpAction"
            }
            status = Status().apply {
                code = 200
                message = "success"
            }
        }

        val expectedTags = mutableListOf(
            Tag.of(BlueprintConstants.METRIC_TAG_BP_NAME, executionServiceOutput.actionIdentifiers.blueprintName),
            Tag.of(BlueprintConstants.METRIC_TAG_BP_VERSION, executionServiceOutput.actionIdentifiers.blueprintVersion),
            Tag.of(BlueprintConstants.METRIC_TAG_BP_ACTION, executionServiceOutput.actionIdentifiers.actionName),
            Tag.of(BlueprintConstants.METRIC_TAG_BP_STATUS, executionServiceOutput.status.code.toString()),
            Tag.of(BlueprintConstants.METRIC_TAG_BP_OUTCOME, executionServiceOutput.status.message)
        )

        val metricTag = cbaMetricTags(executionServiceOutput)

        assertEquals(expectedTags, metricTag)
    }
}
