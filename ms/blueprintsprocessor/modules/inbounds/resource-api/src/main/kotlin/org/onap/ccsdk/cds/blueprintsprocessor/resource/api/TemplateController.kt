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
@RequestMapping("/api/v1/template")
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

    @RequestMapping(path = [""],
        method = [RequestMethod.GET],
        produces = [MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
    @ApiOperation(value = "Retrieve a resolved template.",
        notes = "Retrieve a config template for a given CBA's action, identified by its blueprint name, blueprint version, " +
                "artifact name and resolution key. An extra 'format' parameter can be passed to tell what content-type" +
                " to expect in return")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun get(
        @ApiParam(value = "Name of the CBA.", required = true)
        @RequestParam(value = "bpName") bpName: String,
        @ApiParam(value = "Version of the CBA.", required = true)
        @RequestParam(value = "bpVersion") bpVersion: String,
        @ApiParam(value = "Artifact name for which to retrieve a resolved resource.", required = true)
        @RequestParam(value = "artifactName") artifactName: String,
        @ApiParam(value = "Resolution Key associated with the resolution.", required = true)
        @RequestParam(value = "resolutionKey") resolutionKey: String,
        @ApiParam(value = "Expected format of the template being retrieved.",
            defaultValue = MediaType.TEXT_PLAIN_VALUE,
            required = true)
        @RequestParam(value = "format", required = false, defaultValue = MediaType.TEXT_PLAIN_VALUE) format: String)
            : ResponseEntity<String> = runBlocking {

        val result = templateResolutionService.read(bpName, bpVersion, artifactName, resolutionKey)

        var expectedContentType = format
        if (expectedContentType.indexOf('/') < 0) {
            expectedContentType = "application/$expectedContentType"
        }
        val expectedMediaType: MediaType = MediaType.valueOf(expectedContentType)

        ResponseEntity.ok().contentType(expectedMediaType).body(result)
    }


    @PostMapping("/{bpName}/{bpVersion}/{artifactName}/{resolutionKey}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Store a resolved template",
        notes = "Store a template for a given CBA's action, identified by its blueprint name, blueprint version, " +
                "artifact name and resolution key.",
        response = TemplateResolution::class,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun post(@ApiParam(value = "Name of the CBA.", required = true)
             @RequestParam(value = "bpName") bpName: String,
             @ApiParam(value = "Version of the CBA.", required = true)
             @RequestParam(value = "bpVersion") bpVersion: String,
             @ApiParam(value = "Artifact name for which to retrieve a resolved resource.", required = true)
             @RequestParam(value = "artifactName") artifactName: String,
             @ApiParam(value = "Resolution Key associated with the resolution.", required = true)
             @RequestParam(value = "resolutionKey") resolutionKey: String,
             @ApiParam(value = "Template to store.", required = true)
             @RequestBody result: String): ResponseEntity<TemplateResolution> = runBlocking {

        val resultStored =
            templateResolutionService.write(bpName, bpVersion, resolutionKey, artifactName, result)

        ResponseEntity.ok().body(resultStored)
    }
}
