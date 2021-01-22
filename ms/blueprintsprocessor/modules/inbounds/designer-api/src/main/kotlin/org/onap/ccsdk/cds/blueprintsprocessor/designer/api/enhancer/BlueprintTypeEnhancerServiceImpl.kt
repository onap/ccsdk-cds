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

import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintArtifactDefinitionEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintAttributeDefinitionEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintNodeTemplateEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintNodeTypeEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintPolicyTypeEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintPropertyDefinitionEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintRelationshipTemplateEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintRelationshipTypeEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintServiceTemplateEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTopologyTemplateEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintWorkflowEnhancer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
open class BlueprintTypeEnhancerServiceImpl : BlueprintTypeEnhancerService {

    @Autowired
    private lateinit var context: ApplicationContext

    override fun getServiceTemplateEnhancers(): List<BlueprintServiceTemplateEnhancer> {
        return context.getBeansOfType(BlueprintServiceTemplateEnhancer::class.java).map { it.value }
    }

    override fun getTopologyTemplateEnhancers(): List<BlueprintTopologyTemplateEnhancer> {
        return context.getBeansOfType(BlueprintTopologyTemplateEnhancer::class.java).map { it.value }
    }

    override fun getWorkflowEnhancers(): List<BlueprintWorkflowEnhancer> {
        return context.getBeansOfType(BlueprintWorkflowEnhancer::class.java).map { it.value }
    }

    override fun getNodeTemplateEnhancers(): List<BlueprintNodeTemplateEnhancer> {
        return context.getBeansOfType(BlueprintNodeTemplateEnhancer::class.java).map { it.value }
    }

    override fun getNodeTypeEnhancers(): List<BlueprintNodeTypeEnhancer> {
        return context.getBeansOfType(BlueprintNodeTypeEnhancer::class.java).map { it.value }
    }

    override fun getRelationshipTemplateEnhancers(): List<BlueprintRelationshipTemplateEnhancer> {
        return context.getBeansOfType(BlueprintRelationshipTemplateEnhancer::class.java).map { it.value }
    }

    override fun getRelationshipTypeEnhancers(): List<BlueprintRelationshipTypeEnhancer> {
        return context.getBeansOfType(BlueprintRelationshipTypeEnhancer::class.java).map { it.value }
    }

    override fun getArtifactDefinitionEnhancers(): List<BlueprintArtifactDefinitionEnhancer> {
        return context.getBeansOfType(BlueprintArtifactDefinitionEnhancer::class.java).map { it.value }
    }

    override fun getPolicyTypeEnhancers(): List<BlueprintPolicyTypeEnhancer> {
        return context.getBeansOfType(BlueprintPolicyTypeEnhancer::class.java).map { it.value }
    }

    override fun getPropertyDefinitionEnhancers(): List<BlueprintPropertyDefinitionEnhancer> {
        return context.getBeansOfType(BlueprintPropertyDefinitionEnhancer::class.java).map { it.value }
    }

    override fun getAttributeDefinitionEnhancers(): List<BlueprintAttributeDefinitionEnhancer> {
        return context.getBeansOfType(BlueprintAttributeDefinitionEnhancer::class.java).map { it.value }
    }
}
