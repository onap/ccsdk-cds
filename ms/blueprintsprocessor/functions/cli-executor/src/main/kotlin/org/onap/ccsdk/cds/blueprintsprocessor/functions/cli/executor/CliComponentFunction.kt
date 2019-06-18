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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.cli.executor

import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.SshLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.service.BluePrintSshLibPropertyService
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.readNBLines
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintTemplateService

abstract class CliComponentFunction(private val bluePrintTemplateService: BluePrintTemplateService) : AbstractScriptComponentFunction() {

    open fun bluePrintSshLibPropertyService(): BluePrintSshLibPropertyService =
            functionDependencyInstanceAsType(SshLibConstants.SERVICE_BLUEPRINT_SSH_LIB_PROPERTY)

    open fun resourceResolutionService(): ResourceResolutionService =
            functionDependencyInstanceAsType(ResourceResolutionConstants.SERVICE_RESOURCE_RESOLUTION)


    open suspend fun readCommandLinesFromArtifact(artifactName: String): List<String> {
        val artifactDefinition = bluePrintRuntimeService.resolveNodeTemplateArtifactDefinition(nodeTemplateName, artifactName)
        val file = normalizedFile(bluePrintRuntimeService.bluePrintContext().rootPath, artifactDefinition.file)
        return file.readNBLines()
    }

    suspend fun generateMessage(artifactName: String, json: String): String {
        return bluePrintTemplateService.generateContent(bluePrintRuntimeService, nodeTemplateName, artifactName, json, true)
    }


    fun generateMessage(artifactName: String): String {
        return bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, artifactName)
    }

    fun resolveFromDatabase(resolutionKey: String, artifactName: String): String = runBlocking {
        resourceResolutionService().resolveFromDatabase(bluePrintRuntimeService, artifactName, resolutionKey)
    }

    fun resolveAndGenerateMessage(artifactMapping: String, artifactTemplate: String): String = runBlocking {
        resourceResolutionService().resolveResources(bluePrintRuntimeService, nodeTemplateName,
                artifactMapping, artifactTemplate)
    }

    fun resolveAndGenerateMessage(artifactPrefix: String): String = runBlocking {
        resourceResolutionService().resolveResources(bluePrintRuntimeService, nodeTemplateName,
                artifactPrefix, mapOf())
    }
}