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
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModelContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ConfigModelContentRepository.java Purpose: Provide ConfigModelContentRepository of Repository
 *
 * @author Brinda Santh
 * @version 1.0
 */
@Repository
public interface ConfigModelContentRepository extends JpaRepository<ConfigModelContent, Long> {

    /**
     * This is a findById method
     * 
     * @param id
     * @return Optional<AsdcArtifacts>
     */
    Optional<ConfigModelContent> findById(Long id);

    /**
     * This is a findTopByConfigModelAndContentType method
     * 
     * @param configModel
     * @param contentType
     * @return Optional<ConfigModelContent>
     */
    Optional<ConfigModelContent> findTopByConfigModelAndContentType(ConfigModel configModel, String contentType);

    /**
     * This is a findByConfigModelAndContentType method
     * 
     * @param configModel
     * @param contentType
     * @return Optional<ConfigModelContent>
     */
    List<ConfigModelContent> findByConfigModelAndContentType(ConfigModel configModel, String contentType);

    /**
     * This is a findByConfigModel method
     * 
     * @param configModel
     * @return Optional<ConfigModelContent>
     */
    List<ConfigModelContent> findByConfigModel(ConfigModel configModel);

    /**
     * This is a findByConfigModelAndContentTypeAndName method
     * 
     * @param configModel
     * @param contentType
     * @param name
     * @return Optional<ConfigModelContent>
     */
    Optional<ConfigModelContent> findByConfigModelAndContentTypeAndName(ConfigModel configModel,
                                                                        String contentType, String name);

    /**
     * This is a deleteByMdeleteByConfigModelodelName method
     * 
     * @param configModel
     */
    void deleteByConfigModel(ConfigModel configModel);

    /**
     * This is a deleteById method
     * 
     * @param id
     */
    void deleteById(Long id);

}
