package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.utils

import io.micrometer.core.instrument.Tag
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Status
import org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.SelfServiceMetricConstants
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class UtilsTest {

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
            Tag.of(SelfServiceMetricConstants.TAG_BP_NAME, executionServiceInput.actionIdentifiers.blueprintName),
            Tag.of(SelfServiceMetricConstants.TAG_BP_VERSION, executionServiceInput.actionIdentifiers.blueprintVersion),
            Tag.of(SelfServiceMetricConstants.TAG_BP_ACTION, executionServiceInput.actionIdentifiers.actionName)
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
            Tag.of(SelfServiceMetricConstants.TAG_BP_NAME, executionServiceOutput.actionIdentifiers.blueprintName),
            Tag.of(SelfServiceMetricConstants.TAG_BP_VERSION, executionServiceOutput.actionIdentifiers.blueprintVersion),
            Tag.of(SelfServiceMetricConstants.TAG_BP_ACTION, executionServiceOutput.actionIdentifiers.actionName),
            Tag.of(SelfServiceMetricConstants.TAG_BP_STATUS, executionServiceOutput.status.code.toString()),
            Tag.of(SelfServiceMetricConstants.TAG_BP_OUTCOME, executionServiceOutput.status.message)
        )

        val metricTag = cbaMetricTags(executionServiceOutput)

        assertEquals(expectedTags, metricTag)
    }
}
