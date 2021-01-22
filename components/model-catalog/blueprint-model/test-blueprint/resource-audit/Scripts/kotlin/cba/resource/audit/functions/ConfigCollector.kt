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

package cba.resource.audit.functions

import cba.resource.audit.deviceResourceDefinitions
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentScriptExecutor
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.logger

class ConfigCollector : AbstractScriptComponentFunction() {

    val log = logger(ConfigCollector::class)

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        val deviceId = bluePrintRuntimeService.getInputValue("device-id").textValue()
        val sources = bluePrintRuntimeService.getInputValue("sources")

        log.info("Processing Config Collection for device($deviceId), for sources($sources)")
        deviceResourceDefinitions.forEach { name, resourceDefinition ->
            log.info("collecting for the property : $name")
            resourceDefinition.sources.forEach { sourceName, source ->
                log.info("collecting for the Source : $sourceName")
            }
        }

        // Set the Attributes
        setAttribute(ComponentScriptExecutor.ATTRIBUTE_STATUS, BlueprintConstants.STATUS_SUCCESS.asJsonPrimitive())
        setAttribute(
            ComponentScriptExecutor.ATTRIBUTE_RESPONSE_DATA,
            """{
                      "port-speed" : "10MBS"
        }
            """.trimIndent().jsonAsJsonType()
        )
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
    }
}
