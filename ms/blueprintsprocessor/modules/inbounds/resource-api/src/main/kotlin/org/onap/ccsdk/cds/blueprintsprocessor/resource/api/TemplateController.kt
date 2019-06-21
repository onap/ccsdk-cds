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

import io.swagger.annotations.ApiOperation
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.TemplateResolution
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.TemplateResolutionResultService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Exposes Template Resolution API to store and retrieve rendered template results.
 *
 * @author Serge Simard
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/template")
open class TemplateController(private val templateResolutionService: TemplateResolutionResultService) {

    @RequestMapping(path = ["/ping"], method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun ping(): String = runBlocking {
        "Success"
    }

    @RequestMapping(path = [""], method = [RequestMethod.GET], produces = [MediaType.TEXT_PLAIN_VALUE])
    @ApiOperation(value = "Retrieve a meshed template.",
        notes = "Retrieve a meshed template for a given CBA's action, identified by its blueprint name, blueprint version, " +
                "artifact name and resolution key. And extra 'format' parameter can be passed to tell what content-type" +
                " to expect in return")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun get(@RequestParam(value = "bpName") bpName: String,
            @RequestParam(value = "bpVersion") bpVersion: String,
            @RequestParam(value = "artifactName") artifactName: String,
            @RequestParam(value = "resolutionKey") resolutionKey: String,
            @RequestParam(value = "format", required = false, defaultValue = "text/plain") format: String)
            : ResponseEntity<String> = runBlocking {

        val result = templateResolutionService.read(bpName, bpVersion, artifactName, resolutionKey)

        var expectedContentType = format
        if (expectedContentType.indexOf('/') < 0) {
            expectedContentType = "application/$expectedContentType"
        }
        val expectedMediaType: MediaType = MediaType.valueOf(expectedContentType)

        ResponseEntity.ok().contentType(expectedMediaType).body(result)
    }


    @PostMapping("/{bpName}/{bpVersion}/{artifactName}/{resolutionKey}", produces = [MediaType.TEXT_PLAIN_VALUE])
    @ApiOperation(value = "Store a meshed template",
        notes = "Store a meshed template for a given CBA's action, identified by its blueprint name, blueprint version, " +
                "artifact name and resolution key.")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun put(@PathVariable(value = "bpName") bpName: String,
            @PathVariable(value = "bpVersion") bpVersion: String,
            @PathVariable(value = "artifactName") artifactName: String,
            @PathVariable(value = "resolutionKey") resolutionKey: String,
            @RequestBody result: String): ResponseEntity<TemplateResolution> = runBlocking {

        val resultStored =
            templateResolutionService.write(bpName, bpVersion, resolutionKey, artifactName, result)

        ResponseEntity.ok().body(resultStored)
    }
}
