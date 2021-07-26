/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintArtifactDefinitionValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintArtifactTypeValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintAttributeDefinitionValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintDataTypeValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintNodeTemplateValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintNodeTypeValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintPropertyDefinitionValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintServiceTemplateValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintTopologyTemplateValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintWorkflowValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class BluePrintTypeValidatorServiceImpl : BluePrintTypeValidatorService {

    companion object {

        const val PREFIX_DEFAULT = "default"
    }

    @Autowired
    private lateinit var context: ApplicationContext

    override fun <T : BluePrintValidator<*>> bluePrintValidator(referenceName: String, classType: Class<T>): T? {
        return if (context.containsBean(referenceName)) {
            context.getBean(referenceName, classType)
        } else {
            null
        }
    }

    override fun <T : BluePrintValidator<*>> bluePrintValidators(referenceNamePrefix: String, classType: Class<T>): List<T>? {
        return context.getBeansOfType(classType)
            .filter { it.key.startsWith(referenceNamePrefix) }
            .mapNotNull { it.value }
    }

    override fun <T : BluePrintValidator<*>> bluePrintValidators(classType: Class<T>): List<T>? {
        return context.getBeansOfType(classType).mapNotNull { it.value }
    }

    override fun getServiceTemplateValidators(): List<BluePrintServiceTemplateValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BluePrintServiceTemplateValidator::class.java)
            ?: throw BluePrintProcessorException("failed to get default ServiceTemplate validators")
    }

    override fun getDataTypeValidators(): List<BluePrintDataTypeValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BluePrintDataTypeValidator::class.java)
            ?: throw BluePrintProcessorException("failed to get default DataType validators")
    }

    override fun getArtifactTypeValidators(): List<BluePrintArtifactTypeValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BluePrintArtifactTypeValidator::class.java)
            ?: throw BluePrintProcessorException("failed to get default ArtifactType validators")
    }

    override fun getArtifactDefinitionsValidators(): List<BluePrintArtifactDefinitionValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BluePrintArtifactDefinitionValidator::class.java)
            ?: throw BluePrintProcessorException("failed to get default ArtifactDefinition validators")
    }

    override fun getNodeTypeValidators(): List<BluePrintNodeTypeValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BluePrintNodeTypeValidator::class.java)
            ?: throw BluePrintProcessorException("failed to get default NodeType validators")
    }

    override fun getTopologyTemplateValidators(): List<BluePrintTopologyTemplateValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BluePrintTopologyTemplateValidator::class.java)
            ?: throw BluePrintProcessorException("failed to get default TopologyTemplate validators")
    }

    override fun getNodeTemplateValidators(): List<BluePrintNodeTemplateValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BluePrintNodeTemplateValidator::class.java)
            ?: throw BluePrintProcessorException("failed to get default NodeTemplate validators")
    }

    override fun getWorkflowValidators(): List<BluePrintWorkflowValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BluePrintWorkflowValidator::class.java)
            ?: throw BluePrintProcessorException("failed to get default Workflow validators")
    }

    override fun getPropertyDefinitionValidators(): List<BluePrintPropertyDefinitionValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BluePrintPropertyDefinitionValidator::class.java)
            ?: throw BluePrintProcessorException("failed to get default PropertyDefinition validators")
    }

    override fun getAttributeDefinitionValidators(): List<BluePrintAttributeDefinitionValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BluePrintAttributeDefinitionValidator::class.java)
            ?: throw BluePrintProcessorException("failed to get default AttributeDefinition validators")
    }
}
