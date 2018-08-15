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

package org.onap.ccsdk.apps.blueprintsprocessor.services.execution


import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.Status
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.springframework.stereotype.Service
import com.fasterxml.jackson.databind.node.ObjectNode

/**
 * ExecutionService
 * @author Brinda Santh
 * 8/14/2018
 */
@Service
class ExecutionService {

    fun process(executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput {
        val executionServiceOutput = ExecutionServiceOutput()
        executionServiceOutput.actionIdentifiers = executionServiceInput.actionIdentifiers
        executionServiceOutput.commonHeader = executionServiceInput.commonHeader
        executionServiceOutput.payload = JacksonUtils.jsonNode("{}") as ObjectNode
        val status = Status()
        status.code = 200
        status.message = "Success"
        executionServiceOutput.status = status
        return executionServiceOutput
    }
}