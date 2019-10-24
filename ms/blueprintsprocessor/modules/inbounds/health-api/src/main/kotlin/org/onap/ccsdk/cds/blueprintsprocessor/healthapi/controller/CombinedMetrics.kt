package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.MetricsInfo
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.MetricsResponse
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.CombinedMetricsService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/combinedMetrics")
@Api(value = "/api/v1/health",
        description = "gather all Metrics info from BluePrint and CDSListener")
open class CombinedMetrics (private val combinedMetricsService: CombinedMetricsService) {


    @RequestMapping(path = [""],
            method = [RequestMethod.GET],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation(value = "get MetricsCheck", hidden = true)
    fun getMetricsHealthCheckResponse(): ResponseEntity<MetricsInfo?> {
        return ResponseEntity.ok().body(combinedMetricsService.getMetricsInfo())

    }

}
