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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler

import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.DesignerApiDomains
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ModelType
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.repository.ModelTypeRepository
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.utils.ModelTypeValidator
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.httpProcessorException
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogCodes
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
open class ModelTypeHandler(private val modelTypeRepository: ModelTypeRepository) {

    private val log = LoggerFactory.getLogger(ModelTypeHandler::class.java)!!

    /**
     * This is a getModelTypeByName service
     *
     * @param modelTypeName modelTypeName
     * @return ModelType
     */
    suspend fun getModelTypeByName(modelTypeName: String): ModelType {
        log.info("Searching : $modelTypeName")
        check(modelTypeName.isNotBlank()) { "Model Name Information is missing." }
        val modelType = modelTypeRepository.findByModelName(modelTypeName)
        return if (modelType != null) {
            modelType
        } else {
            throw httpProcessorException(
                ErrorCatalogCodes.RESOURCE_NOT_FOUND, DesignerApiDomains.DESIGNER_API,
                "couldn't get modelType($modelTypeName)"
            )
        }
    }

    /**
     * This is a searchModelTypes service
     *
     * @param tags tags
     * @return List<ModelType>
     </ModelType> */
    suspend fun searchModelTypes(tags: String): List<ModelType> {
        check(tags.isNotBlank()) { "No Search Information provide" }
        return modelTypeRepository.findByTagsContainingIgnoreCase(tags)
    }

    /**
     * This is a saveModel service
     *
     * @param modelType modelType
     * @return ModelType
     * @throws BlueprintException BlueprintException
     */
    @Throws(BlueprintException::class)
    open suspend fun saveModel(modelType: ModelType): ModelType {
        lateinit var dbModel: ModelType
        ModelTypeValidator.validateModelType(modelType)
        val dbModelType: ModelType? = modelTypeRepository.findByModelName(modelType.modelName)
        if (dbModelType != null) {
            dbModel = dbModelType
            dbModel.description = modelType.description
            dbModel.definition = modelType.definition
            dbModel.definitionType = modelType.definitionType
            dbModel.derivedFrom = modelType.derivedFrom
            dbModel.tags = modelType.tags
            dbModel.version = modelType.version
            dbModel.updatedBy = modelType.updatedBy
            dbModel = modelTypeRepository.save(dbModel)
        } else {
            dbModel = modelTypeRepository.save(modelType)
        }
        return dbModel
    }

    /**
     * This is a deleteByModelName service
     *
     * @param modelName modelName
     */
    open suspend fun deleteByModelName(modelName: String) {
        check(modelName.isNotBlank()) { "Model Name Information is missing." }
        modelTypeRepository.deleteByModelName(modelName)
    }

    /**
     * This is a getModelTypeByDefinitionType service
     *
     * @param definitionType definitionType
     * @return List<ModelType>
     */
    suspend fun getModelTypeByDefinitionType(definitionType: String): List<ModelType> {
        check(definitionType.isNotBlank()) { "Model definitionType Information is missing." }
        return modelTypeRepository.findByDefinitionType(definitionType)
    }

    /**
     * This is a getModelTypeByDerivedFrom service
     *
     * @param derivedFrom derivedFrom
     * @return List<ModelType>
     */
    suspend fun getModelTypeByDerivedFrom(derivedFrom: String): List<ModelType> {
        check(derivedFrom.isNotBlank()) { "Model derivedFrom Information is missing." }
        return modelTypeRepository.findByDerivedFrom(derivedFrom)
    }
}
