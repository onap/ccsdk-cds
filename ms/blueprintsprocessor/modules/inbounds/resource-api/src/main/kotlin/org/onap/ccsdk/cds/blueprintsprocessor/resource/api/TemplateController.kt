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
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.TemplateResolution
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.TemplateResolutionService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * Exposes Template Resolution API to store and retrieve rendered template results.
 *
 * @author Serge Simard
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/templates")
@Api(value = "/api/v1/template",
    description = "Interaction with resolved template.")
open class TemplateController(private val templateResolutionService: TemplateResolutionService) {

    @RequestMapping(path = ["/health-check"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation(value = "Health Check", hidden = true)
    fun templateControllerHealthCheck(): JsonNode = runBlocking {
        JacksonUtils.getJsonNode("Success")
    }

    @RequestMapping(
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Retrieve all resolved template.",
        notes = "To retrieve resolved templates provide either resolution-key OR resource-id and resource-type.")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getAll(
        @ApiParam(value = "Name of the CBA.", required = true)
        @RequestParam(value = "bpName") bpName: String,
        @ApiParam(value = "Version of the CBA.", required = true)
        @RequestParam(value = "bpVersion") bpVersion: String,
        @ApiParam(value = "Resolution Key associated with the resolution.", required = false, defaultValue = "")
        @RequestParam(value = "resolutionKey") resolutionKey: String,
        @ApiParam(value = "Resource Type associated with the resolution.", required = false)
        @RequestParam(value = "resourceType", required = false, defaultValue = "") resourceType: String,
        @ApiParam(value = "Resource Id associated with the resolution.", required = false)
        @RequestParam(value = "resourceId", required = false, defaultValue = "") resourceId: String)
            : ResponseEntity<List<TemplateResolution>> = runBlocking {

        val result: List<TemplateResolution>

        if (resolutionKey.isNotEmpty() && (resourceId.isNotEmpty() || resourceType.isNotEmpty())) {
            throw ResolutionException("To retrieve resolved templates provide either resolution-key OR resource-id and resource-type.")
        } else if (resolutionKey.isNotEmpty()) {
            result = templateResolutionService.findByResolutionKeyAndBlueprintNameAndBlueprintVersion(
                bpName,
                bpVersion,
                resolutionKey)
        } else if (resourceType.isNotEmpty() && resourceId.isNotEmpty()) {
            result = templateResolutionService.findByResourceIdAndResourceTypeAndBlueprintNameAndBlueprintVersion(
                bpName,
                bpVersion,
                resourceId,
                resourceType)
        } else {
            throw ResolutionException("Missing param. To retrieve resolved templates provide either resolution-key OR resource-id and resource-type.")
        }

        result.let {
            ResponseEntity.ok().body(it)
        } ?: ResponseEntity.notFound().build()
    }

    @RequestMapping(path = ["/template"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Retrieve resolved template for a given artifact name.",
        notes = "To retrieve resolved templates provide artifact name along with resolution-key OR resource-id and resource-type.")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getOne(
        @ApiParam(value = "Name of the CBA.", required = true)
        @RequestParam(value = "bpName") bpName: String,
        @ApiParam(value = "Version of the CBA.", required = true)
        @RequestParam(value = "bpVersion") bpVersion: String,
        @ApiParam(value = "Artifact name for which to retrieve a resolved resource.", required = false)
        @RequestParam(value = "artifactName", required = false, defaultValue = "") artifactName: String,
        @ApiParam(value = "Resolution Key associated with the resolution.", required = false, defaultValue = "")
        @RequestParam(value = "resolutionKey") resolutionKey: String,
        @ApiParam(value = "Resource Type associated with the resolution.", required = false)
        @RequestParam(value = "resourceType", required = false, defaultValue = "") resourceType: String,
        @ApiParam(value = "Resource Id associated with the resolution.", required = false)
        @RequestParam(value = "resourceId", required = false, defaultValue = "") resourceId: String)
            : ResponseEntity<List<TemplateResolution>> = runBlocking {

        val result: List<TemplateResolution>

        if ((resolutionKey.isNotEmpty() || artifactName.isNotEmpty()) && (resourceId.isNotEmpty() || resourceType.isNotEmpty())) {
            throw ResolutionException("To retrieve resolved templates provide artifact name along with resolution-key OR resource-id and resource-type.")
        } else if (resolutionKey.isNotEmpty() && artifactName.isNotEmpty()) {
            result =
                templateResolutionService.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
                    bpName,
                    bpVersion,
                    artifactName,
                    resolutionKey)

        } else if (resourceType.isNotEmpty() && resourceId.isNotEmpty()) {
            result =
                templateResolutionService.findByResourceIdAndResourceTypeAndBlueprintNameAndBlueprintVersionAndArtifactName(
                    bpName,
                    bpVersion,
                    artifactName,
                    resourceId,
                    resourceType)
        } else {
            throw ResolutionException("Missing param. To retrieve resolved templates provide artifact name along with resolution-key OR resource-id and resource-type.")
        }

        result.let {
            ResponseEntity.ok().body(it)
        } ?: ResponseEntity.notFound().build()
    }

    @PostMapping("/{bpName}/{bpVersion}/{artifactName}/{resolutionKey}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Store a resolved template w/ resolution-key",
        notes = "Store a template for a given CBA's action, identified by its blueprint name, blueprint version, " +
                "artifact name and resolution key.",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun postWithResolutionKey(
        @ApiParam(value = "Name of the CBA.", required = true)
        @PathVariable(value = "bpName") bpName: String,
        @ApiParam(value = "Version of the CBA.", required = true)
        @PathVariable(value = "bpVersion") bpVersion: String,
        @ApiParam(value = "Artifact name for which to retrieve a resolved resource.", required = true)
        @PathVariable(value = "artifactName") artifactName: String,
        @ApiParam(value = "Resolution Key associated with the resolution.", required = true)
        @PathVariable(value = "resolutionKey") resolutionKey: String,
        @ApiParam(value = "Template to store.", required = true)
        @RequestBody result: String): ResponseEntity<TemplateResolution> = runBlocking {

        val resultStored =
            templateResolutionService.write(bpName, bpVersion, artifactName, result, resolutionKey = resolutionKey)

        ResponseEntity.ok().body(resultStored)
    }

    @PostMapping("/{bpName}/{bpVersion}/{artifactName}/{resourceType}/{resourceId}",
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Store a resolved template w/ resourceId and resourceType",
        notes = "Store a template for a given CBA's action, identified by its blueprint name, blueprint version, " +
                "artifact name, resourceId and resourceType.",
        response = TemplateResolution::class,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun postWithResourceIdAndResourceType(
        @ApiParam(value = "Name of the CBA.", required = true)
        @PathVariable(value = "bpName") bpName: String,
        @ApiParam(value = "Version of the CBA.", required = true)
        @PathVariable(value = "bpVersion") bpVersion: String,
        @ApiParam(value = "Artifact name for which to retrieve a resolved resource.", required = true)
        @PathVariable(value = "artifactName") artifactName: String,
        @ApiParam(value = "Resource Type associated with the resolution.", required = false)
        @PathVariable(value = "resourceType", required = true) resourceType: String,
        @ApiParam(value = "Resource Id associated with the resolution.", required = false)
        @PathVariable(value = "resourceId", required = true) resourceId: String,
        @ApiParam(value = "Template to store.", required = true)
        @RequestBody result: String): ResponseEntity<TemplateResolution> = runBlocking {

        val resultStored =
            templateResolutionService.write(bpName,
                bpVersion,
                artifactName,
                result,
                resourceId = resourceId,
                resourceType = resourceType)

        ResponseEntity.ok().body(resultStored)
    }
}