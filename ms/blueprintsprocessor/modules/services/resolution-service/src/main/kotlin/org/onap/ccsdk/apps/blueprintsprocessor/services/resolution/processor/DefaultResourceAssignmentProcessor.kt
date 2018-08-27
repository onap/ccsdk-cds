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

package org.onap.ccsdk.apps.blueprintsprocessor.services.resolution.processor

import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignmentProcessor
import org.springframework.stereotype.Service

@Service("resource-assignment-processor-default")
open class DefaultResourceAssignmentProcessor : ResourceAssignmentProcessor {
    override fun errorHandle(resourceAssignment: ResourceAssignment, context: MutableMap<String, Any>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun process(resourceAssignment: ResourceAssignment, context: MutableMap<String, Any>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun reTrigger(resourceAssignment: ResourceAssignment, context: MutableMap<String, Any>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun validate(resourceAssignment: ResourceAssignment, context: MutableMap<String, Any>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}