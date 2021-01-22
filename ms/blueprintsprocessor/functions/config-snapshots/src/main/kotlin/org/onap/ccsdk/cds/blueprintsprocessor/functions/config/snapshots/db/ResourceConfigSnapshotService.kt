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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.util.Strings
import org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.db.ResourceConfigSnapshot.Status.RUNNING
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * ResourceConfigSnapshot managing service.
 *
 * @author Serge Simard
 * @version 1.0
 */
@Service
open class ResourceConfigSnapshotService(private val resourceConfigSnapshotRepository: ResourceConfigSnapshotRepository) {

    private val log = LoggerFactory.getLogger(ResourceConfigSnapshotService::class.toString())

    suspend fun findAllByResourceIdForStatus(
        resourceId: String,
        status: ResourceConfigSnapshot.Status
    ): List<ResourceConfigSnapshot>? =
        withContext(Dispatchers.IO) {
            resourceConfigSnapshotRepository.findByResourceIdAndStatusOrderByCreatedDateDesc(resourceId, status)
        }

    suspend fun findAllByResourceId(
        resourceId: String
    ): List<ResourceConfigSnapshot>? =
        withContext(Dispatchers.IO) {
            resourceConfigSnapshotRepository.findByResourceIdOrderByCreatedDateDesc(resourceId)
        }

    suspend fun findAllByResourceTypeForStatus(
        resourceType: String,
        status: ResourceConfigSnapshot.Status
    ): List<ResourceConfigSnapshot>? =
        withContext(Dispatchers.IO) {
            resourceConfigSnapshotRepository.findByResourceTypeAndStatusOrderByCreatedDateDesc(resourceType, status)
        }

    suspend fun findAllByResourceType(
        resourceType: String
    ): List<ResourceConfigSnapshot>? =
        withContext(Dispatchers.IO) {
            resourceConfigSnapshotRepository.findByResourceTypeOrderByCreatedDateDesc(resourceType)
        }

    suspend fun findByResourceIdAndResourceTypeAndStatus(
        resourceId: String,
        resourceType: String,
        status: ResourceConfigSnapshot.Status = RUNNING
    ): String =
        withContext(Dispatchers.IO) {
            resourceConfigSnapshotRepository.findByResourceIdAndResourceTypeAndStatus(resourceId, resourceType, status)
                ?.config_snapshot ?: Strings.EMPTY
        }

    suspend fun write(
        snapshot: String,
        resId: String,
        resType: String,
        status: ResourceConfigSnapshot.Status = RUNNING
    ): ResourceConfigSnapshot =
        withContext(Dispatchers.IO) {

            val resourceConfigSnapshotEntry = ResourceConfigSnapshot()
            resourceConfigSnapshotEntry.id = UUID.randomUUID().toString()
            resourceConfigSnapshotEntry.resourceId = resId
            resourceConfigSnapshotEntry.resourceType = resType
            resourceConfigSnapshotEntry.status = status
            resourceConfigSnapshotEntry.config_snapshot = snapshot

            // Overwrite configuration snapshot entry of resId/resType
            if (resId.isNotEmpty() && resType.isNotEmpty()) {
                resourceConfigSnapshotRepository.findByResourceIdAndResourceTypeAndStatus(resId, resType, status)
                    ?.let {
                        log.info(
                            "Overwriting configuration snapshot entry for resourceId=($resId), " +
                                "resourceType=($resType), status=($status)"
                        )
                        resourceConfigSnapshotRepository.deleteByResourceIdAndResourceTypeAndStatus(resId, resType, status)
                    }
            }
            var storedSnapshot: ResourceConfigSnapshot
            try {
                storedSnapshot = resourceConfigSnapshotRepository.saveAndFlush(resourceConfigSnapshotEntry)
                log.info(
                    "Stored configuration snapshot for resourceId=($resId), " +
                        "resourceType=($resType), status=($status), " +
                        "dated=(${storedSnapshot.createdDate})"
                )
            } catch (ex: DataIntegrityViolationException) {
                throw BlueprintException("Failed to store configuration snapshot entry.", ex)
            }
            storedSnapshot
        }
}
