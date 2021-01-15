/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.db.primary.repository

import org.jetbrains.annotations.NotNull
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.domain.BlueprintModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import javax.transaction.Transactional

/**
 * @param <T> Model
 */
@Repository
interface BlueprintModelRepository : JpaRepository<BlueprintModel, String> {

    /**
     * This is a findById method
     *
     * @param id id
     * @return Optional<T>
     */
    override fun findById(id: String): Optional<BlueprintModel>

    /**
     * This is a findByArtifactNameAndArtifactVersion method
     *
     * @param artifactName artifactName
     * @param artifactVersion artifactVersion
     * @return T?
     */
    fun findByArtifactNameAndArtifactVersion(artifactName: String, artifactVersion: String): BlueprintModel?

    /**
     *  Find the Blueprint UUID (blueprint_model_id) for a given artifactName/Version
     *
     * @param artifactName artifactName
     * @param artifactVersion artifactVersion
     * @return String?
     */
    @Query("SELECT m.id FROM BlueprintModel m WHERE m.artifactName = :artifactName AND m.artifactVersion = :artifactVersion")
    fun findIdByArtifactNameAndArtifactVersion(@Param("artifactName") artifactName: String, @Param("artifactVersion") artifactVersion: String): String?

    /**
     * This is a findTopByArtifactNameOrderByArtifactIdDesc method
     *
     * @param artifactName artifactName
     * @return T?
     */
    fun findTopByArtifactNameOrderByArtifactVersionDesc(artifactName: String): BlueprintModel?

    /**
     * This is a findTopByArtifactName method
     *
     * @param artifactName artifactName
     * @return List<T>
     */
    fun findTopByArtifactName(artifactName: String): List<BlueprintModel>

    /**
     * This is a findByTagsContainingIgnoreCase method
     *
     * @param tags tags
     * @return List<T>
     */
    fun findByTagsContainingIgnoreCase(tags: String): List<BlueprintModel>

    /**
     * This is a deleteByArtifactNameAndArtifactVersion method
     *
     * @param artifactName artifactName
     * @param artifactVersion artifactVersion
     */
    @Transactional
    fun deleteByArtifactNameAndArtifactVersion(artifactName: String, artifactVersion: String)

    /**
     * This is a deleteById method
     *
     * @param id id
     */
    override fun deleteById(@NotNull id: String)
}
