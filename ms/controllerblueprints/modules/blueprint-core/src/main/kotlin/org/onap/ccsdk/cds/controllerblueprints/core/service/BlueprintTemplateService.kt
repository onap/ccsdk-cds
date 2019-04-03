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
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTemplateService

class BluePrintTemplateService(val bluePrintRuntimeService: BluePrintRuntimeService<*>,
                               val nodeTemplateName: String, val artifactName: String):
        BlueprintTemplateService {

    override fun generateContent(template: String, json: String, ignoreJsonNull: Boolean, additionalContext: MutableMap<String, Any>): String {
        val templateType = getArtifactTypeName()
        return when (templateType) {
            BluePrintConstants.ARTIFACT_JINJA_TYPE_NAME -> {
                BluePrintJinjaTemplateService.generateContent(template, json, ignoreJsonNull, additionalContext)
            }
            BluePrintConstants.ARTIFACT_VELOCITY_TYPE_NAME -> {
                BluePrintVelocityTemplateService.generateContent(template, json, ignoreJsonNull, additionalContext)
            }
            else -> {
                throw BluePrintProcessorException("Couldn't find artifact type")
            }
        }
    }

    override fun generateContentFromFiles(templatePath: String, jsonPath: String, ignoreJsonNull: Boolean, additionalContext: MutableMap<String, Any>): String {
        val templateType = getArtifactTypeName()
        return when (templateType) {
            BluePrintConstants.ARTIFACT_JINJA_TYPE_NAME -> {
                BluePrintJinjaTemplateService.generateContentFromFiles(templatePath, jsonPath, ignoreJsonNull, additionalContext)
            }
            BluePrintConstants.ARTIFACT_VELOCITY_TYPE_NAME -> {
                BluePrintVelocityTemplateService.generateContent(templatePath, jsonPath, ignoreJsonNull, additionalContext)
            }
            else -> {
                throw BluePrintProcessorException("Couldn't find artifact type")
            }
        }
    }

    private fun getArtifactTypeName(): String {
        val artifactDefinition = bluePrintRuntimeService.resolveNodeTemplateArtifactDefinition(nodeTemplateName, artifactName)
        return artifactDefinition.type
    }

}