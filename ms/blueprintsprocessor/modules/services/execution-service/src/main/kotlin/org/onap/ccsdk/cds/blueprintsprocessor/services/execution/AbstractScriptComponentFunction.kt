/*
 *  Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.slf4j.LoggerFactory

abstract class AbstractScriptComponentFunction : AbstractComponentFunction() {

    private val log = LoggerFactory.getLogger(AbstractScriptComponentFunction::class.java)!!

    companion object {

        const val DYNAMIC_PROPERTIES = "dynamic-properties"
    }

    lateinit var scriptType: String

    /**
     * Store Dynamic Script Dependency Instances, Objects present inside won't be persisted or state maintained.
     */
    @Deprecated("Dependencies will be resolved dynamically")
    var functionDependencyInstances: MutableMap<String, Any> = hashMapOf()

    /**
     * This will be called from the scripts to serve instance from runtime to scripts.
     */
    @Deprecated("Dependencies will be resolved dynamically")
    open fun <T> functionDependencyInstanceAsType(name: String): T {
        return functionDependencyInstances[name] as? T
            ?: throw BluePrintProcessorException("couldn't get script property instance ($name)")
    }

    fun checkDynamicProperties(key: String): Boolean {
        return operationInputs[DYNAMIC_PROPERTIES]?.has(key) ?: false
    }

    fun getDynamicProperties(key: String): JsonNode {
        return operationInputs[DYNAMIC_PROPERTIES]!!.get(key)
    }

    suspend fun executeScript(executionServiceInput: ExecutionServiceInput) {
        return when (scriptType) {
            BluePrintConstants.SCRIPT_JYTHON -> {
                executeScriptBlocking(executionServiceInput)
            }
            else -> {
                executeScriptNB(executionServiceInput)
            }
        }
    }

    private suspend fun executeScriptNB(executionServiceInput: ExecutionServiceInput) {
        try {
            processNB(executionServiceInput)
        } catch (runtimeException: RuntimeException) {
            log.error("failed in ${getName()} : ${runtimeException.message}", runtimeException)
            recoverNB(runtimeException, executionServiceInput)
        }
    }

    private fun executeScriptBlocking(executionServiceInput: ExecutionServiceInput) {
        try {
            process(executionServiceInput)
        } catch (runtimeException: RuntimeException) {
            log.error("failed in ${getName()} : ${runtimeException.message}", runtimeException)
            recover(runtimeException, executionServiceInput)
        }
    }

    /**
     * If Jython Script, Override Blocking methods(process() and recover())
     * If Kotlin or Internal Scripts, Override non blocking methods ( processNB() and recoverNB()), so default
     * blocking
     * methods will have default implementation,
     *
     * Always applyNB() method will be invoked, apply() won't be called from parent
     */

    final override fun apply(executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput {
        throw BluePrintException("Not Implemented, use applyNB method")
    }

    final override fun prepareRequest(executionRequest: ExecutionServiceInput): ExecutionServiceInput {
        throw BluePrintException("Not Implemented required")
    }

    final override fun prepareResponse(): ExecutionServiceOutput {
        throw BluePrintException("Not Implemented required")
    }

    final override suspend fun applyNB(executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput {
        throw BluePrintException("Not Implemented required")
    }

    final override suspend fun prepareRequestNB(executionRequest: ExecutionServiceInput): ExecutionServiceInput {
        throw BluePrintException("Not Implemented required")
    }

    final override suspend fun prepareResponseNB(): ExecutionServiceOutput {
        throw BluePrintException("Not Implemented required")
    }

    override fun process(executionRequest: ExecutionServiceInput) {
        throw BluePrintException("Not Implemented, child class will implement this")
    }

    override fun recover(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        throw BluePrintException("Not Implemented, child class will implement this")
    }
}
