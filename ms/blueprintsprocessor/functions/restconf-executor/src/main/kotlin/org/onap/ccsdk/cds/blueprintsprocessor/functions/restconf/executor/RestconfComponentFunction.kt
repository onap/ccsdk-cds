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
@file:Suppress("unused")

package org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor

import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction


abstract class RestconfComponentFunction : AbstractScriptComponentFunction() {

    open fun bluePrintRestLibPropertyService(): BluePrintRestLibPropertyService =
            functionDependencyInstanceAsType(RestLibConstants.SERVICE_BLUEPRINT_REST_LIB_PROPERTY)

    open fun resourceResolutionService(): ResourceResolutionService =
            functionDependencyInstanceAsType(ResourceResolutionConstants.SERVICE_RESOURCE_RESOLUTION)


    fun restClientService(selector: String): BlueprintWebClientService {
        return bluePrintRestLibPropertyService().blueprintWebClientService(selector)
    }

    fun resolveFromDatabase(resolutionKey: String, artifactName: String): String {
        return resourceResolutionService().resolveFromDatabase(bluePrintRuntimeService, artifactName, resolutionKey)
    }

    fun generateMessage(artifactName: String): String {
        return bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, artifactName)
    }

    fun resolveAndGenerateMessage(artifactMapping: String, artifactTemplate: String): String {
        return resourceResolutionService().resolveResources(bluePrintRuntimeService, nodeTemplateName,
                artifactMapping, artifactTemplate)
    }

    fun resolveAndGenerateMessage(artifactPrefix: String): String {
        return resourceResolutionService().resolveResources(bluePrintRuntimeService, nodeTemplateName,
                artifactPrefix, mapOf())
    }
}