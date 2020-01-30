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

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ACTION_MODE_ASYNC
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.utils.determineHttpStatusCode
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Phaser
import javax.annotation.PreDestroy

@RestController
@RequestMapping("/api/v1/execution-service")
@Api(value = "/api/v1/execution-service",
    description = "Interaction with CBA.")
open class ExecutionServiceController {
    private val scheduler = Schedulers.newElastic("bpthread")
    private val log = logger(ExecutionServiceController::class)

    private val ph = Phaser(1)

    @Autowired
    lateinit var executionServiceHandler: ExecutionServiceHandler

    @RequestMapping(path = ["/health-check"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation(value = "Health Check", hidden = true)
    fun executionServiceControllerHealthCheck(): JsonNode = runBlocking {
        "Success".asJsonPrimitive()
    }

    @PostMapping(path = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "Upload a CBA",
        notes = "Upload the CBA package. This will also run validation on the CBA.",
        produces = MediaType.APPLICATION_JSON_VALUE)
    fun upload(@ApiParam(value = "The ZIP file containing the overall CBA package.", required = true)
               @RequestPart("file") filePart: FilePart): JsonNode = runBlocking {
        val uploadId = executionServiceHandler.upload(filePart)
        """{"upload-id" : "$uploadId"}""".asJsonType()
    }

    @DeleteMapping("/name/{name}/version/{version}")
    @ApiOperation(value = "Delete a CBA",
        notes = "Delete the CBA package identified by its name and version.",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    fun deleteBlueprint(@ApiParam(value = "Name of the CBA.", required = true)
                        @PathVariable(value = "name") name: String,
                        @ApiParam(value = "Version of the CBA.", required = true)
                        @PathVariable(value = "version") version: String) = runBlocking {
        executionServiceHandler.remove(name, version)
    }

    @RequestMapping(path = ["/process"], method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Execute a CBA workflow (action)",
        notes = "Execute the appropriate CBA's action based on the ExecutionServiceInput object passed as input.",
        produces = MediaType.APPLICATION_JSON_VALUE,
        response = ExecutionServiceOutput::class)
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    fun process(@ApiParam(value = "ExecutionServiceInput payload.", required = true)
                @RequestBody executionServiceInput: ExecutionServiceInput): Mono<ResponseEntity<ExecutionServiceOutput>> {
        return Mono.fromSupplier { this.bpProcessBlockingWrapper(executionServiceInput) }
            .subscribeOn(scheduler)
    }

    private fun bpProcessBlockingWrapper(executionServiceInput: ExecutionServiceInput): ResponseEntity<ExecutionServiceOutput> {
        return runBlocking {
            if (executionServiceInput.actionIdentifiers.mode == ACTION_MODE_ASYNC) {
                throw IllegalStateException("Can't process async request through the REST endpoint. Use gRPC for async processing.")
            }

            ph.register()
            val processResult = executionServiceHandler.doProcess(executionServiceInput)
            ph.arriveAndDeregister()
            ResponseEntity(processResult, determineHttpStatusCode(processResult.status.code))
        }
    }

    @PreDestroy
    fun preDestroy() {
        val name = "ExecutionServiceController"
        log.info("Starting to shutdown $name waiting for in-flight requests to finish ...")
        ph.arriveAndAwaitAdvance()
        log.info("Done waiting in $name")
    }
}
