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

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTemplateService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils

class BluePrintTemplateService : BlueprintTemplateService {

    override suspend fun generateContent(bluePrintRuntimeService: BluePrintRuntimeService<*>,
                                         nodeTemplateName: String, artifactName: String, jsonData: String,
                                         ignoreJsonNull: Boolean, additionalContext: MutableMap<String, JsonNode>): String {

        val artifactDefinition = bluePrintRuntimeService.resolveNodeTemplateArtifactDefinition(nodeTemplateName, artifactName)
        val templateType = artifactDefinition.type
        val template = bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, artifactName)
        return generateContent(template, templateType, jsonData, ignoreJsonNull, additionalContext)
    }

    override suspend fun generateContent(template: String, templateType: String, jsonData: String, ignoreJsonNull: Boolean,
                                         additionalContext: MutableMap<String, JsonNode>): String {
        return when (templateType) {
            BluePrintConstants.ARTIFACT_JINJA_TYPE_NAME -> {
                BluePrintJinjaTemplateService.generateContent(template, jsonData, ignoreJsonNull, additionalContext)
            }
            BluePrintConstants.ARTIFACT_VELOCITY_TYPE_NAME -> {
                BluePrintVelocityTemplateService.generateContent(template, jsonData, ignoreJsonNull, additionalContext)
            }
            else -> {
                throw BluePrintProcessorException("Unknown Artifact type, expecting ${BluePrintConstants.ARTIFACT_JINJA_TYPE_NAME}" +
                        "or ${BluePrintConstants.ARTIFACT_VELOCITY_TYPE_NAME}")
            }
        }
    }

    suspend fun generateContentFromFiles(templatePath: String, templateType: String, jsonPath: String,
                                         ignoreJsonNull: Boolean,
                                         additionalContext: MutableMap<String, JsonNode>): String {
        val json = JacksonUtils.getClassPathFileContent(jsonPath)
        val template = JacksonUtils.getClassPathFileContent(templatePath)
        return generateContent(template, templateType, json, ignoreJsonNull, additionalContext)
    }
}