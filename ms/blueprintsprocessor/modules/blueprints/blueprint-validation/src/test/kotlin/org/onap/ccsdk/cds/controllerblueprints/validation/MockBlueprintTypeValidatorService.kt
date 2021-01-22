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

class MockBlueprintTypeValidatorService : BlueprintTypeValidatorService {

    override fun <T : BlueprintValidator<*>> bluePrintValidator(referenceName: String, classType: Class<T>): T? {
        return null
    }

    override fun <T : BlueprintValidator<*>> bluePrintValidators(referenceNamePrefix: String, classType: Class<T>): List<T>? {
        return null
    }

    override fun <T : BlueprintValidator<*>> bluePrintValidators(classType: Class<T>): List<T>? {
        return null
    }

    override fun getServiceTemplateValidators(): List<BlueprintServiceTemplateValidator> {
        return listOf(BlueprintServiceTemplateValidatorImpl(this))
    }

    override fun getDataTypeValidators(): List<BlueprintDataTypeValidator> {
        return listOf(BlueprintDataTypeValidatorImpl(this))
    }

    override fun getArtifactTypeValidators(): List<BlueprintArtifactTypeValidator> {
        return listOf(BlueprintArtifactTypeValidatorImpl(this))
    }

    override fun getArtifactDefinitionsValidators(): List<BlueprintArtifactDefinitionValidator> {
        return listOf(BlueprintArtifactDefinitionValidatorImpl(this))
    }

    override fun getNodeTypeValidators(): List<BlueprintNodeTypeValidator> {
        return listOf(BlueprintNodeTypeValidatorImpl(this))
    }

    override fun getTopologyTemplateValidators(): List<BlueprintTopologyTemplateValidator> {
        return listOf(BlueprintTopologyTemplateValidatorImpl(this))
    }

    override fun getNodeTemplateValidators(): List<BlueprintNodeTemplateValidator> {
        return listOf(BlueprintNodeTemplateValidatorImpl(this))
    }

    override fun getWorkflowValidators(): List<BlueprintWorkflowValidator> {
        return listOf(BlueprintWorkflowValidatorImpl(this))
    }

    override fun getPropertyDefinitionValidators(): List<BlueprintPropertyDefinitionValidator> {
        return listOf(BlueprintPropertyDefinitionValidatorImpl(this))
    }

    override fun getAttributeDefinitionValidators(): List<BlueprintAttributeDefinitionValidator> {
        return listOf(BlueprintAttributeDefinitionValidatorImpl(this))
    }
}
