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

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintArtifactDefinitionValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintArtifactTypeValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintAttributeDefinitionValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintDataTypeValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintNodeTemplateValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintNodeTypeValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintPropertyDefinitionValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintServiceTemplateValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTopologyTemplateValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintWorkflowValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class BlueprintTypeValidatorServiceImpl : BlueprintTypeValidatorService {

    companion object {

        const val PREFIX_DEFAULT = "default"
    }

    @Autowired
    private lateinit var context: ApplicationContext

    override fun <T : BlueprintValidator<*>> bluePrintValidator(referenceName: String, classType: Class<T>): T? {
        return if (context.containsBean(referenceName)) {
            context.getBean(referenceName, classType)
        } else {
            null
        }
    }

    override fun <T : BlueprintValidator<*>> bluePrintValidators(referenceNamePrefix: String, classType: Class<T>): List<T>? {
        return context.getBeansOfType(classType)
            .filter { it.key.startsWith(referenceNamePrefix) }
            .mapNotNull { it.value }
    }

    override fun <T : BlueprintValidator<*>> bluePrintValidators(classType: Class<T>): List<T>? {
        return context.getBeansOfType(classType).mapNotNull { it.value }
    }

    override fun getServiceTemplateValidators(): List<BlueprintServiceTemplateValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BlueprintServiceTemplateValidator::class.java)
            ?: throw BlueprintProcessorException("failed to get default ServiceTemplate validators")
    }

    override fun getDataTypeValidators(): List<BlueprintDataTypeValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BlueprintDataTypeValidator::class.java)
            ?: throw BlueprintProcessorException("failed to get default DataType validators")
    }

    override fun getArtifactTypeValidators(): List<BlueprintArtifactTypeValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BlueprintArtifactTypeValidator::class.java)
            ?: throw BlueprintProcessorException("failed to get default ArtifactType validators")
    }

    override fun getArtifactDefinitionsValidators(): List<BlueprintArtifactDefinitionValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BlueprintArtifactDefinitionValidator::class.java)
            ?: throw BlueprintProcessorException("failed to get default ArtifactDefinition validators")
    }

    override fun getNodeTypeValidators(): List<BlueprintNodeTypeValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BlueprintNodeTypeValidator::class.java)
            ?: throw BlueprintProcessorException("failed to get default NodeType validators")
    }

    override fun getTopologyTemplateValidators(): List<BlueprintTopologyTemplateValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BlueprintTopologyTemplateValidator::class.java)
            ?: throw BlueprintProcessorException("failed to get default TopologyTemplate validators")
    }

    override fun getNodeTemplateValidators(): List<BlueprintNodeTemplateValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BlueprintNodeTemplateValidator::class.java)
            ?: throw BlueprintProcessorException("failed to get default NodeTemplate validators")
    }

    override fun getWorkflowValidators(): List<BlueprintWorkflowValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BlueprintWorkflowValidator::class.java)
            ?: throw BlueprintProcessorException("failed to get default Workflow validators")
    }

    override fun getPropertyDefinitionValidators(): List<BlueprintPropertyDefinitionValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BlueprintPropertyDefinitionValidator::class.java)
            ?: throw BlueprintProcessorException("failed to get default PropertyDefinition validators")
    }

    override fun getAttributeDefinitionValidators(): List<BlueprintAttributeDefinitionValidator> {
        return bluePrintValidators(PREFIX_DEFAULT, BlueprintAttributeDefinitionValidator::class.java)
            ?: throw BlueprintProcessorException("failed to get default AttributeDefinition validators")
    }
}
