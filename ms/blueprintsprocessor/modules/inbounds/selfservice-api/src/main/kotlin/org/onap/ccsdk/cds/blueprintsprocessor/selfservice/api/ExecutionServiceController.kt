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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.utils.determineHttpStatusCode
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/execution-service")
@Api(value = "/api/v1/execution-service",
        description = "Interaction with CBA.")
open class ExecutionServiceController {
    val log = logger(ExecutionServiceController::class)


    val samplePayload = "{\n" +
            "    \"resource-assignment-request\": {\n" +
            "      \"artifact-name\": [\"hostname\"],\n" +
            "      \"store-result\": true,\n" +
            "      \"resource-assignment-properties\" : {\n" +
            "        \"hostname\": \"demo123\"\n" +
            "      }\n" +
            "    }\n" +
            "  }"

    @Autowired
    lateinit var executionServiceHandler: ExecutionServiceHandler

    @RequestMapping(path = ["/health-check"],
            method = [RequestMethod.GET],
            produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation(value = "Health Check", hidden = true)
    suspend fun executionServiceControllerHealthCheck(): JsonNode = withContext(Dispatchers.IO) {
        val res = async(start= CoroutineStart.LAZY) {
            log.info("Health check success...")
            "Sucess".asJsonPrimitive()
        }
        res.start()
        res.await()
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
            : ResponseEntity<ExecutionServiceOutput> = withContext(Dispatchers.IO) {
        val processResult = executionServiceHandler.doProcess(executionServiceInput)
        ResponseEntity(processResult, determineHttpStatusCode(processResult.status.code))
    }
}
