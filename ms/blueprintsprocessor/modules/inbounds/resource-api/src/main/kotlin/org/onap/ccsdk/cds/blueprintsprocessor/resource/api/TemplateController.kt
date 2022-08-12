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
import org.onap.ccsdk.cds.controllerblueprints.core.httpProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogCodes
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * Exposes Template Resolution API to store and retrieve rendered template results.
 *
 * @author Serge Simard
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/template")
@Api(
    value = "Resource template",
    description = "Interaction with resolved templates"
)
open class TemplateController(private val templateResolutionService: TemplateResolutionService) {

    @RequestMapping(
        path = ["/health-check"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    @ApiOperation(value = "Health Check", hidden = true)
    fun templateControllerHealthCheck(): JsonNode = runBlocking {
        JacksonUtils.getJsonNode("Success")
    }

    @RequestMapping(
        path = [""],
        method = [RequestMethod.GET],
        produces = [MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    @ApiOperation(
        value = "Retrieve a resolved template",
        notes = "Retrieve a config template for a given CBA's action, identified by its blueprint name, blueprint version, " +
            "artifact name and resolution key. An extra 'format' parameter can be passed to tell what content-type" +
            " to expect in return"
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun get(
        @ApiParam(value = "Name of the CBA", required = true)
        @RequestParam(value = "bpName") bpName: String,
        @ApiParam(value = "Version of the CBA", required = true)
        @RequestParam(value = "bpVersion") bpVersion: String,
        @ApiParam(value = "Artifact name for which to retrieve a resolved resource", required = true)
        @RequestParam(value = "artifactName") artifactName: String,
        @ApiParam(value = "Resolution Key associated with the resolution", required = false)
        @RequestParam(value = "resolutionKey", required = false, defaultValue = "") resolutionKey: String,
        @ApiParam(value = "Resource Type associated with the resolution", required = false)
        @RequestParam(value = "resourceType", required = false, defaultValue = "") resourceType: String,
        @ApiParam(value = "Resource Id associated with the resolution", required = false)
        @RequestParam(value = "resourceId", required = false, defaultValue = "") resourceId: String,
        @ApiParam(
            value = "Expected format of the template being retrieved",
            defaultValue = MediaType.TEXT_PLAIN_VALUE,
            required = true
        )
        @RequestParam(value = "format", required = false, defaultValue = MediaType.TEXT_PLAIN_VALUE) format: String,
        @ApiParam(value = "Occurrence of the template resolution (1-n)", required = false)
        @RequestParam(value = "occurrence", required = false, defaultValue = "1") occurrence: Int = 1
    ):
        ResponseEntity<String> = runBlocking {

            var result = ""

            if (resolutionKey.isNotEmpty() && (resourceId.isNotEmpty() || resourceType.isNotEmpty())) {
                throw httpProcessorException(
                    ErrorCatalogCodes.REQUEST_NOT_FOUND, ResourceApiDomains.RESOURCE_API,
                    "Either retrieve resolved template using resolution-key OR using resource-id and resource-type."
                )
            } else if (resolutionKey.isNotEmpty() && artifactName.isNotEmpty()) {
                result = templateResolutionService.findByResolutionKeyAndBlueprintNameAndBlueprintVersionAndArtifactName(
                    bpName,
                    bpVersion,
                    artifactName,
                    resolutionKey,
                    occurrence
                )
            } else if (resourceType.isNotEmpty() && resourceId.isNotEmpty() && artifactName.isNotEmpty()) {
                result =
                    templateResolutionService.findByResoureIdAndResourceTypeAndBlueprintNameAndBlueprintVersionAndArtifactName(
                        bpName,
                        bpVersion,
                        artifactName,
                        resourceId,
                        resourceType,
                        occurrence
                    )
            } else {
                throw httpProcessorException(
                    ErrorCatalogCodes.REQUEST_NOT_FOUND, ResourceApiDomains.RESOURCE_API,
                    "Missing param. Either retrieve resolved template using artifact name and resolution-key OR using resource-id and resource-type."
                )
            }

            var expectedContentType = format
            if (expectedContentType.indexOf('/') < 0) {
                expectedContentType = "application/$expectedContentType"
            }
            val expectedMediaType: MediaType = MediaType.valueOf(expectedContentType)

            ResponseEntity.ok().contentType(expectedMediaType).body(result)
        }

    @PostMapping("/{bpName}/{bpVersion}/{artifactName}/{resolutionKey}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        value = "Store a resolved template w/ resolution-key",
        notes = "Store a template for a given CBA's action, identified by its blueprint name, blueprint version, " +
            "artifact name and resolution key.",
        response = TemplateResolution::class
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun postWithResolutionKey(
        @ApiParam(value = "Name of the CBA", required = true)
        @PathVariable(value = "bpName") bpName: String,
        @ApiParam(value = "Version of the CBA", required = true)
        @PathVariable(value = "bpVersion") bpVersion: String,
        @ApiParam(value = "Artifact name for which to retrieve a resolved resource", required = true)
        @PathVariable(value = "artifactName") artifactName: String,
        @ApiParam(value = "Resolution Key associated with the resolution", required = true)
        @PathVariable(value = "resolutionKey") resolutionKey: String,
        @ApiParam(value = "Template to store", required = true)
        @RequestBody result: String
    ): ResponseEntity<TemplateResolution> = runBlocking {

        val resultStored =
            templateResolutionService.write(bpName, bpVersion, artifactName, result, resolutionKey = resolutionKey)

        ResponseEntity.ok().body(resultStored)
    }

    @RequestMapping(
        path = ["/occurrences"],
        method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(
        value = "Get the map of resolved templates with 'occurrence' as the keys to the resolved templates ",
        notes = "With optional 'occurrence' options, subset of stored resolved templates can be retrieved " +
            "using the blueprint name, blueprint version, artifact name and the resolution-key.",
        response = TemplateResolution::class,
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
        @ApiParam(value = "Number of earlier N occurrences of the templates.", required = false)
        @RequestParam(value = "firstN", required = false) firstN: Int?,
        @ApiParam(value = "Number of latest N occurrences of the templates.", required = false)
        @RequestParam(value = "lastN", required = false) lastN: Int?,
        @ApiParam(value = "For Range option - 'begin' is the start occurrence of range of the templates.", required = false)
        @RequestParam(value = "begin", required = false) begin: Int?,
        @ApiParam(value = "For Range option - 'end' is the end occurrence of the range of the templates.", required = false)
        @RequestParam(value = "end", required = false) end: Int?
    ): ResponseEntity<Map<Int, List<TemplateResolution>>> = runBlocking {
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
                templateResolutionService.findFirstNOccurrences(bpName, bpVersion, artifactName, resolutionKey, firstN)
            lastN != null ->
                templateResolutionService.findLastNOccurrences(bpName, bpVersion, artifactName, resolutionKey, lastN)
            begin != null && end != null ->
                templateResolutionService.findOccurrencesWithinRange(bpName, bpVersion, artifactName, resolutionKey, begin, end)
            else ->
                templateResolutionService.readWithResolutionKey(bpName, bpVersion, artifactName, resolutionKey).groupBy(TemplateResolution::occurrence).toSortedMap(reverseOrder())
        }.let { result -> ResponseEntity.ok().body(result) }
    }

    @PostMapping(
        "/{bpName}/{bpVersion}/{artifactName}/{resourceType}/{resourceId}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(
        value = "Store a resolved template w/ resourceId and resourceType",
        notes = "Store a template for a given CBA's action, identified by its blueprint name, blueprint version, " +
            "artifact name, resourceId and resourceType.",
        response = TemplateResolution::class
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun postWithResourceIdAndResourceType(
        @ApiParam(value = "Name of the CBA", required = true)
        @PathVariable(value = "bpName") bpName: String,
        @ApiParam(value = "Version of the CBA", required = true)
        @PathVariable(value = "bpVersion") bpVersion: String,
        @ApiParam(value = "Artifact name for which to retrieve a resolved resource", required = true)
        @PathVariable(value = "artifactName") artifactName: String,
        @ApiParam(value = "Resource Type associated with the resolution", required = false)
        @PathVariable(value = "resourceType", required = true) resourceType: String,
        @ApiParam(value = "Resource Id associated with the resolution", required = false)
        @PathVariable(value = "resourceId", required = true) resourceId: String,
        @ApiParam(value = "Template to store", required = true)
        @RequestBody result: String
    ): ResponseEntity<TemplateResolution> = runBlocking {

        val resultStored =
            templateResolutionService.write(bpName, bpVersion, artifactName, result, resourceId = resourceId, resourceType = resourceType)

        ResponseEntity.ok().body(resultStored)
    }

    @RequestMapping(
        path = [""],
        method = [RequestMethod.DELETE], produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @PreAuthorize("hasRole('USER')")
    fun deleteTemplates(
        @ApiParam(value = "Name of the CBA", required = true)
        @RequestParam(value = "bpName", required = true) bpName: String,
        @ApiParam(value = "Version of the CBA", required = true)
        @RequestParam(value = "bpVersion", required = true) bpVersion: String,
        @ApiParam(value = "Artifact name", required = true)
        @RequestParam(value = "artifactName", required = true, defaultValue = "") artifactName: String,
        @ApiParam(value = "Resolution Key associated with the template", required = false)
        @RequestParam(value = "resolutionKey", required = false) resolutionKey: String?,
        @ApiParam(value = "resourceType associated with the template, must be used with resourceId", required = false)
        @RequestParam(value = "resourceType", required = false) resourceType: String?,
        @ApiParam(value = "Resolution Key associated with the template, must be used with resourceType", required = false)
        @RequestParam(value = "resourceId", required = false) resourceId: String?,
        @ApiParam(value = "Only delete last N occurrences", required = false)
        @RequestParam(value = "lastN", required = false) lastN: Int?
    ) = runBlocking {
        when {
            resolutionKey?.isNotBlank() == true -> templateResolutionService.deleteTemplates(
                bpName, bpVersion, artifactName, resolutionKey, lastN
            )
            resourceType?.isNotBlank() == true && resourceId?.isNotBlank() == true ->
                templateResolutionService.deleteTemplates(
                    bpName, bpVersion, artifactName, resourceType, resourceId, lastN
                )
            else -> throw httpProcessorException(
                ErrorCatalogCodes.REQUEST_NOT_FOUND,
                ResourceApiDomains.RESOURCE_API,
                "Either use resolutionKey or resourceType + resourceId. Values cannot be blank"
            )
        }.let { ResponseEntity.ok().body(DeleteResponse(it)) }
    }
}
