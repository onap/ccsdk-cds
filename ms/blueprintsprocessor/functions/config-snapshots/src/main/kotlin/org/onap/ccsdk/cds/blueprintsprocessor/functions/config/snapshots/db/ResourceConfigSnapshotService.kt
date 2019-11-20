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

import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.util.Strings
import org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.db.ResourceConfigSnapshot.Status.RUNNING
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

/**
 * ResourceConfigSnapshot managing service.
 *
 * @author Serge Simard
 * @version 1.0
 */
@Service
class ResourceConfigSnapshotService(private val repository: ResourceConfigSnapshotRepository) {

    private val log = LoggerFactory.getLogger(ResourceConfigSnapshotService::class.toString())

    suspend fun findByResourceIdAndResourceTypeAndStatus(
        resourceId: String,
        resourceType: String,
        status: ResourceConfigSnapshot.Status = RUNNING
    ): String =
        withContext(Dispatchers.IO) {
            repository.findByResourceIdAndResourceTypeAndStatus(resourceId, resourceType, status)
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
                repository.findByResourceIdAndResourceTypeAndStatus(resId, resType, status)
                    ?.let {
                        log.info("Overwriting configuration snapshot entry for resourceId=($resId), " +
                                "resourceType=($resType), status=($status)")
                        repository.deleteByResourceIdAndResourceTypeAndStatus(resId, resType, status)
                    }
            }
            var storedSnapshot: ResourceConfigSnapshot
            try {
                storedSnapshot = repository.saveAndFlush(resourceConfigSnapshotEntry)
                log.info("Stored configuration snapshot for resourceId=($resId), " +
                        "resourceType=($resType), status=($status), " +
                        "dated=(${storedSnapshot.createdDate})")
            } catch (ex: DataIntegrityViolationException) {
                throw BluePrintException("Failed to store configuration snapshot entry.", ex)
            }
            storedSnapshot
        }
}
