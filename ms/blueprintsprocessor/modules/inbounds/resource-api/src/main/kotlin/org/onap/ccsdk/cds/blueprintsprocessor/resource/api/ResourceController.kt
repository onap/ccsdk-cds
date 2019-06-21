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
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolution
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db.ResourceResolutionDBService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/resources")
open class ResourceController(private var resourceResolutionDBService: ResourceResolutionDBService) {

    @RequestMapping(path = ["/ping"], method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun ping(): String = runBlocking {
        "Success"
    }

    @RequestMapping(path = [""],
        method = [RequestMethod.GET])
    @ApiOperation(value = "Fetch all resource values associated to a resolution key. ",
        notes = "Retrieve a stored resource value using the blueprint metadata, artifact name and the resolution-key.")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getAllFromKey(@RequestParam(value = "bpName") bpName: String,
                      @RequestParam(value = "bpVersion") bpVersion: String,
                      @RequestParam(value = "artifactName") artifactName: String,
                      @RequestParam(value = "resolutionKey") resolutionKey: String)
            : ResponseEntity<List<ResourceResolution>> = runBlocking {

        ResponseEntity.ok()
            .body(resourceResolutionDBService.readArtifact(bpName, bpVersion, artifactName, resolutionKey))
    }

    @RequestMapping(path = ["/resource"],
        method = [RequestMethod.GET])
    @ApiOperation(value = "Fetch a resource value using resolution key.",
        notes = "Retrieve a stored resource value using the blueprint metadata, artifact name, resolution-key along with the name of the resource value to retrieve.")
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
    @ApiOperation(value = "Fetch all resource result for a given resource id / type combination",
        notes = "Retrieve all stored resource values using the blueprint metadata, artifact name, resource id and resource type.")
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