/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2018 - 2019 IBM, Bell Canada.
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
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils

/**
 * Register the Netconf module exposed dependency
 */
fun BluePrintDependencyService.netconfClientService(): ResourceResolutionService =
    instance(ResourceResolutionConstants.SERVICE_RESOURCE_RESOLUTION)

fun AbstractComponentFunction.netconfDevice(requirementName: String): NetconfDevice {
    val deviceInfo = netconfDeviceInfo(requirementName)
    return NetconfDevice(deviceInfo)
}

fun AbstractComponentFunction.netconfDeviceInfo(requirementName: String): DeviceInfo {

    val blueprintContext = bluePrintRuntimeService.bluePrintContext()

    val requirement = blueprintContext.nodeTemplateRequirement(nodeTemplateName, requirementName)

    val capabilityProperties = bluePrintRuntimeService.resolveNodeTemplateCapabilityProperties(
        requirement
            .node!!,
        requirement.capability!!
    )

    return netconfDeviceInfo(capabilityProperties)
}

private fun AbstractComponentFunction.netconfDeviceInfo(capabilityProperty: MutableMap<String, JsonNode>): DeviceInfo {
    return JacksonUtils.getInstanceFromMap(capabilityProperty, DeviceInfo::class.java)
}

/**
 * Blocking Methods called from Jython Scripts
 */
