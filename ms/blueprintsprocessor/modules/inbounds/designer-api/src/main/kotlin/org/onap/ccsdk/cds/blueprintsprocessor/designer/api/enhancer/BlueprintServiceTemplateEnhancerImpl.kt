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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.enhancer

import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintServiceTemplateEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BlueprintServiceTemplateEnhancerImpl(private val bluePrintTypeEnhancerService: BlueprintTypeEnhancerService) :
    BlueprintServiceTemplateEnhancer {

    private val log = logger(BlueprintServiceTemplateEnhancerImpl::class)

    lateinit var bluePrintRuntimeService: BlueprintRuntimeService<*>
    lateinit var bluePrintContext: BlueprintContext

    override fun enhance(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, type: ServiceTemplate) {
        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        initialCleanUp()
        enhanceTopologyTemplate()
    }

    open fun initialCleanUp() {
        bluePrintContext.serviceTemplate.artifactTypes?.clear()
        bluePrintContext.serviceTemplate.nodeTypes?.clear()
        bluePrintContext.serviceTemplate.dataTypes?.clear()
        bluePrintContext.serviceTemplate.policyTypes?.clear()
        bluePrintContext.serviceTemplate.relationshipTypes?.clear()

        bluePrintContext.serviceTemplate.artifactTypes = mutableMapOf()
        bluePrintContext.serviceTemplate.nodeTypes = mutableMapOf()
        bluePrintContext.serviceTemplate.dataTypes = mutableMapOf()
        bluePrintContext.serviceTemplate.policyTypes = mutableMapOf()
        bluePrintContext.serviceTemplate.relationshipTypes = mutableMapOf()
        log.info("reinitialized all type definitions")
    }

    open fun enhanceTopologyTemplate() {
        bluePrintContext.serviceTemplate.topologyTemplate?.let { topologyTemplate ->
            bluePrintTypeEnhancerService.enhanceTopologyTemplate(bluePrintRuntimeService, "topology_template", topologyTemplate)
        }
    }
}
