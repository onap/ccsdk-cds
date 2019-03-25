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

package org.onap.ccsdk.cds.controllerblueprints.service.repository

import org.onap.ccsdk.cds.controllerblueprints.service.domain.ModelType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
interface ModelTypeRepository : JpaRepository<ModelType, String> {
    /**
     * This is a findByModelName method
     *
     * @param modelName Model Name
     * @return Optional<ModelType>
     */
    fun findByModelName(modelName: String): ModelType?
    /**
     * This is a findByModelNameIn method
     *
     * @param modelNames Model Names
     * @return List<ModelType>
     */
    fun findByModelNameIn(modelNames: List<String>): List<ModelType>
    /**
     * This is a findByDerivedFrom method
     *
     * @param derivedFrom Derived From
     * @return List<ModelType>
    */
    fun findByDerivedFrom(derivedFrom: String): List<ModelType>
    /**
     * This is a findByDerivedFromIn method
     *
     * @param derivedFroms Derived Froms
     * @return List<ModelType>
    */
    fun findByDerivedFromIn(derivedFroms: List<String>): List<ModelType>

    /**
     * This is a findByDefinitionType method
     *
     * @param definitionType Definition Type
     * @return List<ModelType>
     */
    fun findByDefinitionType(definitionType: String): List<ModelType>
    /**
     * This is a findByDefinitionTypeIn method
     *
     * @param definitionTypes Definition Types
     * @return List<ModelType>
    */
    fun findByDefinitionTypeIn(definitionTypes: List<String>): List<ModelType>

    /**
     * This is a findByTagsContainingIgnoreCase method
     *
     * @param tags Tags
     * @return Optional<ModelType>
     */
    fun findByTagsContainingIgnoreCase(tags: String): List<ModelType>

    /**
     * This is a deleteByModelName method
     *
     * @param modelName ModelName
     */
    @Transactional
    fun deleteByModelName(modelName: String)
}
