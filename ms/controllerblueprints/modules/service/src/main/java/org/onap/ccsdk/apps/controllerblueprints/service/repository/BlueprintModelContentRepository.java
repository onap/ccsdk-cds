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

import org.onap.ccsdk.apps.controllerblueprints.service.domain.BlueprintModel;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.BlueprintModelContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * BlueprintModelContentRepository.java Purpose: Provide BlueprintModelContentRepository of Repository
 *
 * @author Brinda Santh
 * @version 1.0
 */
@Repository
public interface BlueprintModelContentRepository extends JpaRepository<BlueprintModelContent, String> {

    /**
     * This is a findById method
     * 
     * @param id id
     * @return Optional<AsdcArtifacts>
     */
    @NotNull
    Optional<BlueprintModelContent> findById(@NotNull String id);

    /**
     * This is a findTopByBlueprintModelAndContentType method
     * 
     * @param blueprintModel  blueprintModel
     * @param contentType contentType
     * @return Optional<BlueprintModelContent>
     */
    @SuppressWarnings("unused")
    Optional<BlueprintModelContent> findTopByBlueprintModelAndContentType(BlueprintModel blueprintModel, String contentType);

    /**
     * This is a findByBlueprintModelAndContentType method
     * 
     * @param blueprintModel blueprintModel
     * @param contentType contentType
     * @return Optional<BlueprintModelContent>
     */
    @SuppressWarnings("unused")
    List<BlueprintModelContent> findByBlueprintModelAndContentType(BlueprintModel blueprintModel, String contentType);

    /**
     * This is a findByBlueprintModel method
     * 
     * @param blueprintModel blueprintModel
     * @return Optional<BlueprintModelContent>
     */
    @SuppressWarnings("unused")
    List<BlueprintModelContent> findByBlueprintModel(BlueprintModel blueprintModel);

    /**
     * This is a findByBlueprintModelAndContentTypeAndName method
     * 
     * @param blueprintModel blueprintModel
     * @param contentType contentType
     * @param name name
     * @return Optional<BlueprintModelContent>
     */
    @SuppressWarnings("unused")
    Optional<BlueprintModelContent> findByBlueprintModelAndContentTypeAndName(BlueprintModel blueprintModel,
                                                                           String contentType, String name);

    /**
     * This is a deleteByMdeleteByBlueprintModelodelName method
     * 
     * @param blueprintModel blueprintModel
     */
    void deleteByBlueprintModel(BlueprintModel blueprintModel);

    /**
     * This is a deleteById method
     * 
     * @param  id id
     */
    void deleteById(@NotNull String id);

}
