/*
 *  Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.repository

import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ResourceDictionary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * ResourceMappingRepository.java Purpose: Provide Configuration Generator ResourceMappingRepository
 *
 * @author Brinda Santh
 * @version 1.0
 */
@Repository
interface ResourceDictionaryRepository : JpaRepository<ResourceDictionary, String> {

    /**
     * This is a findByName method
     *
     * @param name name
     * @return Optional<ResourceMapping>
     </ResourceMapping> */
    fun findByName(name: String): ResourceDictionary?

    /**
     * This is a findByNameIn method
     *
     * @param names names
     * @return Optional<ResourceMapping>
     </ResourceMapping> */
    fun findByNameIn(names: List<String>): List<ResourceDictionary>

    /**
     * This is a findByTagsContainingIgnoreCase method
     *
     * @param tags tags
     * @return Optional<ModelType>
     </ModelType> */
    fun findByTagsContainingIgnoreCase(tags: String): List<ResourceDictionary>

    /**
     * This is a deleteByName method
     *
     * @param name name
     */
    fun deleteByName(name: String)

    /**
     *this method for getting resource dictionary group distinct
     * (Dictionary library instances)
     *
     * */
    @Query("SELECT distinct resourceDictionary.resourceDictionaryGroup FROM ResourceDictionary resourceDictionary")
    fun findDistinctByResourceDictionaryGroup(): List<String>
}
