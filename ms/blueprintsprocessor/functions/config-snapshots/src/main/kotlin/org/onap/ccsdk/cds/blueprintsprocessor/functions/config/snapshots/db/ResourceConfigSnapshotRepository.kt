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
import javax.transaction.Transactional

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
}
