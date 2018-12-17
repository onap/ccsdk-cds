/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.blueprintsprocessor.selfservice.api

import io.swagger.annotations.ApiOperation
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/execution-service")
class ExecutionServiceController {

    @Autowired
    lateinit var executionServiceHandler: ExecutionServiceHandler


    @RequestMapping(path = arrayOf("/ping"), method = arrayOf(RequestMethod.GET), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    @ResponseBody
    fun ping(): Mono<String> {
        return Mono.just("Success")
    }

    @RequestMapping(path = arrayOf("/process"), method = arrayOf(RequestMethod.POST), produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    @ApiOperation(value = "Resolve Resource Mappings", notes = "Takes the blueprint information and process as per the payload")
    @ResponseBody
    fun process(@RequestBody executionServiceInput: ExecutionServiceInput): Mono<ExecutionServiceOutput> {
        val executionServiceOutput = executionServiceHandler.process(executionServiceInput)
        return Mono.just(executionServiceOutput)
    }
}
