/*
 *  Copyright © 20201 Bell Canada.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package cba.cds.uat

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.logger

open class TestScript : AbstractScriptComponentFunction() {

    private val log = logger(TestScript::class)

    private val FAILED = "FAILED".asJsonPrimitive()
    private val SUCCEEDED = "SUCCEEDED".asJsonPrimitive()

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        val failingSteps = inputValue("failing-steps")
        var shouldFail = false
        if (failingSteps?.has(this.stepName) == true) {
            shouldFail = failingSteps[this.stepName].asBoolean()
        }
        log.info("running step ${this.stepName}, should fail: $shouldFail")
        setAttribute("response-data", if (shouldFail) FAILED else SUCCEEDED)

        if (shouldFail) {
            throw BluePrintException("Step failed: ${this.stepName}")
        }
    }

    fun inputValue(name: String): JsonNode? {
        return try {
            return bluePrintRuntimeService.getInputValue(name)
        } catch (e: BluePrintProcessorException) { null }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("Executing Recovery for step ${this.stepName}")
        addError(runtimeException.message ?: "Failed without error message")
    }
}
