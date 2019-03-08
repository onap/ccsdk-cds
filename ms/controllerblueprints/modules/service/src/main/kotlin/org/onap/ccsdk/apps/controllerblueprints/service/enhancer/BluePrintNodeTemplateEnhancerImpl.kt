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

package org.onap.ccsdk.apps.controllerblueprints.service.enhancer

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintNodeTemplateEnhancer
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintRepoService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.apps.controllerblueprints.service.utils.BluePrintEnhancerUtils
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BluePrintNodeTemplateEnhancerImpl(private val bluePrintRepoService: BluePrintRepoService,
                                             private val bluePrintTypeEnhancerService: BluePrintTypeEnhancerService)
    : BluePrintNodeTemplateEnhancer {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintNodeTemplateEnhancerImpl::class.toString())

    lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>
    lateinit var bluePrintContext: BluePrintContext


    override fun enhance(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, nodeTemplate: NodeTemplate) {
        log.info("***** Enhancing NodeTemplate($name)")
        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.bluePrintContext = bluePrintRuntimeService.bluePrintContext()


        val nodeTypeName = nodeTemplate.type
        // Get NodeType from Repo and Update Service Template
        val nodeType = BluePrintEnhancerUtils.populateNodeType(bluePrintContext, bluePrintRepoService, nodeTypeName)

        // Enrich NodeType
        bluePrintTypeEnhancerService.enhanceNodeType(bluePrintRuntimeService, nodeTypeName, nodeType)

        //Enrich Node Template Artifacts
        enhanceNodeTemplateArtifactDefinition(name, nodeTemplate)
    }

    open fun enhanceNodeTemplateArtifactDefinition(nodeTemplateName: String, nodeTemplate: NodeTemplate) {

        nodeTemplate.artifacts?.forEach { artifactDefinitionName, artifactDefinition ->
            // Enhance Artifacct Definitions
            bluePrintTypeEnhancerService.enhanceArtifactDefinition(bluePrintRuntimeService, artifactDefinitionName, artifactDefinition)
        }
    }

}