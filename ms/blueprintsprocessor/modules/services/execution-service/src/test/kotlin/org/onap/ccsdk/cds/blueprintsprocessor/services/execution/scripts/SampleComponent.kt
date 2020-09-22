/*-
 * ============LICENSE_START=======================================================
 * ONAP - CDS
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentFunctionScriptingService
import org.slf4j.LoggerFactory

open class SampleComponent : AbstractComponentFunction() {

    val log = LoggerFactory.getLogger(SampleComponent::class.java)!!

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
    }
}

open class SampleRestconfComponent(private var componentFunctionScriptingService: ComponentFunctionScriptingService) :
    AbstractComponentFunction() {

    val log = LoggerFactory.getLogger(SampleScriptComponent::class.java)!!

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        var scriptComponent: AbstractScriptComponentFunction
        scriptComponent = componentFunctionScriptingService
            .scriptInstance<AbstractScriptComponentFunction>(
                this,
                "internal",
                "org.onap.ccsdk.cds.blueprintsprocessor.services" +
                    ".execution.scripts.SampleTest",
                mutableListOf()
            )
        scriptComponent.executeScript(executionServiceInput)
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
    }
}

open class SampleScriptComponent : AbstractScriptComponentFunction() {

    val log = LoggerFactory.getLogger(SampleScriptComponent::class.java)!!

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
    }
}
