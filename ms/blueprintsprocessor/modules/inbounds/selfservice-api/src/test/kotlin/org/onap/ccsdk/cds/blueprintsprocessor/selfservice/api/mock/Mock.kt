/*
 * Copyright (C) 2019 Bell Canada.
 * Modifications Copyright © 2019 IBM.
 *
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
 */
package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.mock

import io.mockk.mockk
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.ResourceAssignmentProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class MockComponentConfiguration {

    @Bean(name = ["component-resource-assignment", "component-netconf-executor", "component-jython-executor"])
    open fun createComponentFunction(): AbstractComponentFunction {
        return MockComponentFunction()
    }
}

class MockComponentFunction : AbstractComponentFunction() {

    private val log = LoggerFactory.getLogger(MockComponentFunction::class.java)

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        log.info("Processing component : $operationInputs")

        bluePrintRuntimeService.setNodeTemplateAttributeValue(
            nodeTemplateName,
            "assignment-params", "params".asJsonPrimitive()
        )
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("Recovering component..")
    }
}

open class MockResourceSource {
    @Bean(
        name = [
            "rr-processor-source-input",
            "rr-processor-source-default",
            "rr-processor-source-db",
            "rr-processor-source-rest"
        ]
    )
    open fun sourceInstance(): ResourceAssignmentProcessor {
        return mockk<ResourceAssignmentProcessor>()
    }
}
