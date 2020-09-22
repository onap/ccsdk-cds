/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2019 Orange.
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

package org.onap.ccsdk.cds.blueprintsprocessor.db.primary.repository

import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain.BlueprintModelSearch
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Provide Configuration Generator AsdcArtifactsRepository
 *
 * @author Brinda Santh
 * @version 1.0
 */
@Repository
interface BlueprintModelSearchRepository : JpaRepository<BlueprintModelSearch, Long> {

    /**
     * This is a findById method
     *
     * @param id id
     * @return Optional<BlueprintModelSearch>
     </BlueprintModelSearch> */
    fun findById(id: String): BlueprintModelSearch?

    /**
     * This is a findAll method
     * @return List<BlueprintModelSearch>
     </BlueprintModelSearch> */
    override fun findAll(): List<BlueprintModelSearch>

    /**
     * This is a findByArtifactNameAndArtifactVersion method
     *
     * @param artifactName artifactName
     * @param artifactVersion artifactVersion
     * @return Optional<AsdcArtifacts>
     </AsdcArtifacts> */
    fun findByArtifactNameAndArtifactVersion(artifactName: String, artifactVersion: String): BlueprintModelSearch?

    /**
     * This is a findByTagsContainingIgnoreCase method
     *
     * @param tags
     * @return Optional<BlueprintModelSearch>
     </BlueprintModelSearch> */
    fun findByTagsContainingIgnoreCase(tags: String): List<BlueprintModelSearch>

    /**
     * This is a findby some attributes method
     *
     * @author Shaaban Ebrahim
     *
     * @param updatedBy
     * @param tags
     * @param artifactName
     * @param artifactVersion
     * @param artifactType
     * @return Optional<BlueprintModelSearch>
     </BlueprintModelSearch>
     */
    fun findByUpdatedByOrTagsOrOrArtifactNameOrOrArtifactVersionOrArtifactType(
        updatedBy: String,
        tags: String,
        artifactName: String,
        artifactVersion: String,
        artifactType: String
    ): List<BlueprintModelSearch>

    /**
     * This is a findby some attributes method
     *
     * @author Shaaban Ebrahim
     *
     * @param updatedBy
     * @param tags
     * @param artifactName
     * @param artifactVersion
     * @param artifactType
     * @param pageRequest
     * @return Page<BlueprintModelSearch>
     */
    fun findByUpdatedByContainingIgnoreCaseOrTagsContainingIgnoreCaseOrArtifactNameContainingIgnoreCaseOrArtifactVersionContainingIgnoreCaseOrArtifactTypeContainingIgnoreCase(
        updatedBy: String,
        tags: String,
        artifactName: String,
        artifactVersion: String,
        artifactType: String,
        pageRequest: Pageable
    ): Page<BlueprintModelSearch>
}
