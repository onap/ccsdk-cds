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

package cba.e2e.it

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceResponse
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.netconfDevice
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.storedContentFromResolvedArtifactNB
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException

open class ConfigDeploy : AbstractScriptComponentFunction() {

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        val resolutionKey = getDynamicProperties("resolution-key").asText()
        val config = storedContentFromResolvedArtifactNB(resolutionKey, "config")

        val device = netconfDevice("netconf-connection")
        val rpc = device.netconfRpcService
        device.netconfSession.connect()
        try {
            rpc.lock().requireSuccess("lock")
            rpc.editConfig(config).requireSuccess("edit-config")
            rpc.commit().requireSuccess("commit")
            rpc.unLock().requireSuccess("unlock")
        } finally {
            device.netconfSession.disconnect()
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        addError("Config deploy failed: ${runtimeException.message}")
    }

    private fun DeviceResponse.requireSuccess(operation: String) {
        if (!isSuccess()) {
            throw BluePrintProcessorException("$operation failed: $errorMessage")
        }
    }
}
