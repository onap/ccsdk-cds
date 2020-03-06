/*
 *  Copyright © 2020 IBM, Bell Canada.
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

package org.onap.ccsdk.error.catalog.services.repository

import org.onap.ccsdk.error.catalog.services.domain.ErrorMessageModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.Optional

/**
 * @param <T> Model
 */
@Repository
interface ErrorMessageModelRepository : JpaRepository<ErrorMessageModel, UUID> {
    /**
     * This is a findById method
     *
     * @param id id
     * @return Optional<T>
     */
    override fun findById(id: UUID): Optional<ErrorMessageModel>

    /**
     * This is a findByDomains method
     *
     * @param domainId domainId
     * @return List<T>
     */
    fun findByDomainsId(domainId: UUID): List<ErrorMessageModel>
}
