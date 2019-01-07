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

package org.onap.ccsdk.apps.controllerblueprints.service.repository;

import org.onap.ccsdk.apps.controllerblueprints.service.domain.BlueprintModelSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * ControllerBlueprintModelSearchRepository.java Purpose: Provide Configuration Generator AsdcArtifactsRepository
 *
 * @author Brinda Santh
 * @version 1.0
 */
@Repository
public interface ControllerBlueprintModelSearchRepository extends JpaRepository<BlueprintModelSearch, Long> {

    /**
     * This is a findById method
     *
     * @param id id
     * @return Optional<BlueprintModelSearch>
     */
    @NotNull
    Optional<BlueprintModelSearch> findById(@NotNull String id);

    /**
     * This is a findAll method
     * @return List<BlueprintModelSearch>
     */
    @Override
    List<BlueprintModelSearch> findAll();

    /**
     * This is a findByArtifactNameAndArtifactVersion method
     *
     * @param artifactName artifactName
     * @param artifactVersion artifactVersion
     * @return Optional<AsdcArtifacts>
     */
    Optional<BlueprintModelSearch> findByArtifactNameAndArtifactVersion(String artifactName, String artifactVersion);

    /**
     * This is a findByTagsContainingIgnoreCase method
     * 
     * @param tags
     * @return Optional<BlueprintModelSearch>
     */
    List<BlueprintModelSearch> findByTagsContainingIgnoreCase(String tags);
}
