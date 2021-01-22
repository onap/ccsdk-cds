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

package org.onap.ccsdk.cds.controllerblueprints.core.interfaces

import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService

interface BlueprintWorkflowExecutionService<Input, Output> {

    suspend fun executeBlueprintWorkflow(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        executionServiceInput: Input,
        properties: MutableMap<String, Any> = hashMapOf()
    ): Output
}
