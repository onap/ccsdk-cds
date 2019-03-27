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

import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.ResourceAssignmentProcessor
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory

open class ScriptResourceAssignmentProcessor : ResourceAssignmentProcessor() {

    private val log = LoggerFactory.getLogger(ScriptResourceAssignmentProcessor::class.java)!!

    override fun getName(): String {
        return "ScriptResourceAssignmentProcessor"
    }

    override fun process(executionRequest: ResourceAssignment) {
        log.info("Processing input")
    }

    override fun recover(runtimeException: RuntimeException, executionRequest: ResourceAssignment) {
        log.info("Recovering input")
    }
}