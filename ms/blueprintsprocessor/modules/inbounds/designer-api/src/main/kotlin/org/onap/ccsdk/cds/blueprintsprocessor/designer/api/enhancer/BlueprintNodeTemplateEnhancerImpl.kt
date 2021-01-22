/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.enhancer

import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.utils.BlueprintEnhancerUtils
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintNodeTemplateEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintRepoService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BlueprintNodeTemplateEnhancerImpl(
    private val bluePrintRepoService: BlueprintRepoService,
    private val bluePrintTypeEnhancerService: BlueprintTypeEnhancerService
) :
    BlueprintNodeTemplateEnhancer {

    private val log = logger(BlueprintNodeTemplateEnhancerImpl::class)

    lateinit var bluePrintRuntimeService: BlueprintRuntimeService<*>
    lateinit var bluePrintContext: BlueprintContext

    override fun enhance(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, nodeTemplate: NodeTemplate) {
        log.info("***** Enhancing NodeTemplate($name)")
        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val nodeTypeName = nodeTemplate.type
        // Get NodeType from Repo and Update Service Template
        val nodeType = BlueprintEnhancerUtils.populateNodeType(bluePrintContext, bluePrintRepoService, nodeTypeName)

        // Enrich NodeType
        bluePrintTypeEnhancerService.enhanceNodeType(bluePrintRuntimeService, nodeTypeName, nodeType)

        // Enrich Node Template Artifacts
        enhanceNodeTemplateArtifactDefinition(name, nodeTemplate)
    }

    open fun enhanceNodeTemplateArtifactDefinition(nodeTemplateName: String, nodeTemplate: NodeTemplate) {

        nodeTemplate.artifacts?.forEach { artifactDefinitionName, artifactDefinition ->
            // Enhance Artifacct Definitions
            bluePrintTypeEnhancerService.enhanceArtifactDefinition(bluePrintRuntimeService, artifactDefinitionName, artifactDefinition)
        }
    }
}
