/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution

import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component("component-resource-resolution")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ResourceResolutionComponent(private val resourceResolutionService: ResourceResolutionService) : AbstractComponentFunction() {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    override fun process(executionRequest: ExecutionServiceInput) {

        val artifactPrefixNamesNode = getOperationInput(ResourceResolutionConstants.INPUT_ARTIFACT_PREFIX_NAMES)

        val artifactPrefixNames = JacksonUtils.getListFromJsonNode(artifactPrefixNamesNode, String::class.java)
                ?: throw BluePrintProcessorException("coundn't transform ${artifactPrefixNamesNode.asText()} to string array")

        val resolvedParamContents = resourceResolutionService.resolveResources(bluePrintRuntimeService!!, nodeTemplateName, artifactPrefixNames)

        // Set Output Attributes
        bluePrintRuntimeService!!.setNodeTemplateAttributeValue(nodeTemplateName,
                ResourceResolutionConstants.OUTPUT_ASSIGNMENT_PARAMS, resolvedParamContents.asJsonNode())
    }

    override fun recover(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}