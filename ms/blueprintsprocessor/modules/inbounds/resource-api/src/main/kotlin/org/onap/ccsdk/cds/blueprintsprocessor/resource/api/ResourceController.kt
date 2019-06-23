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
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/resources")
@Api(value = "/api/v1/resources",
    description = "Interaction with resolved resources.")
open class ResourceController(private var resourceResolutionDBService: ResourceResolutionDBService) {

    @RequestMapping(path = ["/health-check"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation(value = "Health Check", hidden = true)
    fun resourceControllerHealthCheck(): JsonNode = runBlocking {
        JacksonUtils.getJsonNode("Success")
    }

    @RequestMapping(path = [""],
        method = [RequestMethod.GET])
    @ApiOperation(value = "Get all resolved resources using the resolution key. ",
        notes = "Retrieve all stored resolved resources using the blueprint name, blueprint version, " +
                "artifact name and the resolution-key.",
        response = ResourceResolution::class,
        responseContainer = "List",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getAllFromKey(@ApiParam(value = "Name of the CBA.", required = true)
                      @RequestParam(value = "bpName") bpName: String,
                      @ApiParam(value = "Version of the CBA.", required = true)
                      @RequestParam(value = "bpVersion") bpVersion: String,
                      @ApiParam(value = "Artifact name for which to retrieve a resolved resource.", required = true)
                      @RequestParam(value = "artifactName") artifactName: String,
                      @ApiParam(value = "Resolution Key associated with the resolution.", required = true)
                      @RequestParam(value = "resolutionKey") resolutionKey: String)
            : ResponseEntity<List<ResourceResolution>> = runBlocking {

        ResponseEntity.ok()
            .body(resourceResolutionDBService.readArtifact(bpName, bpVersion, artifactName, resolutionKey))
    }

    @RequestMapping(path = ["/resource"],
        method = [RequestMethod.GET])
    @ApiOperation(value = "Get a resolved resource using resolution the key along with the name of the resource.",
        notes = "Retrieve a stored resolved resource value using the blueprint name, blueprint version, artifact name," +
                " resolution-key along with the name of the resource value to retrieve.",
        response = ResourceResolution::class,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getFromKey(@ApiParam(value = "Name of the CBA.", required = true)
                   @RequestParam(value = "bpName") bpName: String,
                   @ApiParam(value = "Version of the CBA.", required = true)
                   @RequestParam(value = "bpVersion") bpVersion: String,
                   @ApiParam(value = "Artifact name for which to retrieve a resolved resource.", required = true)
                   @RequestParam(value = "artifactName") artifactName: String,
                   @ApiParam(value = "Resolution Key associated with the resolution.", required = true)
                   @RequestParam(value = "resolutionKey") resolutionKey: String,
                   @ApiParam(value = "Name of the resource to retrieve.", required = true)
                   @RequestParam(value = "name") name: String)
            : ResponseEntity<ResourceResolution> = runBlocking {

        ResponseEntity.ok()
            .body(resourceResolutionDBService.readValue(bpName, bpVersion, artifactName, resolutionKey, name))
    }


    @RequestMapping(path = ["/fromResourceId"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Get all resolved resources using the combination of resource id / resource type.",
        notes = "Retrieve all stored resolved resource using the blueprint name, blueprint version, artifact name," +
                "resource id and resource type.",
        response = ResourceResolution::class,
        responseContainer = "List",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getFromResourceId(@ApiParam(value = "Name of the CBA.", required = true)
                          @RequestParam(value = "bpName") bpName: String,
                          @ApiParam(value = "Version of the CBA.", required = true)
                          @RequestParam(value = "bpVersion") bpVersion: String,
                          @ApiParam(value = "Resource ID associated with the resolution.", required = true)
                          @RequestParam(value = "resourceId") resourceId: String,
                          @ApiParam(value = "Resource Type associated with the resolution.", required = true)
                          @RequestParam(value = "resourceType") resourceType: String)
            : ResponseEntity<List<ResourceResolution>> = runBlocking {

        ResponseEntity.ok()
            .body(resourceResolutionDBService.readWithResourceId(bpName, bpVersion, resourceId, resourceType))
    }
}