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
import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintServiceTemplateEnhancer
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BluePrintServiceTemplateEnhancerImpl(private val bluePrintTypeEnhancerService: BluePrintTypeEnhancerService)
    : BluePrintServiceTemplateEnhancer {
    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintServiceTemplateEnhancerImpl::class.toString())


    lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>
    lateinit var bluePrintContext: BluePrintContext

    override fun enhance(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, type: ServiceTemplate) {
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