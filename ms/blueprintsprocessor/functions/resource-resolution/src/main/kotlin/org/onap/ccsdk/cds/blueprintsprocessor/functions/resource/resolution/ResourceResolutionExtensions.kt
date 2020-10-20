/*
 *  Copyright © 2019 IBM.
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
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolution
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionDBService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService

/**
 * Register the Resolution module exposed dependency
 */
fun BluePrintDependencyService.resourceResolutionService(): ResourceResolutionService =
    instance(ResourceResolutionConstants.SERVICE_RESOURCE_RESOLUTION)

fun BluePrintDependencyService.resourceResolutionDBService(): ResourceResolutionDBService =
    instance(ResourceResolutionDBService::class.java)

suspend fun AbstractComponentFunction.storedContentFromResolvedArtifactNB(
    resolutionKey: String,
    artifactName: String
): String {
    return BluePrintDependencyService.resourceResolutionService()
        .resolveFromDatabase(bluePrintRuntimeService, artifactName, resolutionKey)
}

suspend fun AbstractComponentFunction.storedResourceResolutionsNB(
    resolutionKey: String,
    artifactName: String,
    occurrence: Int = 1
): List<ResourceResolution> {
    return BluePrintDependencyService.resourceResolutionDBService()
            .findByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKeyAndOccurrence(
            bluePrintRuntimeService,
            resolutionKey,
            occurrence,
            artifactName)
}

/**
 * Return resolved and mashed artifact content for artifact prefix [artifactPrefix]
 */
suspend fun AbstractComponentFunction.contentFromResolvedArtifactNB(artifactPrefix: String): String {
    return BluePrintDependencyService.resourceResolutionService()
        .resolveResources(bluePrintRuntimeService, nodeTemplateName, artifactPrefix, mapOf())
        .first
}

/**
 * Blocking Methods called from Jython Scripts
 */

fun AbstractComponentFunction.storedContentFromResolvedArtifact(resolutionKey: String, artifactName: String):
    String = runBlocking {
        storedContentFromResolvedArtifactNB(resolutionKey, artifactName)
    }

fun AbstractComponentFunction.contentFromResolvedArtifact(artifactPrefix: String): String = runBlocking {
    contentFromResolvedArtifactNB(artifactPrefix)
}
