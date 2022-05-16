/*
 * Copyright © 2022 Bell Canada
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants.INPUT_ARTIFACT_PREFIX_NAMES
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_ID
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionDBService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.TemplateResolutionService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asObjectNode
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component("component-resource-deletion")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ResourceDeletionComponent(
    private val resourceResolutionDBService: ResourceResolutionDBService,
    private val templateResolutionService: TemplateResolutionService
) : AbstractComponentFunction() {

    companion object {
        const val INPUT_LAST_N_OCCURRENCES = "last-n-occurrences"
        const val INPUT_FAIL_ON_EMPTY = "fail-on-empty"
        const val ATTRIBUTE_RESULT = "result"
        const val ATTRIBUTE_SUCCESS = "success"
    }

    data class DeletionResult(val nDeletedTemplates: Int, val nDeletedResources: Int)

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        bluePrintRuntimeService.setNodeTemplateAttributeValue(
            nodeTemplateName, ATTRIBUTE_RESULT, emptyMap<String, Any>().asJsonNode()
        )
        bluePrintRuntimeService.setNodeTemplateAttributeValue(
            nodeTemplateName, ATTRIBUTE_SUCCESS, false.asJsonPrimitive()
        )

        val resolutionKey = getOptionalOperationInput(RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY)?.textValue() ?: ""
        val resourceId = getOptionalOperationInput(RESOURCE_RESOLUTION_INPUT_RESOURCE_ID)?.textValue() ?: ""
        val resourceType = getOptionalOperationInput(RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE)?.textValue() ?: ""

        val resultMap = when {
            resolutionKey.isNotBlank() -> runDelete(byResolutionKey(resolutionKey))
            resourceType.isNotBlank() && resourceId.isNotBlank() ->
                runDelete(byResourceTypeAndId(resourceType, resourceId))
            else -> throw BluePrintProcessorException(
                "Please use resolution-key OR resource-type + resource-id. Values must not be blank"
            )
        }
        bluePrintRuntimeService.setNodeTemplateAttributeValue(
            nodeTemplateName, ATTRIBUTE_RESULT, resultMap.asObjectNode()
        )

        getOptionalOperationInput(INPUT_FAIL_ON_EMPTY)?.booleanValue().takeIf { it == true }?.let {
            resultMap.all { it.value.nDeletedResources == 0 && it.value.nDeletedTemplates == 0 }
                .takeIf { it }?.let {
                    throw BluePrintProcessorException("No templates or resources were deleted")
                }
        }

        bluePrintRuntimeService.setNodeTemplateAttributeValue(
            nodeTemplateName, ATTRIBUTE_SUCCESS, true.asJsonPrimitive()
        )
    }

    private suspend fun runDelete(fn: suspend (String, String, String, Int?) -> DeletionResult):
        Map<String, DeletionResult> {
            val metadata = bluePrintRuntimeService.bluePrintContext().metadata!!
            val blueprintVersion = metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION]!!
            val blueprintName = metadata[BluePrintConstants.METADATA_TEMPLATE_NAME]!!
            val artifactPrefixNamesNode = getOperationInput(INPUT_ARTIFACT_PREFIX_NAMES)
            val artifactPrefixNames = JacksonUtils.getListFromJsonNode(artifactPrefixNamesNode, String::class.java)
            val lastN = getOptionalOperationInput(INPUT_LAST_N_OCCURRENCES)?.let {
                if (it.isInt) it.intValue() else null
            }

            return artifactPrefixNames.associateWith { fn(blueprintName, blueprintVersion, it, lastN) }
        }

    private fun byResolutionKey(resolutionKey: String):
        suspend (String, String, String, Int?) -> DeletionResult = {
            bpName, bpVersion, artifactName, lastN ->
            val nDeleteTemplates = templateResolutionService.deleteTemplates(
                bpName, bpVersion, artifactName, resolutionKey, lastN
            )
            val nDeletedResources = resourceResolutionDBService.deleteResources(
                bpName, bpVersion, artifactName, resolutionKey, lastN
            )
            DeletionResult(nDeleteTemplates, nDeletedResources)
        }

    private fun byResourceTypeAndId(resourceType: String, resourceId: String):
        suspend (String, String, String, Int?) -> DeletionResult = {
            bpName, bpVersion, artifactName, lastN ->
            val nDeletedTemplates = templateResolutionService.deleteTemplates(
                bpName, bpVersion, artifactName, resourceType, resourceId, lastN
            )
            val nDeletedResources = resourceResolutionDBService.deleteResources(
                bpName, bpVersion, artifactName, resourceType, resourceId, lastN
            )
            DeletionResult(nDeletedTemplates, nDeletedResources)
        }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        addError(runtimeException.message ?: "Failed in ResourceDeletionComponent")
    }
}
