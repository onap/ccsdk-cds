/*
 * Copyright © 2019 Bell Canada
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

package org.onap.ccsdk.cds.blueprintsprocessor.resource.api

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolution
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionDBService
import org.onap.ccsdk.cds.controllerblueprints.core.httpProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogCodes
import org.onap.ccsdk.cds.error.catalog.core.ErrorPayload
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/resources")
@Api(
    value = "Resources",
    description = "Interaction with resolved resources"
)
open class ResourceController(private var resourceResolutionDBService: ResourceResolutionDBService) {

    @RequestMapping(
        path = ["/health-check"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    @ApiOperation(value = "Health Check", hidden = true)
    fun resourceControllerHealthCheck(): JsonNode = runBlocking {
        JacksonUtils.getJsonNode("Success")
    }

    @RequestMapping(
        method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(
        value = "Get all resolved resources using the resolution key",
        notes = "Retrieve all stored resolved resources using the blueprint name, blueprint version, " +
            "artifact name and the resolution-key.",
        response = ResourceResolution::class,
        responseContainer = "List",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getAllFromResolutionKeyOrFromResourceTypeAndId(
        @ApiParam(value = "Name of the CBA", required = true)
        @RequestParam(value = "bpName", required = true) bpName: String,
        @ApiParam(value = "Version of the CBA", required = true)
        @RequestParam(value = "bpVersion", required = true) bpVersion: String,
        @ApiParam(value = "Artifact name for which to retrieve a resolved resource", required = true)
        @RequestParam(value = "artifactName", required = false, defaultValue = "") artifactName: String,
        @ApiParam(value = "Resolution Key associated with the resolution", required = false)
        @RequestParam(value = "resolutionKey", required = false, defaultValue = "") resolutionKey: String,
        @ApiParam(value = "Resource Type associated with the resolution", required = false)
        @RequestParam(value = "resourceType", required = false, defaultValue = "") resourceType: String,
        @ApiParam(value = "Resource Id associated with the resolution", required = false)
        @RequestParam(value = "resourceId", required = false, defaultValue = "") resourceId: String
    ):
        ResponseEntity<List<ResourceResolution>> = runBlocking {

            if ((resolutionKey.isNotEmpty() || artifactName.isNotEmpty()) && (resourceId.isNotEmpty() || resourceType.isNotEmpty())) {
                throw httpProcessorException(
                    ErrorCatalogCodes.REQUEST_NOT_FOUND, ResourceApiDomains.RESOURCE_API,
                    "Either retrieve resolved value using artifact name and resolution-key OR using resource-id and resource-type."
                )
            } else if (resolutionKey.isNotEmpty() && artifactName.isNotEmpty()) {
                ResponseEntity.ok()
                    .body(resourceResolutionDBService.readWithResolutionKey(bpName, bpVersion, artifactName, resolutionKey))
            } else if (resourceType.isNotEmpty() && resourceId.isNotEmpty()) {
                ResponseEntity.ok()
                    .body(
                        resourceResolutionDBService.readWithResourceIdAndResourceType(
                            bpName,
                            bpVersion,
                            resourceId,
                            resourceType
                        )
                    )
            } else {
                throw httpProcessorException(
                    ErrorCatalogCodes.REQUEST_NOT_FOUND, ResourceApiDomains.RESOURCE_API,
                    "Missing param. Either retrieve resolved value using artifact name and resolution-key OR using resource-id and resource-type."
                )
            }
        }

    @RequestMapping(
        path = ["/occurrences"],
        method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(
        value = "Get the map of resolved resources with 'occurrence' as the keys to the resolved resources ",
        notes = "With optional 'occurrence' options, subset of stored resolved resources can be retrieved " +
            "using the blueprint name, blueprint version, artifact name and the resolution-key.",
        response = ResourceResolution::class,
        responseContainer = "List",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getOccurrences(
        @ApiParam(value = "Name of the CBA.", required = true)
        @RequestParam(value = "bpName", required = true) bpName: String,
        @ApiParam(value = "Version of the CBA.", required = true)
        @RequestParam(value = "bpVersion", required = true) bpVersion: String,
        @ApiParam(value = "Artifact name for which to retrieve a resolved resource.", required = true)
        @RequestParam(value = "artifactName", required = true, defaultValue = "") artifactName: String,
        @ApiParam(value = "Resolution Key associated with the resolution.", required = true)
        @RequestParam(value = "resolutionKey", required = true, defaultValue = "") resolutionKey: String,
        @ApiParam(value = "Number of earlier N occurrences of the resolutions.", required = false)
        @RequestParam(value = "firstN", required = false) firstN: Int?,
        @ApiParam(value = "Number of latest N occurrences of the resolutions.", required = false)
        @RequestParam(value = "lastN", required = false) lastN: Int?,
        @ApiParam(value = "For Range option - 'begin' is the start occurrence of range of the resolutions.", required = false)
        @RequestParam(value = "begin", required = false) begin: Int?,
        @ApiParam(value = "For Range option - 'end' is the end occurrence of the range of the resolutions.", required = false)
        @RequestParam(value = "end", required = false) end: Int?
    ): ResponseEntity<Map<Int, List<ResourceResolution>>> = runBlocking {
        when {
            artifactName.isEmpty() -> "'artifactName' must not be empty"
            resolutionKey.isEmpty() -> "'resolutionKey' must not be empty"
            // Optional options - validate if provided
            (firstN != null && lastN != null) -> "Retrieve occurrences using either 'firstN'  OR 'lastN' option"
            ((firstN != null || lastN != null) && (begin != null || end != null)) -> "Retrieve occurrences using either 'firstN'  OR 'lastN' OR 'begin' and 'end' option."
            ((begin != null && end == null) || (begin == null && end != null)) -> " Retrieving occurrences within range - please provide both 'begin' and 'end' option"
            else -> null
        }?.let { throw httpProcessorException(ErrorCatalogCodes.REQUEST_NOT_FOUND, ResourceApiDomains.RESOURCE_API, it) }

        when {
            firstN != null ->
                resourceResolutionDBService.findFirstNOccurrences(bpName, bpVersion, artifactName, resolutionKey, firstN)
            lastN != null ->
                resourceResolutionDBService.findLastNOccurrences(bpName, bpVersion, artifactName, resolutionKey, lastN)
            begin != null && end != null ->
                resourceResolutionDBService.findOccurrencesWithinRange(bpName, bpVersion, artifactName, resolutionKey, begin, end)
            else ->
                resourceResolutionDBService.readWithResolutionKey(bpName, bpVersion, artifactName, resolutionKey).groupBy(ResourceResolution::occurrence).toSortedMap(reverseOrder())
        }.let { result -> ResponseEntity.ok().body(result) }
    }

    @RequestMapping(
        method = [RequestMethod.DELETE], produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(
        value = "Delete resources using resolution key",
        notes = "Delete all the resources associated to a resolution-key using blueprint metadata, artifact name and the resolution-key."
    )
    @PreAuthorize("hasRole('USER')")
    fun deleteByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKey(
        @ApiParam(value = "Name of the CBA", required = true)
        @RequestParam(value = "bpName", required = true) bpName: String,
        @ApiParam(value = "Version of the CBA", required = true)
        @RequestParam(value = "bpVersion", required = true) bpVersion: String,
        @ApiParam(value = "Artifact name for which to retrieve a resolved resource", required = true)
        @RequestParam(value = "artifactName", required = false, defaultValue = "") artifactName: String,
        @ApiParam(value = "Resolution Key associated with the resolution", required = true)
        @RequestParam(value = "resolutionKey", required = true) resolutionKey: String
    ) = runBlocking {
        ResponseEntity.ok()
            .body(
                resourceResolutionDBService.deleteByBlueprintNameAndBlueprintVersionAndArtifactNameAndResolutionKey(
                    bpName,
                    bpVersion,
                    artifactName,
                    resolutionKey
                )
            )
    }

    @RequestMapping(
        path = ["/resource"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(
        value = "Fetch a resource value using resolution key",
        notes = "Retrieve a stored resource value using the blueprint metadata, artifact name, resolution-key along with the name of the resource value to retrieve."
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getOneFromResolutionKey(
        @ApiParam(value = "Name of the CBA", required = true)
        @RequestParam(value = "bpName", required = true) bpName: String,
        @ApiParam(value = "Version of the CBA", required = true)
        @RequestParam(value = "bpVersion", required = true) bpVersion: String,
        @ApiParam(value = "Artifact name for which to retrieve a resolved resource", required = true)
        @RequestParam(value = "artifactName", required = true) artifactName: String,
        @ApiParam(value = "Resolution Key associated with the resolution", required = true)
        @RequestParam(value = "resolutionKey", required = true) resolutionKey: String,
        @ApiParam(value = "Name of the resource to retrieve", required = true)
        @RequestParam(value = "name", required = true) name: String
    ):
        ResponseEntity<out Any>? = runBlocking {

            var result: ResourceResolution? = resourceResolutionDBService.readValue(bpName, bpVersion, artifactName, resolutionKey, name)
            if (result != null) {
                ResponseEntity.ok().body(result)
            } else {
                var errorPayload = ErrorPayload(HttpStatus.NOT_FOUND.value(), ErrorCatalogCodes.GENERIC_FAILURE, "No records found")
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorPayload)
            }
        }
}
