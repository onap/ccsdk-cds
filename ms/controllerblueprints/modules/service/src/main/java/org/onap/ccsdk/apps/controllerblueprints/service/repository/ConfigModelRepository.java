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

package org.onap.ccsdk.apps.controllerblueprints.service.repository;

import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AsdcArtifactsRepository.java Purpose: Provide Configuration Generator AsdcArtifactsRepository
 *
 * @author Brinda Santh
 * @version 1.0
 */
@Repository
public interface ConfigModelRepository extends JpaRepository<ConfigModel, Long> {
    /**
     * This is a findById method
     * 
     * @param id
     * @return Optional<AsdcArtifacts>
     */
    Optional<ConfigModel> findById(Long id);

    /**
     * This is a findByArtifactNameAndArtifactVersion method
     * 
     * @param artifactName
     * @param artifactVersion
     * @return Optional<AsdcArtifacts>
     */
    Optional<ConfigModel> findByArtifactNameAndArtifactVersion(String artifactName, String artifactVersion);

    /**
     * This is a findTopByArtifactNameOrderByArtifactIdDesc method
     * 
     * @param artifactName
     * @return Optional<AsdcArtifacts>
     */
    Optional<ConfigModel> findTopByArtifactNameOrderByArtifactVersionDesc(String artifactName);

    /**
     * This is a findTopByArtifactName method
     * 
     * @param artifactName
     * @return Optional<AsdcArtifacts>
     */
    List<ConfigModel> findTopByArtifactName(String artifactName);

    /**
     * This is a findByTagsContainingIgnoreCase method
     * 
     * @param tags
     * @return Optional<ModelType>
     */
    List<ConfigModel> findByTagsContainingIgnoreCase(String tags);

    /**
     * This is a deleteByArtifactNameAndArtifactVersion method
     * 
     * @param artifactName
     * @param artifactVersion
     */
    void deleteByArtifactNameAndArtifactVersion(String artifactName, String artifactVersion);

    /**
     * This is a deleteById method
     * 
     * @param id
     */
    void deleteById(Long id);

}
