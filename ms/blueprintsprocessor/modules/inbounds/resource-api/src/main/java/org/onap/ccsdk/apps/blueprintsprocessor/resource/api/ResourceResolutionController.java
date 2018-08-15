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

package org.onap.ccsdk.apps.blueprintsprocessor.resource.api;

import io.swagger.annotations.ApiOperation;
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ResourceResolutionInput;
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ResourceResolutionOutput;
import org.onap.ccsdk.apps.blueprintsprocessor.services.resolution.ResourceResolutionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * ResourceResolutionController
 *
 * @author Brinda Santh Date : 8/13/2018
 */

@RestController
@RequestMapping("/api/v1/resource")
public class ResourceResolutionController {

    private ResourceResolutionService resourceResolutionService;

    public ResourceResolutionController(ResourceResolutionService resourceResolutionService) {
        this.resourceResolutionService = resourceResolutionService;
    }

    @RequestMapping(path = "/ping", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Mono<String> ping() {
        return Mono.just("Success");
    }

    @RequestMapping(path = "/resolve-mapping", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Resolve Resource Mappings",
            notes = "Also returns a link to retrieve all students with rel - all-students")
    public @ResponseBody
    Mono<ResourceResolutionOutput> resolveResource(@RequestBody ResourceResolutionInput resourceResolutionInput) {
        return Mono.just(resourceResolutionService.resolveResource(resourceResolutionInput));
    }
}
