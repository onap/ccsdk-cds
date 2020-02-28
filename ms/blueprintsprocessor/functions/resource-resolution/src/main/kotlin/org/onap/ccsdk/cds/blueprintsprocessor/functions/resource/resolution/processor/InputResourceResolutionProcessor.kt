/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *  Modifications Copyright © 2018 - 2020 IBM, Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor

import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.DatabaseResourceSource
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.error.BlueprintProcessorErrorCodes
import org.onap.ccsdk.cds.controllerblueprints.core.isNotEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.error.catalog.data.ErrorMessage
import org.onap.ccsdk.error.catalog.data.ErrorMessageLibConstants
import org.onap.ccsdk.error.catalog.interfaces.ErrorCatalogException
import org.onap.ccsdk.error.catalog.utils.errorCauseOrDefault
import org.onap.ccsdk.error.catalog.utils.errorMessageOrDefault
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

/**
 * InputResourceResolutionProcessor
 *
 * @author Kapil Singal
 */
@Service("${PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-input")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class InputResourceResolutionProcessor : ResourceAssignmentProcessor() {

    override fun getName(): String {
        return "${PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-input"
    }

    override suspend fun processNB(resourceAssignment: ResourceAssignment) {
        try {
            if (isNotEmpty(resourceAssignment.name) && !setFromInput(resourceAssignment)) {
                setFromKeyDependencies(resourceAssignment)
            }
            // Check the value has populated for mandatory case
            ResourceAssignmentUtils.assertTemplateKeyValueNotNull(resourceAssignment)
        } catch (e: ErrorCatalogException) {
            e.addErrorModel(ErrorMessage(BlueprintProcessorErrorCodes.RESOLUTION_FAILURE.domain,
                    "Error while processing Input Resource Resolution", null))
            throw e
        } catch (e: Exception) {
            ResourceAssignmentUtils.setFailedResourceDataValue(resourceAssignment, e.message)
            throw errorManager.generateException(BlueprintProcessorErrorCodes.RESOLUTION_FAILURE,
                    errorMessage = "Failed in template key ($resourceAssignment) " +
                    "assignments with : (${e.errorMessageOrDefault()})", errorCause = e.errorCauseOrDefault())
        }
    }

    // usecase: where input data attribute doesn't match with resourceName, and needs an alternate mapping provided under key-dependencies.
    private fun setFromKeyDependencies(resourceAssignment: ResourceAssignment) {
        val dName = resourceAssignment.dictionaryName!!
        val dSource = resourceAssignment.dictionarySource!!
        val resourceDefinition = resourceDefinition(dName)

        /** Check Resource Assignment has the source definitions, If not get from Resource Definition **/
        val resourceSource = resourceAssignment.dictionarySourceDefinition
            ?: resourceDefinition?.sources?.get(dSource)
            ?: throw errorManager.generateException(BlueprintProcessorErrorCodes.RESOLUTION_FAILURE,
                    errorMessage = "couldn't get resource definition $dName source($dSource)")
        val resourceSourceProperties = checkNotNull(resourceSource.properties) {
            "failed to get source properties for $dName "
        }
        val sourceProperties =
            JacksonUtils.getInstanceFromMap(resourceSourceProperties, DatabaseResourceSource::class.java)

        val keyDependency = checkNotNull(sourceProperties.keyDependencies) {
            "failed to get input-key-mappings for $dName under $dSource properties"
        }
        // keyDependency = service-instance.service-instance-id
        setFromInputKeyDependencies(keyDependency, resourceAssignment); // New API which picks arrtibute from Input
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, resourceAssignment: ResourceAssignment) {
        raRuntimeService.getBluePrintError().addError(runtimeException.message!!)
    }
}
