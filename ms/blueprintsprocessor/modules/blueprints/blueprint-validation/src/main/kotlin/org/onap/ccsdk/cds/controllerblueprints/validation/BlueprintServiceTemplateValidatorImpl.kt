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

package org.onap.ccsdk.cds.controllerblueprints.validation

import com.google.common.base.Preconditions
import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintError
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.TopologyTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintServiceTemplateValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service("default-service-template-validator")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BlueprintServiceTemplateValidatorImpl(private val bluePrintTypeValidatorService: BlueprintTypeValidatorService) :
    BlueprintServiceTemplateValidator {

    private val log = LoggerFactory.getLogger(BlueprintServiceTemplateValidatorImpl::class.toString())

    lateinit var bluePrintRuntimeService: BlueprintRuntimeService<*>
    lateinit var error: BlueprintError

    var paths: MutableList<String> = arrayListOf()

    override fun validate(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, serviceTemplate: ServiceTemplate) {
        log.trace("Validating Service Template..")
        try {
            this.bluePrintRuntimeService = bluePrintRuntimeService
            this.error = bluePrintRuntimeService.getBlueprintError()

            serviceTemplate.metadata?.let { validateMetadata(serviceTemplate.metadata!!) }
            serviceTemplate.dataTypes?.let { validateDataTypes(serviceTemplate.dataTypes!!) }
            serviceTemplate.artifactTypes?.let { validateArtifactTypes(serviceTemplate.artifactTypes!!) }
            serviceTemplate.nodeTypes?.let { validateNodeTypes(serviceTemplate.nodeTypes!!) }
            serviceTemplate.topologyTemplate?.let { validateTopologyTemplate(serviceTemplate.topologyTemplate!!) }
        } catch (e: Exception) {
            log.error("failed in blueprint service template validation", e)
            error.addError(BlueprintConstants.PATH_SERVICE_TEMPLATE, paths.joinToString(BlueprintConstants.PATH_DIVIDER), e.message!!)
        }
    }

    fun validateMetadata(metaDataMap: MutableMap<String, String>) {

        paths.add(BlueprintConstants.PATH_METADATA)

        val templateName = metaDataMap[BlueprintConstants.METADATA_TEMPLATE_NAME]
        val templateVersion = metaDataMap[BlueprintConstants.METADATA_TEMPLATE_VERSION]
        val templateTags = metaDataMap[BlueprintConstants.METADATA_TEMPLATE_TAGS]
        val templateAuthor = metaDataMap[BlueprintConstants.METADATA_TEMPLATE_AUTHOR]

        Preconditions.checkArgument(StringUtils.isNotBlank(templateName), "failed to get template name metadata")
        Preconditions.checkArgument(StringUtils.isNotBlank(templateVersion), "failed to get template version metadata")
        Preconditions.checkArgument(StringUtils.isNotBlank(templateTags), "failed to get template tags metadata")
        Preconditions.checkArgument(StringUtils.isNotBlank(templateAuthor), "failed to get template author metadata")

        paths.removeAt(paths.lastIndex)
    }

    fun validateDataTypes(dataTypes: MutableMap<String, DataType>) {

        paths.add(BlueprintConstants.PATH_DATA_TYPES)
        dataTypes.forEach { dataTypeName, dataType ->
            // Validate Single Data Type
            bluePrintTypeValidatorService.validateDataType(bluePrintRuntimeService, dataTypeName, dataType)
        }
        paths.removeAt(paths.lastIndex)
    }

    fun validateArtifactTypes(artifactTypes: MutableMap<String, ArtifactType>) {
        paths.add(BlueprintConstants.PATH_ARTIFACT_TYPES)
        artifactTypes.forEach { artifactName, artifactType ->
            // Validate Single Artifact Type
            bluePrintTypeValidatorService.validateArtifactType(bluePrintRuntimeService, artifactName, artifactType)
        }
        paths.removeAt(paths.lastIndex)
    }

    fun validateNodeTypes(nodeTypes: MutableMap<String, NodeType>) {
        paths.add(BlueprintConstants.PATH_NODE_TYPES)
        nodeTypes.forEach { nodeTypeName, nodeType ->
            // Validate Single Node Type
            bluePrintTypeValidatorService.validateNodeType(bluePrintRuntimeService, nodeTypeName, nodeType)
        }
        paths.removeAt(paths.lastIndex)
    }

    fun validateTopologyTemplate(topologyTemplate: TopologyTemplate) {
        paths.add(BlueprintConstants.PATH_TOPOLOGY_TEMPLATE)
        bluePrintTypeValidatorService.validateTopologyTemplate(bluePrintRuntimeService, "topologyTemplate", topologyTemplate)
        paths.removeAt(paths.lastIndex)
    }
}
