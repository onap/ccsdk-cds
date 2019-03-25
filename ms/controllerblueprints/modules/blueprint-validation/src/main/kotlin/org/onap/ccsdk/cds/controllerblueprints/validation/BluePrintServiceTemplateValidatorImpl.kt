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

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import com.google.common.base.Preconditions
import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintError
import org.onap.ccsdk.cds.controllerblueprints.core.data.*
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintServiceTemplateValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service("default-service-template-validator")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BluePrintServiceTemplateValidatorImpl(private val bluePrintTypeValidatorService: BluePrintTypeValidatorService) : BluePrintServiceTemplateValidator {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintServiceTemplateValidatorImpl::class.toString())

    lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>
    lateinit var error: BluePrintError

    var paths: MutableList<String> = arrayListOf()

    override fun validate(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, serviceTemplate: ServiceTemplate) {
        log.trace("Validating Service Template..")
        try {
            this.bluePrintRuntimeService = bluePrintRuntimeService
            this.error = bluePrintRuntimeService.getBluePrintError()

            serviceTemplate.metadata?.let { validateMetadata(serviceTemplate.metadata!!) }
            serviceTemplate.dataTypes?.let { validateDataTypes(serviceTemplate.dataTypes!!) }
            serviceTemplate.artifactTypes?.let { validateArtifactTypes(serviceTemplate.artifactTypes!!) }
            serviceTemplate.nodeTypes?.let { validateNodeTypes(serviceTemplate.nodeTypes!!) }
            serviceTemplate.topologyTemplate?.let { validateTopologyTemplate(serviceTemplate.topologyTemplate!!) }
        } catch (e: Exception) {
            log.error("failed in blueprint service template validation", e)
            error.addError(BluePrintConstants.PATH_SERVICE_TEMPLATE, paths.joinToString(BluePrintConstants.PATH_DIVIDER), e.message!!)
        }
    }

    fun validateMetadata(metaDataMap: MutableMap<String, String>) {

        paths.add(BluePrintConstants.PATH_METADATA)

        val templateName = metaDataMap[BluePrintConstants.METADATA_TEMPLATE_NAME]
        val templateVersion = metaDataMap[BluePrintConstants.METADATA_TEMPLATE_VERSION]
        val templateTags = metaDataMap[BluePrintConstants.METADATA_TEMPLATE_TAGS]
        val templateAuthor = metaDataMap[BluePrintConstants.METADATA_TEMPLATE_AUTHOR]

        Preconditions.checkArgument(StringUtils.isNotBlank(templateName), "failed to get template name metadata")
        Preconditions.checkArgument(StringUtils.isNotBlank(templateVersion), "failed to get template version metadata")
        Preconditions.checkArgument(StringUtils.isNotBlank(templateTags), "failed to get template tags metadata")
        Preconditions.checkArgument(StringUtils.isNotBlank(templateAuthor), "failed to get template author metadata")

        paths.removeAt(paths.lastIndex)
    }


    fun validateDataTypes(dataTypes: MutableMap<String, DataType>) {

        paths.add(BluePrintConstants.PATH_DATA_TYPES)
        dataTypes.forEach { dataTypeName, dataType ->
            // Validate Single Data Type
            bluePrintTypeValidatorService.validateDataType(bluePrintRuntimeService, dataTypeName, dataType)
        }
        paths.removeAt(paths.lastIndex)
    }

    fun validateArtifactTypes(artifactTypes: MutableMap<String, ArtifactType>) {
        paths.add(BluePrintConstants.PATH_ARTIFACT_TYPES)
        artifactTypes.forEach { artifactName, artifactType ->
            // Validate Single Artifact Type
            bluePrintTypeValidatorService.validateArtifactType(bluePrintRuntimeService, artifactName, artifactType)
        }
        paths.removeAt(paths.lastIndex)
    }

    fun validateNodeTypes(nodeTypes: MutableMap<String, NodeType>) {
        paths.add(BluePrintConstants.PATH_NODE_TYPES)
        nodeTypes.forEach { nodeTypeName, nodeType ->
            // Validate Single Node Type
            bluePrintTypeValidatorService.validateNodeType(bluePrintRuntimeService, nodeTypeName, nodeType)
        }
        paths.removeAt(paths.lastIndex)
    }

    fun validateTopologyTemplate(topologyTemplate: TopologyTemplate) {
        paths.add(BluePrintConstants.PATH_TOPOLOGY_TEMPLATE)
        bluePrintTypeValidatorService.validateTopologyTemplate(bluePrintRuntimeService, "topologyTemplate", topologyTemplate)
        paths.removeAt(paths.lastIndex)
    }
}