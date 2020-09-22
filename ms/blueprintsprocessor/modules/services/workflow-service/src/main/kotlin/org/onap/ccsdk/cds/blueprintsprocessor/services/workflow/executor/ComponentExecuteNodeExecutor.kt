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

package org.onap.ccsdk.cds.blueprintsprocessor.services.workflow.executor

import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.services.workflow.BlueprintSvcLogicContext
import org.onap.ccsdk.cds.blueprintsprocessor.services.workflow.NodeTemplateExecutionService
import org.onap.ccsdk.sli.core.sli.SvcLogicContext
import org.onap.ccsdk.sli.core.sli.SvcLogicException
import org.onap.ccsdk.sli.core.sli.SvcLogicNode
import org.onap.ccsdk.sli.core.sli.provider.base.ExecuteNodeExecutor
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicExpressionResolver
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicServiceBase
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
open class ComponentExecuteNodeExecutor(private val nodeTemplateExecutionService: NodeTemplateExecutionService) :
    ExecuteNodeExecutor() {

    private val log = LoggerFactory.getLogger(ComponentExecuteNodeExecutor::class.java)

    @Throws(SvcLogicException::class)
    override fun execute(svc: SvcLogicServiceBase, node: SvcLogicNode, svcLogicContext: SvcLogicContext):
        SvcLogicNode = runBlocking {

            var outValue: String

            val ctx = svcLogicContext as BlueprintSvcLogicContext

            val nodeTemplateName = SvcLogicExpressionResolver.evaluate(node.getAttribute("plugin"), node, ctx)

            val executionInput = ctx.getRequest() as ExecutionServiceInput

            try { // Get the Request from the Context and Set to the Function Input and Invoke the function
                val executionOutput = nodeTemplateExecutionService.executeNodeTemplate(
                    ctx.getBluePrintService(),
                    nodeTemplateName, executionInput
                )

                ctx.setResponse(executionOutput)

                outValue = executionOutput.status.message
                ctx.status = executionOutput.status.message
            } catch (e: Exception) {
                log.error("Could not execute plugin($nodeTemplateName) : ", e)
                outValue = "failure"
                ctx.status = "failure"
            }

            getNextNode(node, outValue)
        }
}
