/*
 *  Copyright © 2019 IBM.
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

package cba.resource.audit.processor

import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.ResourceAssignmentProcessor
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment

class PortSpeedRAProcessor : ResourceAssignmentProcessor() {
    val log = logger(PortSpeedRAProcessor::class)
    override fun getName(): String {
        return "PortSpeedRAProcessor"
    }

    override suspend fun processNB(executionRequest: ResourceAssignment) {
        log.info("Executing Resource PortSpeedRAProcessor")
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ResourceAssignment) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
