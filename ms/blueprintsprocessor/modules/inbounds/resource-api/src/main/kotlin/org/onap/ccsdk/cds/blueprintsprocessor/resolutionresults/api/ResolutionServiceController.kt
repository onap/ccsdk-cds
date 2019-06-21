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

package org.onap.ccsdk.cds.blueprintsprocessor.resolutionresults.api

import io.swagger.annotations.ApiOperation
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolution
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionDBService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/resolution")
open class ResolutionServiceController(private var resourceResolutionDBService: ResourceResolutionDBService) {

    @RequestMapping(path = ["/ping"], method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun ping(): String = runBlocking {
        "Success"
    }

    @RequestMapping(path = ["/fromResolutionKey"],
        method = [RequestMethod.GET])
    @ApiOperation(value = "Fetch a resolution result ",
        notes = "Retrieve a stored resolution value using the blueprint metadata, artifact name, resolution-key along with the name of the resource value to retrieve.")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getFromKey(@RequestParam(value = "bpName") bpName: String,
                   @RequestParam(value = "bpVersion") bpVersion: String,
                   @RequestParam(value = "artifactName") artifactName: String,
                   @RequestParam(value = "resolutionKey") resolutionKey: String)
            : ResponseEntity<List<ResourceResolution>> = runBlocking {

        ResponseEntity.ok()
            .body(resourceResolutionDBService.readArtifact(bpName, bpVersion, artifactName, resolutionKey))
    }

    @RequestMapping(path = ["/fromResolutionKey"],
        method = [RequestMethod.GET])
    @ApiOperation(value = "Fetch a resolution result ",
        notes = "Retrieve a stored resolution value using the blueprint metadata, artifact name, resolution-key along with the name of the resource value to retrieve.")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getFromKey(@RequestParam(value = "bpName") bpName: String,
                   @RequestParam(value = "bpVersion") bpVersion: String,
                   @RequestParam(value = "artifactName") artifactName: String,
                   @RequestParam(value = "resolutionKey") resolutionKey: String,
                   @RequestParam(value = "name") name: String)
            : ResponseEntity<ResourceResolution> = runBlocking {

        ResponseEntity.ok()
            .body(resourceResolutionDBService.readValue(bpName, bpVersion, artifactName, resolutionKey, name))
    }

    @RequestMapping(path = ["/fromResourceId"], method = [RequestMethod.GET])
    @ApiOperation(value = "Fetch a resolution result ",
        notes = "Retrieve a stored resolution value using the blueprint metadata, artifact name, resolution-key along with the name of the resource value to retrieve.")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getFromResourceId(@RequestParam(value = "bpName") bpName: String,
                          @RequestParam(value = "bpVersion") bpVersion: String,
                          @RequestParam(value = "resourceId") resourceId: String,
                          @RequestParam(value = "resourceType") resourceType: String)
            : ResponseEntity<List<ResourceResolution>> = runBlocking {

        ResponseEntity.ok()
            .body(resourceResolutionDBService.readWithResourceId(bpName, bpVersion, resourceId, resourceType))
    }
}
