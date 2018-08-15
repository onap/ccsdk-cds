/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.apps.blueprintsprocessor.selfservice.api;

import io.swagger.annotations.ApiOperation;
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput;
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceOutput;
import org.onap.ccsdk.apps.blueprintsprocessor.services.execution.ExecutionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * ExecutionServiceController
 *
 * @author Brinda Santh 8/14/2018
 */
@RestController
@RequestMapping("/api/v1/execution-service")
public class ExecutionServiceController {

    private ExecutionService executionService;

    public ExecutionServiceController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @RequestMapping(path = "/ping", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Mono<String> ping() {
        return Mono.just("Success");
    }

    @RequestMapping(path = "/process", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Resolve Resource Mappings",
            notes = "Takes the blueprint information and process as per the payload")
    public @ResponseBody
    Mono<ExecutionServiceOutput> process(@RequestBody ExecutionServiceInput executionServiceInput) {
        return Mono.just(executionService.process(executionServiceInput));
    }
}
