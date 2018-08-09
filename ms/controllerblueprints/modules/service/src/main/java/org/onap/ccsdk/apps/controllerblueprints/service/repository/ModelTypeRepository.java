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

import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * ModelTypeRepository.java Purpose: Provide Configuration Generator ModelTypeRepository
 *
 * @author Brinda Santh
 * @version 1.0
 */
@Repository
public interface ModelTypeRepository extends JpaRepository<ModelType, String> {


    /**
     * This is a findByModelName method
     * 
     * @param modelName
     * @return Optional<ModelType>
     */
    Optional<ModelType> findByModelName(String modelName);

    /**
     * This is a findByDerivedFrom method
     * 
     * @param derivedFrom
     * @return List<ModelType>
     */
    List<ModelType> findByDerivedFrom(String derivedFrom);


    /**
     * This is a findByDerivedFromIn method
     * 
     * @param derivedFroms
     * @return List<ModelType>
     */
    List<ModelType> findByDerivedFromIn(List<String> derivedFroms);

    /**
     * This is a findByDefinitionType method
     * 
     * @param definitionType
     * @return List<ModelType>
     */
    List<ModelType> findByDefinitionType(String definitionType);

    /**
     * This is a findByDefinitionTypeIn method
     * 
     * @param definitionTypes
     * @return List<ModelType>
     */
    List<ModelType> findByDefinitionTypeIn(List<String> definitionTypes);


    /**
     * This is a findByTagsContainingIgnoreCase method
     * 
     * @param tags
     * @return Optional<ModelType>
     */
    List<ModelType> findByTagsContainingIgnoreCase(String tags);


    /**
     * This is a deleteByModelName method
     * 
     * @param modelName
     * @return Optional<ModelType>
     */
    void deleteByModelName(String modelName);



}
