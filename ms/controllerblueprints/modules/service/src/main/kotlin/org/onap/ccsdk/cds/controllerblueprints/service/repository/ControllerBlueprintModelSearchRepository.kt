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

package org.onap.ccsdk.cds.controllerblueprints.service.repository

import org.onap.ccsdk.cds.controllerblueprints.service.domain.BlueprintModelSearch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * ControllerBlueprintModelSearchRepository.java Purpose: Provide Configuration Generator AsdcArtifactsRepository
 *
 * @author Brinda Santh
 * @version 1.0
 */
@Repository
interface ControllerBlueprintModelSearchRepository : JpaRepository<BlueprintModelSearch, Long> {

    /**
     * This is a findById method
     *
     * @param id id
     * @return Optional<BlueprintModelSearch>
    </BlueprintModelSearch> */
    fun findById(id: String): Optional<BlueprintModelSearch>

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
    fun findByArtifactNameAndArtifactVersion(artifactName: String, artifactVersion: String): Optional<BlueprintModelSearch>

    /**
     * This is a findByTagsContainingIgnoreCase method
     *
     * @param tags
     * @return Optional<BlueprintModelSearch>
    </BlueprintModelSearch> */
    fun findByTagsContainingIgnoreCase(tags: String): List<BlueprintModelSearch>
}