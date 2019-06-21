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

import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction

@Deprecated("Methods defined as extension function of AbstractComponentFunction")
abstract class RestconfComponentFunction : AbstractScriptComponentFunction() {

    @Deprecated("Defined in AbstractScriptComponentFunction extension class")
    open fun bluePrintRestLibPropertyService(): BluePrintRestLibPropertyService =
            functionDependencyInstanceAsType(RestLibConstants.SERVICE_BLUEPRINT_REST_LIB_PROPERTY)

    @Deprecated(" Use resourceResolutionService method directly",
            replaceWith = ReplaceWith("resourceResolutionService()",
                    "org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.resourceResolutionService"))
    open fun resourceResolutionService(): ResourceResolutionService =
            functionDependencyInstanceAsType(ResourceResolutionConstants.SERVICE_RESOURCE_RESOLUTION)


    @Deprecated("Defined in AbstractScriptComponentFunction extension class")
    fun restClientService(selector: String): BlueprintWebClientService {
        return bluePrintRestLibPropertyService().blueprintWebClientService(selector)
    }

    @Deprecated(" Use storedContentFromResolvedArtifact method directly",
            replaceWith = ReplaceWith("storedContentFromResolvedArtifact(resolutionKey, artifactName)",
                    "org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.storedContentFromResolvedArtifact"))
    fun resolveFromDatabase(resolutionKey: String, artifactName: String): String = runBlocking {
        resourceResolutionService().resolveFromDatabase(bluePrintRuntimeService, artifactName, resolutionKey)
    }

    @Deprecated(" Use artifactContent method directly",
            replaceWith = ReplaceWith("artifactContent(artifactName)",
                    "org.onap.ccsdk.cds.blueprintsprocessor.services.execution.artifactContent"))
    fun generateMessage(artifactName: String): String {
        return bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, artifactName)
    }

    @Deprecated(" Use contentFromResolvedArtifact method directly",
            replaceWith = ReplaceWith("resolveAndGenerateMessage(artifactPrefix)",
                    "org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.resolveAndGenerateMessage"))
    fun resolveAndGenerateMessage(artifactPrefix: String): String = runBlocking {
        resourceResolutionService().resolveResources(bluePrintRuntimeService, nodeTemplateName,
                artifactPrefix, mapOf())
    }
}