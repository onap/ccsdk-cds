/*
 *  Copyright © 2018 IBM.
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

package org.onap.ccsdk.apps.blueprintsprocessor.services.resolution.processor

import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignmentProcessor
import org.springframework.stereotype.Service

/**
 * SdncResourceAssignmentProcessor
 *
 * @author Brinda Santh
 */
@Service("resource-assignment-processor-db")
open class SdncResourceAssignmentProcessor : ResourceAssignmentProcessor {

    private val log = EELFManager.getInstance().getLogger(SdncResourceAssignmentProcessor::class.java)

    override fun validate(resourceAssignment: ResourceAssignment, context: MutableMap<String, Any>) {
        log.info("Validation Resource Assignments")
    }

    override fun process(resourceAssignment: ResourceAssignment, context: MutableMap<String, Any>) {
        log.info("Processing Resource Assignments")
    }

    override fun errorHandle(resourceAssignment: ResourceAssignment, context: MutableMap<String, Any>) {
        log.info("ErrorHandle Resource Assignments")
    }

    override fun reTrigger(resourceAssignment: ResourceAssignment, context: MutableMap<String, Any>) {
        log.info("Re Trigger Resource Assignments")
    }

}