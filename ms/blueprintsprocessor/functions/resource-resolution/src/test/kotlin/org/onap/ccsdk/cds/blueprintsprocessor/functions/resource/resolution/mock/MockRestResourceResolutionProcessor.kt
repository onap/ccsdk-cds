/*
 * Copyright © 2019 IBM, Bell Canada.
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
package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.mock

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.RestResourceSource
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.RestResourceResolutionProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory

class MockRestResourceResolutionProcessor(
    private val blueprintRestLibPropertyService:
        MockBluePrintRestLibPropertyService
) : RestResourceResolutionProcessor(blueprintRestLibPropertyService) {

    private val logger = LoggerFactory.getLogger(MockRestResourceResolutionProcessor::class.java)

    override fun resolveInputKeyMappingVariables(
        inputKeyMapping: Map<String, String>,
        templatingConstants: Map<String, String>?
    ): Map<String, JsonNode> {
        this.raRuntimeService.putResolutionStore("service-instance-id", "10".asJsonPrimitive())
        this.raRuntimeService.putResolutionStore("vnf_name", "vnf1".asJsonPrimitive())
        this.raRuntimeService.putResolutionStore("vnf-id", "123456".asJsonPrimitive())
        return super.resolveInputKeyMappingVariables(inputKeyMapping, templatingConstants)
    }

    override fun getName(): String {
        return "${ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-rest"
    }

    override fun blueprintWebClientService(
        resourceAssignment: ResourceAssignment,
        restResourceSource: RestResourceSource
    ): BlueprintWebClientService {
        return blueprintRestLibPropertyService.mockBlueprintWebClientService(resourceAssignment.dictionarySource!!)
    }
}
