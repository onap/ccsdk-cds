/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.apps.controllerblueprints.core.validation

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import com.google.common.base.Preconditions
import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintValidationError
import org.onap.ccsdk.apps.controllerblueprints.core.data.*
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintServiceTemplateValidator
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext

open class BluePrintServiceTemplateValidatorImpl(private val bluePrintTypeValidatorService: BluePrintTypeValidatorService) : BluePrintServiceTemplateValidator {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintServiceTemplateValidatorImpl::class.toString())

    var bluePrintContext: BluePrintContext? = null
    var error: BluePrintValidationError? = null

    override fun validate(bluePrintContext: BluePrintContext, error: BluePrintValidationError, name: String, serviceTemplate: ServiceTemplate) {
        log.info("Validating Service Template..")
        try {
            this.bluePrintContext = bluePrintContext
            this.error = error

            serviceTemplate.metadata?.let { validateMetadata(serviceTemplate.metadata!!) }
            serviceTemplate.dataTypes?.let { validateDataTypes(serviceTemplate.dataTypes!!) }
            serviceTemplate.artifactTypes?.let { validateArtifactTypes(serviceTemplate.artifactTypes!!) }
            serviceTemplate.nodeTypes?.let { validateNodeTypes(serviceTemplate.nodeTypes!!) }
            serviceTemplate.topologyTemplate?.let { validateTopologyTemplate(serviceTemplate.topologyTemplate!!) }
        } catch (e: Exception) {
            throw BluePrintException(e, "failed to validate blueprint with message ${e.message}")
        }
    }

    fun validateMetadata(metaDataMap: MutableMap<String, String>) {

        val templateName = metaDataMap[BluePrintConstants.METADATA_TEMPLATE_NAME]
        val templateVersion = metaDataMap[BluePrintConstants.METADATA_TEMPLATE_VERSION]
        val templateTags = metaDataMap[BluePrintConstants.METADATA_TEMPLATE_TAGS]
        val templateAuthor = metaDataMap[BluePrintConstants.METADATA_TEMPLATE_AUTHOR]

        Preconditions.checkArgument(StringUtils.isNotBlank(templateName), "failed to get template name metadata")
        Preconditions.checkArgument(StringUtils.isNotBlank(templateVersion), "failed to get template version metadata")
        Preconditions.checkArgument(StringUtils.isNotBlank(templateTags), "failed to get template tags metadata")
        Preconditions.checkArgument(StringUtils.isNotBlank(templateAuthor), "failed to get template author metadata")
    }


    fun validateDataTypes(dataTypes: MutableMap<String, DataType>) {
        dataTypes.forEach { dataTypeName, dataType ->
            // Validate Single Data Type
            bluePrintTypeValidatorService.validateDataType(bluePrintContext!!, error!!, dataTypeName, dataType)
        }
    }

    fun validateArtifactTypes(artifactTypes: MutableMap<String, ArtifactType>) {
        artifactTypes.forEach { artifactName, artifactType ->
            // Validate Single Artifact Type
            bluePrintTypeValidatorService.validateArtifactType(bluePrintContext!!, error!!, artifactName, artifactType)
        }
    }

    fun validateNodeTypes(nodeTypes: MutableMap<String, NodeType>) {
        nodeTypes.forEach { nodeTypeName, nodeType ->
            // Validate Single Node Type
            bluePrintTypeValidatorService.validateNodeType(bluePrintContext!!, error!!, nodeTypeName, nodeType)
        }
    }

    fun validateTopologyTemplate(topologyTemplate: TopologyTemplate) {
        bluePrintTypeValidatorService.validateTopologyTemplate(bluePrintContext!!, error!!, "topologyTemplate", topologyTemplate)
    }
}