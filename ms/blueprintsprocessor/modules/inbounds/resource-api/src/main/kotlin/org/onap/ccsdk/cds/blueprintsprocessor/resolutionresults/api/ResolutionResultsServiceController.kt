/*
 * Copyright © 2018-2019 Bell Canada Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.resolutionresults.api

import io.swagger.annotations.ApiOperation
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * Exposes Resolution Results API to store and retrieve resource resolution results from external processes,
 * like python or ansible scripts
 *
 * @author Serge Simard
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/resolution-results")
open class ResolutionResultsServiceController {

    @Autowired
    lateinit var resolutionResultsServiceHandler: ResolutionResultsServiceHandler

    @RequestMapping(path = ["/ping"], method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun ping(): String = runBlocking {
        "Success"
    }

    @RequestMapping(path = ["/{resolution_result_id}"], method = [RequestMethod.GET], produces = [MediaType.TEXT_PLAIN_VALUE])
    @ApiOperation(value = "Fetch a stored result by ID",
            notes = "Loads a stored result using the resolution_result_id primary key")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getStoredResultById(@PathVariable(value = "resolution_result_id") resolutionResultId: String)
            : String = runBlocking {
        resolutionResultsServiceHandler.loadStoredResultById(resolutionResultId)
    }

    @RequestMapping(path = ["/"], method = [RequestMethod.GET], produces = [MediaType.TEXT_PLAIN_VALUE])
    @ApiOperation(value = "Fetch a stored result ",
            notes = "Loads a stored result using the blueprint metadata, artifact name and resolution-key")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getStoredResult(@RequestParam(value = "bpName") bpName: String,
                        @RequestParam(value = "bpVersion") bpVersion: String,
                        @RequestParam(value = "artifactName") artifactName: String,
                        @RequestParam(value = "resolutionKey") resolutionKey: String,
                        @RequestParam(value = "format", required = false, defaultValue = "text/plain") format: String)
            : ResponseEntity<String> = runBlocking {

        val payload = resolutionResultsServiceHandler.loadStoredResult(bpName, bpVersion, artifactName, resolutionKey)

        var expectedContentType = format
        if (expectedContentType.indexOf('/') < 0) {
            expectedContentType = "application/$expectedContentType"
        }
        val expectedMediaType : MediaType = MediaType.valueOf(expectedContentType)

        ResponseEntity.ok().contentType(expectedMediaType).body(payload)
    }


    @PostMapping("/{bpName}/{bpVersion}/{artifactName}/{resolutionKey}", produces = [MediaType.TEXT_PLAIN_VALUE])
    @ApiOperation(value = "Store result ",
            notes = "Store result under resolution-key for the specified blueprint/version/artifact.")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun putStoredResult(@PathVariable(value = "bpName") bpName: String,
                      @PathVariable(value = "bpVersion") bpVersion: String,
                      @PathVariable(value = "artifactName") artifactName: String,
                      @PathVariable(value = "resolutionKey") resolutionKey: String,
                      @RequestBody result : String): String? = runBlocking {
        resolutionResultsServiceHandler.saveNewStoredResult(bpName, bpVersion, artifactName, resolutionKey, result).id
    }


    @DeleteMapping(path = ["/{resolution_result_id}"])
    @ApiOperation(value = "Deletes a stored result by ID",
            notes = "Removes a stored result, using the resolution_result_id primary key")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun deleteStoredResult(@PathVariable(value = "resolution_result_id") resolutionResultId: String) = runBlocking {
        resolutionResultsServiceHandler.removeStoredResultById(resolutionResultId)
    }

}
