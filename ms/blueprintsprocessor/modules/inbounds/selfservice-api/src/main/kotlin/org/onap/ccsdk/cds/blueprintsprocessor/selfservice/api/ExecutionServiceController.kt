/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import kotlinx.coroutines.delay
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ACTION_MODE_ASYNC
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Status
import org.onap.ccsdk.cds.blueprintsprocessor.core.monoMdc
import org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.utils.determineHttpStatusCode
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/execution-service")
@Api(value = "/api/v1/execution-service",
        description = "Interaction with CBA.")
open class ExecutionServiceController {
    val log = logger(ExecutionServiceController::class)

    @Autowired
    lateinit var executionServiceHandler: ExecutionServiceHandler

    @RequestMapping(path = ["/health-check"],
            method = [RequestMethod.GET],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation(value = "Health Check", hidden = true)
    fun executionServiceControllerHealthCheck() = monoMdc {
        log.info("Health check success...")
        "Success".asJsonPrimitive()
    }

    @RequestMapping(path = ["/process"], method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Execute a CBA workflow (action)",
            notes = "Execute the appropriate CBA's action based on the ExecutionServiceInput object passed as input.",
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = ExecutionServiceOutput::class)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    suspend fun process(@ApiParam(value = "ExecutionServiceInput payload.", required = true)
                @RequestBody executionServiceInput: ExecutionServiceInput)
            : ResponseEntity<ExecutionServiceOutput> {

        if (executionServiceInput.actionIdentifiers.mode == ACTION_MODE_ASYNC) {
            throw IllegalStateException("Can't process async request through the REST endpoint. Use gRPC for async processing.")
        }
        val processResult = executionServiceHandler.doProcess(executionServiceInput)
        return ResponseEntity(processResult, determineHttpStatusCode(processResult.status.code))
    }

    @RequestMapping(path = ["/process2/{waitTime}"], method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Execute a CBA workflow (action)",
            notes = "Execute the appropriate CBA's action based on the ExecutionServiceInput object passed as input.",
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = ExecutionServiceOutput::class)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    suspend fun process2(@ApiParam(value = "ExecutionServiceInput payload.", required = true)
                        @PathVariable(value = "waitTime") waitTime: Long,
                        @RequestBody executionServiceInput: ExecutionServiceInput)
            : ResponseEntity<String> {

        if (executionServiceInput.actionIdentifiers.mode == ACTION_MODE_ASYNC) {
            throw IllegalStateException("Can't process async request through the REST endpoint. Use gRPC for async processing.")
        }

        delay(waitTime)
        return ResponseEntity("Process", HttpStatus.OK)
    }
}
