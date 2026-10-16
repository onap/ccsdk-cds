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

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.MetricsInfo
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.CombinedMetricsService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * Exposes API for checking Metrics of BluePrint processor and CDSListener.
 *
 * @author Shaaban Ebrahim
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/combinedMetrics")
@Tag(
    name = "/api/v1/combinedMetrics",
    description = "gather all Metrics info from BluePrint and CDSListener"
)
open class CombinedMetrics(private val combinedMetricsService: CombinedMetricsService) {

    @RequestMapping(
        path = [""],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    @Operation(summary = " Metrics Check", hidden = true)
    suspend fun getMetricsHealthCheckResponse(): ResponseEntity<MetricsInfo?> =
        withContext(Dispatchers.IO) {
            ResponseEntity.ok().body(combinedMetricsService.metricsInfo)
        }
}
