/*
 * Copyright © 2019-2020 Orange.
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

package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ApplicationHealth
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.CombinedHealthService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * Exposes API for checking health for other services .
 *
 * @author Shaaban Ebrahim
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/combinedHealth")
@Api(value = "/api/v1/combinedHealth",
        description = "gather all HealthCheckResponses for HealthChecks known to the runtime")
open class CombinedHealth(private val combinedHealthService: CombinedHealthService) {

    @RequestMapping(path = [""],
            method = [RequestMethod.GET],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation(value = "Health Check", hidden = true)
    fun getSystemHealthCheckResponse(): ResponseEntity<List<ApplicationHealth?>> {
        return ResponseEntity.ok().body(combinedHealthService.getCombinedHealthCheck())

    }
}

