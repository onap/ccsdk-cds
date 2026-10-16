/*
 * Copyright © 2021 Bell Canada
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

package org.onap.ccsdk.cds.blueprintsprocessor.configs.api

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody as RequestBodyDoc
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.db.ResourceConfigSnapshot
import org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.db.ResourceConfigSnapshotService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.mdcWebCoroutineScope
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.httpProcessorException
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogCodes
import org.onap.ccsdk.cds.error.catalog.core.utils.errorCauseOrDefault
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.Optional

/**
 * Exposes Resource Configuration Snapshot API to store and retrieve stored resource configurations.
 *
 * @author Serge Simard
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/configs")
@Tag(
    name = "Resource configuration",
    description = "Interaction with stored configurations"
)
open class ResourceConfigSnapshotController(private val resourceConfigSnapshotService: ResourceConfigSnapshotService) {

    private val JSON_MIME_TYPE = "application/json"

    @RequestMapping(
        path = ["/health-check"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    @Operation(summary = "Health Check", hidden = true)
    fun ressCfgSnapshotControllerHealthCheck(): JsonNode = runBlocking {
        "Success".asJsonPrimitive()
    }

    @RequestMapping(
        method = [RequestMethod.GET],
        produces = [MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    @Operation(
        summary = "Retrieve a resource configuration snapshot",
        description = "Retrieve a config snapshot, identified by its Resource Id and Type. " +
            "An extra 'format' parameter can be passed to tell what content-type is expected."
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun get(
        @Parameter(description = "Resource Type associated of the resource configuration snapshot", required = false, example = "PNF")
        @RequestParam(value = "resourceType", required = true) resourceType: String,

        @Parameter(description = "Resource Id associated of the resource configuration snapshot", required = false, example = "1")
        @RequestParam(value = "resourceId", required = true) resourceId: String,

        @Parameter(description = "Status of the snapshot being retrieved", required = false)
        @RequestParam(value = "status", required = false, defaultValue = "RUNNING") status: String,

        @Parameter(
            description = "Expected format of the snapshot being retrieved",
            required = false
        )
        @RequestParam(value = "format", required = false, defaultValue = MediaType.TEXT_PLAIN_VALUE) format: String
    ):

        ResponseEntity<String> = runBlocking {

            var configSnapshot = ""

            if (resourceType.isNotEmpty() && resourceId.isNotEmpty()) {
                try {
                    configSnapshot = resourceConfigSnapshotService.findByResourceIdAndResourceTypeAndStatus(
                        resourceId,
                        resourceType, ResourceConfigSnapshot.Status.valueOf(status.toUpperCase())
                    )
                } catch (ex: NoSuchElementException) {
                    throw httpProcessorException(
                        ErrorCatalogCodes.RESOURCE_NOT_FOUND, ConfigsApiDomains.CONFIGS_API,
                        "Could not find configuration snapshot entry for type $resourceType and Id $resourceId",
                        ex.errorCauseOrDefault()
                    )
                } catch (ex: Exception) {
                    throw httpProcessorException(
                        ErrorCatalogCodes.INVALID_REQUEST_FORMAT, ConfigsApiDomains.CONFIGS_API,
                        "Could not find configuration snapshot entry for type $resourceType and Id $resourceId",
                        ex.errorCauseOrDefault()
                    )
                }
            } else {
                throw httpProcessorException(
                    ErrorCatalogCodes.INVALID_REQUEST_FORMAT, ConfigsApiDomains.CONFIGS_API,
                    "Missing param. You must specify resource-id and resource-type."
                )
            }

            var expectedContentType = format
            if (expectedContentType.indexOf('/') < 0) {
                expectedContentType = "application/$expectedContentType"
            }
            val expectedMediaType: MediaType = MediaType.valueOf(expectedContentType)

            ResponseEntity.ok().contentType(expectedMediaType).body(configSnapshot)
        }

    @PostMapping(
        "/{resourceType}/{resourceId}/{status}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Store a resource configuration snapshot identified by resourceId, resourceType, status",
        description = "Store a resource configuration snapshot, identified by its resourceId and resourceType, " +
            "and optionally its status, either RUNNING or CANDIDATE."
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun postWithResourceIdAndResourceType(
        @Parameter(description = "Resource Type associated with the resolution", required = false, example = "PNF")
        @PathVariable(value = "resourceType", required = true) resourceType: String,
        @Parameter(description = "Resource Id associated with the resolution", required = false, example = "1")
        @PathVariable(value = "resourceId", required = true) resourceId: String,
        @Parameter(description = "Status of the snapshot being retrieved", required = true)
        @PathVariable(value = "status", required = true) status: String,
        @RequestBodyDoc(description = "Config snapshot to store", required = true, content = [Content(schema = Schema(example = "config_snapshot"))])
        @RequestBody snapshot: String
    ): ResponseEntity<ResourceConfigSnapshot> = runBlocking {

        val resultStored =
            resourceConfigSnapshotService.write(
                snapshot, resourceId, resourceType,
                ResourceConfigSnapshot.Status.valueOf(status.toUpperCase())
            )

        ResponseEntity.ok().body(resultStored)
    }

    @DeleteMapping(
        "/{resourceType}/{resourceId}/{status}",
        "/{resourceType}/{resourceId}"
    )
    @Operation(
        summary = "Delete a resource configuration snapshot identified by resourceId, resourceType, status.",
        description = "Delete a resource configuration snapshot, identified by its resourceId and resourceType, " +
            "and optionally its status, either RUNNING or CANDIDATE."
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    suspend fun deleteWithResourceIdAndResourceType(
        @Parameter(description = "Resource Type associated with the resolution.", required = true)
        @PathVariable(value = "resourceType", required = true) resourceType: String,
        @Parameter(description = "Resource Id associated with the resolution.", required = true)
        @PathVariable(value = "resourceId", required = true) resourceId: String,
        @Parameter(description = "Status of the snapshot being deleted.", required = false)
        @PathVariable(value = "status", required = false) status: Optional<String>
    ) = mdcWebCoroutineScope {

        if (resourceId.isBlank() || resourceType.isBlank())
            throw httpProcessorException(
                ErrorCatalogCodes.INVALID_REQUEST_FORMAT, ConfigsApiDomains.CONFIGS_API,
                "You must specify path variables resource-id and resource-type."
            )

        try {
            if (status.isPresent)
                resourceConfigSnapshotService.deleteByResourceIdAndResourceTypeAndStatus(
                    resourceId, resourceType,
                    ResourceConfigSnapshot.Status.valueOf(status.get().toUpperCase())
                )
            else {
                resourceConfigSnapshotService.deleteByResourceIdAndResourceTypeAndStatus(
                    resourceId, resourceType,
                    ResourceConfigSnapshot.Status.RUNNING
                )
                resourceConfigSnapshotService.deleteByResourceIdAndResourceTypeAndStatus(
                    resourceId, resourceType,
                    ResourceConfigSnapshot.Status.CANDIDATE
                )
            }
        } catch (ex: Exception) {
            throw httpProcessorException(
                ErrorCatalogCodes.INVALID_REQUEST_FORMAT, ConfigsApiDomains.CONFIGS_API,
                "Could not delete configuration snapshot entry for type $resourceType and Id $resourceId",
                ex.errorCauseOrDefault()
            )
        }
    }

    @RequestMapping(
        path = ["/allByID"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Retrieve all resource configuration snapshots identified by a given resource_id",
        description = "Retrieve all config snapshots, identified by its Resource Id, ordered by most recently created/modified date. "
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getAllByID(
        @Parameter(description = "Resource Id associated of the resource configuration snapshots", required = false, example = "1")
        @RequestParam(value = "resourceId", required = true) resourceId: String,
        @Parameter(description = "Status of the snapshot being retrieved", required = false)
        @RequestParam(value = "status", required = false, defaultValue = "ANY") status: String
    ): ResponseEntity<List<ResourceConfigSnapshot>?> = runBlocking {
        var configSnapshots: List<ResourceConfigSnapshot>?

        try {
            if (status == "ANY") {
                configSnapshots = resourceConfigSnapshotService.findAllByResourceId(resourceId)
            } else {
                configSnapshots = resourceConfigSnapshotService.findAllByResourceIdForStatus(
                    resourceId, ResourceConfigSnapshot.Status.valueOf(status.toUpperCase())
                )
            }
        } catch (ex: NoSuchElementException) {
            throw httpProcessorException(
                ErrorCatalogCodes.RESOURCE_NOT_FOUND, ConfigsApiDomains.CONFIGS_API,
                "Could not find configuration snapshot entry for ID $resourceId",
                ex.errorCauseOrDefault()
            )
        } catch (ex: Exception) {
            throw httpProcessorException(
                ErrorCatalogCodes.INVALID_REQUEST_FORMAT, ConfigsApiDomains.CONFIGS_API,
                "Unexpected error while finding configuration snapshot entries for ID $resourceId",
                ex.errorCauseOrDefault()
            )
        }

        val expectedMediaType: MediaType = MediaType.valueOf(JSON_MIME_TYPE)
        ResponseEntity.ok().contentType(expectedMediaType).body(configSnapshots)
    }

    @RequestMapping(
        path = ["allByType"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Retrieve all resource configuration snapshots for a given resource type",
        description = "Retrieve all config snapshots matching a specified Resource Type, ordered by most recently created/modified date. "
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getAllByType(
        @Parameter(description = "Resource Type associated of the resource configuration snapshot", required = false, example = "PNF")
        @RequestParam(value = "resourceType", required = true) resourceType: String,
        @Parameter(description = "Status of the snapshot being retrieved", required = false)
        @RequestParam(value = "status", required = false, defaultValue = "ANY") status: String
    ): ResponseEntity<List<ResourceConfigSnapshot>?> = runBlocking {
        var configSnapshots: List<ResourceConfigSnapshot>?

        try {
            if (status == "ANY") {
                configSnapshots = resourceConfigSnapshotService.findAllByResourceType(resourceType)
            } else {
                configSnapshots = resourceConfigSnapshotService.findAllByResourceTypeForStatus(
                    resourceType, ResourceConfigSnapshot.Status.valueOf(status.toUpperCase())
                )
            }
        } catch (ex: NoSuchElementException) {
            throw httpProcessorException(
                ErrorCatalogCodes.RESOURCE_NOT_FOUND, ConfigsApiDomains.CONFIGS_API,
                "Could not find configuration snapshot entry for ID $resourceType",
                ex.errorCauseOrDefault()
            )
        } catch (ex: Exception) {
            throw httpProcessorException(
                ErrorCatalogCodes.INVALID_REQUEST_FORMAT, ConfigsApiDomains.CONFIGS_API,
                "Unexpected error while finding configuration snapshot entries for type $resourceType",
                ex.errorCauseOrDefault()
            )
        }

        val expectedMediaType: MediaType = MediaType.valueOf(JSON_MIME_TYPE)
        ResponseEntity.ok().contentType(expectedMediaType).body(configSnapshots)
    }
}
