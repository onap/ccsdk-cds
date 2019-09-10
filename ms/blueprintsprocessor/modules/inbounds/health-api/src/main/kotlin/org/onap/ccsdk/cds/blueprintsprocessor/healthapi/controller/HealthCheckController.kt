package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthApiResponse
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.HealthCheckService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/health-api")
@Api(value = "/api/v1/health/",
        description = "gather all HealthCheckResponses for HealthChecks known to the runtime")
open class HealthCheckController {

    @Autowired
    lateinit var healthApiService: HealthCheckService

    @RequestMapping(path = ["/health"],
            method = [RequestMethod.GET],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation(value = "Health Check", hidden = true)
    fun getResourceDictionaryByName(): ResponseEntity<HealthApiResponse> {
        return ResponseEntity.ok().body(healthApiService.execute())


    }
}
