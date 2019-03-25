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

package org.onap.ccsdk.cds.controllerblueprints.service.enhancer

import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
open class BluePrintTypeEnhancerServiceImpl : BluePrintTypeEnhancerService {

    @Autowired
    private lateinit var context: ApplicationContext

    override fun getServiceTemplateEnhancers(): List<BluePrintServiceTemplateEnhancer> {
        return context.getBeansOfType(BluePrintServiceTemplateEnhancer::class.java).map { it.value }
    }

    override fun getTopologyTemplateEnhancers(): List<BluePrintTopologyTemplateEnhancer> {
        return context.getBeansOfType(BluePrintTopologyTemplateEnhancer::class.java).map { it.value }
    }

    override fun getWorkflowEnhancers(): List<BluePrintWorkflowEnhancer> {
        return context.getBeansOfType(BluePrintWorkflowEnhancer::class.java).map { it.value }
    }

    override fun getNodeTemplateEnhancers(): List<BluePrintNodeTemplateEnhancer> {
        return context.getBeansOfType(BluePrintNodeTemplateEnhancer::class.java).map { it.value }
    }

    override fun getNodeTypeEnhancers(): List<BluePrintNodeTypeEnhancer> {
        return context.getBeansOfType(BluePrintNodeTypeEnhancer::class.java).map { it.value }
    }

    override fun getArtifactDefinitionEnhancers(): List<BluePrintArtifactDefinitionEnhancer> {
        return context.getBeansOfType(BluePrintArtifactDefinitionEnhancer::class.java).map { it.value }
    }

    override fun getPolicyTypeEnhancers(): List<BluePrintPolicyTypeEnhancer> {
        return context.getBeansOfType(BluePrintPolicyTypeEnhancer::class.java).map { it.value }
    }

    override fun getPropertyDefinitionEnhancers(): List<BluePrintPropertyDefinitionEnhancer> {
        return context.getBeansOfType(BluePrintPropertyDefinitionEnhancer::class.java).map { it.value }
    }

    override fun getAttributeDefinitionEnhancers(): List<BluePrintAttributeDefinitionEnhancer> {
        return context.getBeansOfType(BluePrintAttributeDefinitionEnhancer::class.java).map { it.value }
    }
}