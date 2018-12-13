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

package org.onap.ccsdk.apps.controllerblueprints.service.validator

import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class BluePrintTypeValidatorServiceImpl : BluePrintTypeValidatorService {

    @Autowired
    private lateinit var context: ApplicationContext

    override fun getServiceTemplateValidators(): List<BluePrintServiceTemplateValidator> {
        return context.getBeansOfType(BluePrintServiceTemplateValidator::class.java).mapNotNull { it.value }
    }

    override fun getDataTypeValidators(): List<BluePrintDataTypeValidator> {
        return context.getBeansOfType(BluePrintDataTypeValidator::class.java).mapNotNull { it.value }
    }

    override fun getArtifactTypeValidators(): List<BluePrintArtifactTypeValidator> {
        return context.getBeansOfType(BluePrintArtifactTypeValidator::class.java).mapNotNull { it.value }
    }

    override fun getNodeTypeValidators(): List<BluePrintNodeTypeValidator> {
        return context.getBeansOfType(BluePrintNodeTypeValidator::class.java).mapNotNull { it.value }
    }

    override fun getTopologyTemplateValidators(): List<BluePrintTopologyTemplateValidator> {
        return context.getBeansOfType(BluePrintTopologyTemplateValidator::class.java).mapNotNull { it.value }
    }

    override fun getNodeTemplateValidators(): List<BluePrintNodeTemplateValidator> {
        return context.getBeansOfType(BluePrintNodeTemplateValidator::class.java).mapNotNull { it.value }
    }

    override fun getWorkflowValidators(): List<BluePrintWorkflowValidator> {
        return context.getBeansOfType(BluePrintWorkflowValidator::class.java).mapNotNull { it.value }
    }

    override fun getPropertyDefinitionValidators(): List<BluePrintPropertyDefinitionValidator> {
        return context.getBeansOfType(BluePrintPropertyDefinitionValidator::class.java).mapNotNull { it.value }
    }

    override fun getAttributeDefinitionValidators(): List<BluePrintAttributeDefinitionValidator> {
        return context.getBeansOfType(BluePrintAttributeDefinitionValidator::class.java).mapNotNull { it.value }
    }
}

