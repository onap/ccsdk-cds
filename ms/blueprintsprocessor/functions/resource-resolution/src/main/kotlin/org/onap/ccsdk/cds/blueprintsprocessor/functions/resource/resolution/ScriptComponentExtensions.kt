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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution

import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.core.service.DefaultBluePrintRuntimeService

abstract class ResourceResolutionScriptComponentFunction: AbstractComponentFunction() {
    fun getResourceResolutionService(): ResourceResolutionService {
        return BluePrintDependencyService.resourceResolutionService()
    }

    fun storedContentFromResolvedArtifactInternal(resolutionKey: String, artifactName: String)
            : String = runBlocking {
        storedContentFromResolvedArtifact(resolutionKey, artifactName)
    }

    fun contentFromResolvedArtifactInternal(artifactPrefix: String): String = runBlocking {
        contentFromResolvedArtifact(artifactPrefix)
    }

}

/**
 * Register the Resolution module exposed dependency
 */
fun BluePrintDependencyService.resourceResolutionService(): ResourceResolutionService =
        instance(ResourceResolutionConstants.SERVICE_RESOURCE_RESOLUTION)


suspend fun AbstractComponentFunction.storedContentFromResolvedArtifactNB(resolutionKey: String,
                                                                          artifactName: String): String {
    val resourceAssignmentRuntimeService = ResourceAssignmentUtils.transformToRARuntimeService(bluePrintRuntimeService,
            artifactName)
    return BluePrintDependencyService.resourceResolutionService()
            .resolveFromDatabase(resourceAssignmentRuntimeService, artifactName, resolutionKey)
}

/**
 * Return resolved and mashed artifact content for artifact prefix [artifactPrefix]
 */
suspend fun AbstractComponentFunction.contentFromResolvedArtifactNB(artifactPrefix: String): String {
    val resourceAssignmentRuntimeService = ResourceAssignmentUtils.transformToRARuntimeService(bluePrintRuntimeService,
            artifactPrefix)
    return BluePrintDependencyService.resourceResolutionService()
            .resolveResources(resourceAssignmentRuntimeService, nodeTemplateName, artifactPrefix, mapOf())
}

/**
 * Blocking Methods called from Jython Scripts
 */

fun AbstractComponentFunction.storedContentFromResolvedArtifact(resolutionKey: String, artifactName: String)
        : String = runBlocking {
    storedContentFromResolvedArtifactNB(resolutionKey, artifactName)
}

fun AbstractComponentFunction.contentFromResolvedArtifact(artifactPrefix: String): String = runBlocking {
    contentFromResolvedArtifactNB(artifactPrefix)
}