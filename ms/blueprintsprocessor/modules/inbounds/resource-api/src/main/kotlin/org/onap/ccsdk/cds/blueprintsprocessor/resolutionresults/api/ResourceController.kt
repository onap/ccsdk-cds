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
        method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Fetch all resource values associated to a resolution key. ",
        notes = "Retrieve a stored resource value using the blueprint metadata, artifact name and the resolution-key.",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getAllFromResolutionKeyOrFromResourceTypeAndId(@RequestParam(value = "bpName", required = true) bpName: String,
                                @RequestParam(value = "bpVersion", required = true) bpVersion: String,
                                @RequestParam(value = "artifactName", required = false, defaultValue = "") artifactName: String,
                                @RequestParam(value = "resolutionKey", required = false, defaultValue = "") resolutionKey: String,
                                @RequestParam(value = "resourceType", required = false, defaultValue = "") resourceType: String,
                                @RequestParam(value = "resourceId", required = false, defaultValue = "") resourceId: String)
            : ResponseEntity<List<ResourceResolution>> = runBlocking {

        if ((resolutionKey.isNotEmpty() || artifactName.isNotEmpty()) && (resourceId.isNotEmpty() || resourceType.isNotEmpty())) {
            throw ResourceException("Either retrieve resolved value using artifact name and resolution-key OR using resource-id and resource-type.")
        } else if (resolutionKey.isNotEmpty() && artifactName.isNotEmpty()) {
            ResponseEntity.ok()
                .body(resourceResolutionDBService.readWithResolutionKey(bpName, bpVersion, artifactName, resolutionKey))
        } else if (resourceType.isNotEmpty() && resourceId.isNotEmpty()){
                ResponseEntity.ok()
                    .body(resourceResolutionDBService.readWithResourceIdAndResourceType(bpName, bpVersion, resourceId, resourceType))
        } else {
            throw ResourceException("Missing param. Either retrieve resolved value using artifact name and resolution-key OR using resource-id and resource-type.")
        }
    }

    @RequestMapping(path = ["/resource"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Fetch a resource value using resolution key.",
        notes = "Retrieve a stored resource value using the blueprint metadata, artifact name, resolution-key along with the name of the resource value to retrieve.",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun getOneFromResolutionKey(@RequestParam(value = "bpName", required = true) bpName: String,
                                @RequestParam(value = "bpVersion", required = true) bpVersion: String,
                                @RequestParam(value = "artifactName", required = true) artifactName: String,
                                @RequestParam(value = "resolutionKey", required = true) resolutionKey: String,
                                @RequestParam(value = "name", required = true) name: String)
            : ResponseEntity<ResourceResolution> = runBlocking {

        ResponseEntity.ok()
            .body(resourceResolutionDBService.readValue(bpName, bpVersion, artifactName, resolutionKey, name))
    }
}