/*
 * Copyright (C) 2019 Bell Canada.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import jakarta.transaction.Transactional

/**
 * JPA repository managing the underlying ResourceConfigSnapshot table.
 *
 * @author Serge Simard
 * @version 1.0
 */
@Repository
interface ResourceConfigSnapshotRepository : JpaRepository<ResourceConfigSnapshot, String> {

    fun findByResourceIdAndResourceTypeAndStatus(
        resourceId: String,
        resourceType: String,
        status: ResourceConfigSnapshot.Status
    ): ResourceConfigSnapshot?

    @Transactional
    fun deleteByResourceIdAndResourceTypeAndStatus(
        resourceId: String,
        resourceType: String,
        status: ResourceConfigSnapshot.Status
    )

    /**
     * Finds all ResourceConfigSnapshot for a given resourceId and status as search criterias,
     * ordering the resulting list in reverse chronological order.
     *
     * @param resourceId a resource identifier, e.g. CLLI1234555
     * @param status RUNNING or CANDIDATE
     *
     * @return A list of entries are found returns a list of ConfigSnapshot.
     * If no entries are found, this method returns an empty list.
     */
    fun findByResourceIdAndStatusOrderByCreatedDateDesc(
        resourceId: String,
        status: ResourceConfigSnapshot.Status
    ): List<ResourceConfigSnapshot>?

    /**
     * Finds all ResourceConfigSnapshot for a given resourceId,
     * ordering the resulting list in reverse chronological order.
     *
     * @param resourceId a resource identifier, e.g. CLLI1234555
     *
     * @return A list of entries are found returns a list of ConfigSnapshot.
     * If no entries are found, this method returns an empty list.
     */
    fun findByResourceIdOrderByCreatedDateDesc(
        resourceId: String
    ): List<ResourceConfigSnapshot>?

    /**
     * Finds all ResourceConfigSnapshot for a given resourceType and status as search criterias,
     * ordering the resulting list in reverse chronological order.
     *
     * @param resourceType a resource type name, e.g full_config
     * @param status RUNNING or CANDIDATE
     *
     * @return A list of entries are found returns a list of ConfigSnapshot.
     * If no entries are found, this method returns an empty list.
     */
    fun findByResourceTypeAndStatusOrderByCreatedDateDesc(
        resourceType: String,
        status: ResourceConfigSnapshot.Status
    ): List<ResourceConfigSnapshot>?

    /**
     * Finds all ResourceConfigSnapshot for a given resourceType,
     * ordering the resulting list in reverse chronological order.
     *
     * @param resourceType a resource type name, e.g full_config
     *
     * @return A list of entries are found returns a list of ConfigSnapshot.
     * If no entries are found, this method returns an empty list.
     */
    fun findByResourceTypeOrderByCreatedDateDesc(
        resourceType: String
    ): List<ResourceConfigSnapshot>?
}
