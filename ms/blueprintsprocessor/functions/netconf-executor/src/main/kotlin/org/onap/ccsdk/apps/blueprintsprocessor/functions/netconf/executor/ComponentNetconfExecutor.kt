/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Modifications Copyright © 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor

import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.functions.python.executor.BlueprintJythonService
import org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.ResourceResolutionService
import org.onap.ccsdk.apps.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component("component-netconf-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentNetconfExecutor(private val blueprintJythonService: BlueprintJythonService,
                                    private var resourceResolutionService: ResourceResolutionService)
    : AbstractComponentFunction() {

    private val log = LoggerFactory.getLogger(ComponentNetconfExecutor::class.java)

    lateinit var scriptComponent: NetconfComponentFunction

    override fun process(executionRequest: ExecutionServiceInput) {

        scriptComponent = blueprintJythonService.jythonComponentInstance(this) as NetconfComponentFunction
        checkNotNull(scriptComponent) { "failed to get netconf script component" }

        // FIXME("Populate the reference in Abstract Script Instance Injection map")
        scriptComponent.bluePrintRuntimeService = bluePrintRuntimeService
        scriptComponent.resourceResolutionService = resourceResolutionService

        scriptComponent.processId = processId
        scriptComponent.workflowName = workflowName
        scriptComponent.stepName = stepName
        scriptComponent.interfaceName = interfaceName
        scriptComponent.operationName = operationName
        scriptComponent.nodeTemplateName = nodeTemplateName
        scriptComponent.operationInputs = operationInputs

        scriptComponent.process(executionServiceInput)
    }

    override fun recover(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        scriptComponent.recover(runtimeException, executionRequest)
    }


}