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

import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.*

class MockBluePrintTypeValidatorService : BluePrintTypeValidatorService {

    override fun <T : BluePrintValidator<*>> bluePrintValidator(referenceName: String, classType: Class<T>): T? {
        return null
    }

    override fun <T : BluePrintValidator<*>> bluePrintValidators(referenceNamePrefix: String, classType: Class<T>): List<T>? {
       return null
    }

    override fun <T : BluePrintValidator<*>> bluePrintValidators(classType: Class<T>): List<T>? {
       return null
    }

    override fun getServiceTemplateValidators(): List<BluePrintServiceTemplateValidator> {
        return listOf(BluePrintServiceTemplateValidatorImpl(this))
    }

    override fun getDataTypeValidators(): List<BluePrintDataTypeValidator> {
        return listOf(BluePrintDataTypeValidatorImpl(this))
    }

    override fun getArtifactTypeValidators(): List<BluePrintArtifactTypeValidator> {
        return listOf(BluePrintArtifactTypeValidatorImpl(this))
    }

    override fun getArtifactDefinitionsValidators(): List<BluePrintArtifactDefinitionValidator> {
        return listOf(BluePrintArtifactDefinitionValidatorImpl(this))
    }

    override fun getNodeTypeValidators(): List<BluePrintNodeTypeValidator> {
        return listOf(BluePrintNodeTypeValidatorImpl(this))
    }

    override fun getTopologyTemplateValidators(): List<BluePrintTopologyTemplateValidator> {
        return listOf(BluePrintTopologyTemplateValidatorImpl(this))
    }

    override fun getNodeTemplateValidators(): List<BluePrintNodeTemplateValidator> {
        return listOf(BluePrintNodeTemplateValidatorImpl(this))
    }

    override fun getWorkflowValidators(): List<BluePrintWorkflowValidator> {
        return listOf(BluePrintWorkflowValidatorImpl(this))
    }

    override fun getPropertyDefinitionValidators(): List<BluePrintPropertyDefinitionValidator> {
        return listOf(BluePrintPropertyDefinitionValidatorImpl(this))
    }

    override fun getAttributeDefinitionValidators(): List<BluePrintAttributeDefinitionValidator> {
        return listOf(BluePrintAttributeDefinitionValidatorImpl(this))
    }
}