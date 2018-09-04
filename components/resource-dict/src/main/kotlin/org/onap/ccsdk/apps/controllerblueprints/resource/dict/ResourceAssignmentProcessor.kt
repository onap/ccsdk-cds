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

package org.onap.ccsdk.apps.controllerblueprints.resource.dict

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException

interface ResourceAssignmentProcessor {

    @Throws(BluePrintProcessorException::class)
    fun validate(resourceAssignment: ResourceAssignment, context : MutableMap<String, Any>)

    @Throws(BluePrintProcessorException::class)
    fun process(resourceAssignment: ResourceAssignment, context : MutableMap<String, Any>)

    @Throws(BluePrintProcessorException::class)
    fun errorHandle(resourceAssignment: ResourceAssignment, context : MutableMap<String, Any>)

    @Throws(BluePrintProcessorException::class)
    fun reTrigger(resourceAssignment: ResourceAssignment, context : MutableMap<String, Any>)
}