/*
 *  Copyright © 2018-2019 IBM, Bell Canada.
 *  Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils

abstract class NetconfComponentFunction : AbstractScriptComponentFunction() {

    open fun resourceResolutionService(): ResourceResolutionService =
            functionDependencyInstanceAsType(ResourceResolutionConstants.SERVICE_RESOURCE_RESOLUTION)

    // Called from python script
    fun initializeNetconfConnection(requirementName: String): NetconfDevice {
        val deviceInfo = deviceProperties(requirementName)
        return NetconfDevice(deviceInfo)
    }

    fun generateMessage(artifactName: String): String {
        return bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, artifactName)
    }

    fun resolveFromDatabase(resolutionKey: String, artifactName: String): String = runBlocking {
        resourceResolutionService().resolveFromDatabase(bluePrintRuntimeService, artifactName, resolutionKey)
    }

    fun resolveAndGenerateMessage(artifactPrefix: String): String = runBlocking {
        resourceResolutionService().resolveResources(bluePrintRuntimeService, nodeTemplateName,
                artifactPrefix, mapOf())
    }

    private fun deviceProperties(requirementName: String): DeviceInfo {

        val blueprintContext = bluePrintRuntimeService.bluePrintContext()

        val requirement = blueprintContext.nodeTemplateRequirement(nodeTemplateName, requirementName)

        val capabilityProperties = bluePrintRuntimeService.resolveNodeTemplateCapabilityProperties(requirement
                .node!!, requirement.capability!!)

        return deviceProperties(capabilityProperties)
    }

    private fun deviceProperties(capabilityProperty: MutableMap<String, JsonNode>): DeviceInfo {
        return JacksonUtils.getInstanceFromMap(capabilityProperty, DeviceInfo::class.java)
    }
}