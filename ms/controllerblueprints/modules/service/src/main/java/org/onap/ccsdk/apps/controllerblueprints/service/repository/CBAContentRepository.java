/*
 * Copyright © 2018 IBM Intellectual Property.
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

import org.jetbrains.annotations.NotNull;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.CbaContent;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CBAContentRepository.java Purpose: Provide Configuration Generator CRUD methods for CBAContent table
 *
 * @author Ruben Chang
 * @version 1.0
 */
@Repository
public interface CBAContentRepository extends JpaRepository<CbaContent, String>  {

    /**
     * This is a findAll method
     * @return List<CbaContent>
     */
    @Override
    List<CbaContent> findAll();

    /**
     * Returns a CbaContent based on the cbaUUID
     * @param cbaUUID the CbaUUID
     * @return Optional<CbaContent>
     */
    @Override
    @NotNull
    Optional<CbaContent> findById(@NotNull String cbaUUID);

    /**
     * This is a deleteById methid
     * @param cbaUUID the user ID for a particular CBAFile
     */
    @Override
    void deleteById(@NotNull String cbaUUID);
}
