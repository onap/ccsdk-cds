/*
 *  Copyright © 2018 IBM.
 *  Modifications Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolutionprocessor

import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.springframework.stereotype.Service

/**
 * SimpleRestResourceAssignmentProcessor
 *
 * @author Brinda Santh
 */
@Service("resource-assignment-processor-mdsal")
open class SimpleRestResourceAssignmentProcessor : ResourceAssignmentProcessor() {

    override fun getName(): String {
        return "resource-assignment-processor-mdsal"
    }

    override fun process(executionRequest: ResourceAssignment) {
    }

    override fun recover(runtimeException: RuntimeException, executionRequest: ResourceAssignment) {
    }
}