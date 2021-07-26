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

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.config.BluePrintLoadConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTemplateService
import org.springframework.stereotype.Service

@Service
class BluePrintTemplateService(private val bluePrintLoadConfiguration: BluePrintLoadConfiguration) :
    BlueprintTemplateService {

    override suspend fun generateContent(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
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
            BluePrintConstants.MODEL_TYPE_ARTIFACT_TEMPLATE_JINJA -> {
                BluePrintJinjaTemplateService.generateContent(
                    template,
                    jsonData,
                    ignoreJsonNull,
                    additionalContext,
                    bluePrintLoadConfiguration,
                    bluePrintRuntimeService.bluePrintContext().name(),
                    bluePrintRuntimeService.bluePrintContext().version()
                )
            }
            BluePrintConstants.MODEL_TYPE_ARTIFACT_TEMPLATE_VELOCITY -> {
                BluePrintVelocityTemplateService.generateContent(template, jsonData, ignoreJsonNull, additionalContext)
            }
            else -> {
                throw BluePrintProcessorException(
                    "Unknown Artifact type, expecting ${BluePrintConstants.MODEL_TYPE_ARTIFACT_TEMPLATE_JINJA}" +
                        "or ${BluePrintConstants.MODEL_TYPE_ARTIFACT_TEMPLATE_VELOCITY}"
                )
            }
        }
    }
}
