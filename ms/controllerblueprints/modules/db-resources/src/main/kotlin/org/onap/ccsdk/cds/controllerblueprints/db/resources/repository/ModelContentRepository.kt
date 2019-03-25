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

package org.onap.ccsdk.cds.controllerblueprints.db.resources.repository

import org.jetbrains.annotations.NotNull
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean

/**
 * @param <T> Model
 * @param <B> ModelContent
 */
@NoRepositoryBean
interface ModelContentRepository<T, B> : JpaRepository<B, String> {

    /**
     * This is a findById method
     *
     * @param id id
     * @return Optional<T>
     */
    @NotNull
    override fun findById(@NotNull id: String): Optional<B>

    /**
     * This is a findTopByBlueprintModelAndContentType method
     *
     * @param blueprintModel blueprintModel
     * @param contentType contentType
     * @return B?
     */
    fun findTopByBlueprintModelAndContentType(blueprintModel: T, contentType: String): B?

    /**
     * This is a findByBlueprintModelAndContentType method
     *
     * @param blueprintModel blueprintModel
     * @param contentType contentType
     * @return List<B>
     */
    fun findByBlueprintModelAndContentType(blueprintModel: T, contentType: String): List<B>

    /**
     * This is a findByBlueprintModel method
     *
     * @param blueprintModel T
     * @return List<B>
     */
    fun findByBlueprintModel(blueprintModel: T): List<B>

    /**
     * This is a findByBlueprintModelAndContentTypeAndName method
     *
     * @param blueprintModel blueprintModel
     * @param contentType contentType
     * @param name name
     * @return B?
     */
    fun findByBlueprintModelAndContentTypeAndName(blueprintModel: T, contentType: String, name: String): B?

    /**
     * This is a deleteByMdeleteByBlueprintModelodelName method
     *
     * @param blueprintModel T
     */
    fun deleteByBlueprintModel(blueprintModel: T)

    /**
     * This is a deleteById method
     *
     * @param id id
     */
    override fun deleteById(@NotNull id: String)

}
