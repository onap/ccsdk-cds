/*
 *  Copyright © 2018 IBM.
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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.DeviceInfo
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.NetconfRpcClientService
import org.onap.ccsdk.apps.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils


abstract class NetconfComponentFunction : AbstractComponentFunction() {

    fun deviceProperties(requirementName: String): DeviceInfo {

        val blueprintContext = bluePrintRuntimeService.bluePrintContext()

        val requirement = blueprintContext.nodeTemplateRequirement(nodeTemplateName, requirementName)

        val capabilityProperties = bluePrintRuntimeService.resolveNodeTemplateCapabilityProperties(requirement
                .node!!, requirement.capability!!)

        return deviceProperties(capabilityProperties)
    }

    fun deviceProperties(capabilityProperty: MutableMap<String, JsonNode>): DeviceInfo {
        return JacksonUtils.getInstanceFromMap(capabilityProperty, DeviceInfo::class.java)
    }

    fun netconfRpcClientService(): NetconfRpcClientService {
        return NetconfRpcService()
    }

    fun netconfRpcClientService(requirementName: String): NetconfRpcClientService {
        val deviceProperties = deviceProperties(requirementName)
        val netconfRpcClientService = NetconfRpcService()
        netconfRpcClientService.connect(deviceProperties)
        return netconfRpcClientService
    }

    fun generateMessage(): String {
        TODO()
    }

    fun resolveAndGenerateMesssage(): String {
        TODO()
    }

}