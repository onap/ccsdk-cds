/*
 *  Copyright © 2019 IBM, Bell Canada
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
package org.onap.ccsdk.cds.controllerblueprints.core.service

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.config.BlueprintLoadConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTemplateService
import org.springframework.stereotype.Service

@Service
class BlueprintTemplateService(private val bluePrintLoadConfiguration: BlueprintLoadConfiguration) :
    BlueprintTemplateService {

    override suspend fun generateContent(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        nodeTemplateName: String,
        artifactName: String,
        jsonData: String,
        ignoreJsonNull: Boolean,
        additionalContext: MutableMap<String, Any>
    ): String {

        val artifactDefinition =
            bluePrintRuntimeService.resolveNodeTemplateArtifactDefinition(nodeTemplateName, artifactName)
        val templateType = artifactDefinition.type
        val template = bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, artifactName)

        return when (templateType) {
            BlueprintConstants.MODEL_TYPE_ARTIFACT_TEMPLATE_JINJA -> {
                BlueprintJinjaTemplateService.generateContent(
                    template,
                    jsonData,
                    ignoreJsonNull,
                    additionalContext,
                    bluePrintLoadConfiguration,
                    bluePrintRuntimeService.bluePrintContext().name(),
                    bluePrintRuntimeService.bluePrintContext().version()
                )
            }
            BlueprintConstants.MODEL_TYPE_ARTIFACT_TEMPLATE_VELOCITY -> {
                BlueprintVelocityTemplateService.generateContent(template, jsonData, ignoreJsonNull, additionalContext)
            }
            else -> {
                throw BlueprintProcessorException(
                    "Unknown Artifact type, expecting ${BlueprintConstants.MODEL_TYPE_ARTIFACT_TEMPLATE_JINJA}" +
                        "or ${BlueprintConstants.MODEL_TYPE_ARTIFACT_TEMPLATE_VELOCITY}"
                )
            }
        }
    }
}
