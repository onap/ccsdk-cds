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

package org.onap.ccsdk.apps.controllerblueprints.service.enhancer

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintError
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.apps.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.apps.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.apps.controllerblueprints.core.format
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintNodeTemplateEnhancer
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintRepoService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BluePrintNodeTemplateEnhancerImpl(private val bluePrintRepoService: BluePrintRepoService,
                                             private val bluePrintTypeEnhancerService: BluePrintTypeEnhancerService)
    : BluePrintNodeTemplateEnhancer {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintNodeTemplateEnhancerImpl::class.toString())

    lateinit var bluePrintContext: BluePrintContext
    lateinit var error: BluePrintError

    override fun enhance(bluePrintContext: BluePrintContext, error: BluePrintError, name: String, nodeTemplate: NodeTemplate) {
        log.info("Enhancing NodeTemplate($name)")
        this.bluePrintContext = bluePrintContext
        this.error = error

        val nodeTypeName = nodeTemplate.type
        // Get NodeType from Repo and Update Service Template
        val nodeType = populateNodeType(nodeTypeName)

        // Enrich NodeType
        bluePrintTypeEnhancerService.enhanceNodeType(bluePrintContext, error, nodeTypeName, nodeType)

        //Enrich Node Template Artifacts
        enhanceNodeTemplateArtifactDefinition(name, nodeTemplate)
    }


    open fun populateNodeType(nodeTypeName: String): NodeType {

        val nodeType = bluePrintContext.serviceTemplate.nodeTypes?.get(nodeTypeName)
                ?: bluePrintRepoService.getNodeType(nodeTypeName)
                ?: throw BluePrintException(format("Couldn't get NodeType({}) from repo.", nodeTypeName))
        bluePrintContext.serviceTemplate.nodeTypes?.put(nodeTypeName, nodeType)
        return nodeType
    }

    open fun enhanceNodeTemplateArtifactDefinition(nodeTemplateName: String, nodeTemplate: NodeTemplate) {

        nodeTemplate.artifacts?.forEach { artifactDefinitionName, artifactDefinition ->
            val artifactTypeName = artifactDefinition.type
                    ?: throw BluePrintException(format("Artifact type is missing for NodeTemplate({}) artifact({})", nodeTemplateName, artifactDefinitionName))

            // Populate Artifact Type
            populateArtifactType(artifactTypeName)
        }
    }

    open fun populateArtifactType(artifactTypeName: String): ArtifactType {
        val artifactType = bluePrintContext.serviceTemplate.artifactTypes?.get(artifactTypeName)
                ?: bluePrintRepoService.getArtifactType(artifactTypeName)
                ?: throw BluePrintException(format("Couldn't get ArtifactType({}) from repo.", artifactTypeName))
        bluePrintContext.serviceTemplate.artifactTypes?.put(artifactTypeName, artifactType)
        return artifactType
    }

}