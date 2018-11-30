/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.blueprintsprocessor.services.workflow

import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.sli.core.sli.*
import org.onap.ccsdk.sli.core.sli.provider.*
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct

interface BlueprintSvcLogicService : SvcLogicService {

    fun registerDefaultExecutors()

    fun registerExecutors(name: String, svcLogicNodeExecutor: SvcLogicNodeExecutor)

    fun unRegisterExecutors(name: String)

    fun execute(graph: SvcLogicGraph, bluePrintRuntimeService: BluePrintRuntimeService<*>, input: Any): Any

    @Deprecated("Populate Graph Dynamically from Blueprints, No need to get from Database Store ")
    override fun getStore(): SvcLogicStore {
        TODO("not implemented")
    }

    @Deprecated("Not used in Micro service Implementation")
    override fun hasGraph(module: String, rpc: String, version: String?, mode: String): Boolean {
        TODO("not implemented")
    }

    @Deprecated("Not used in Micro service Implementation")
    override fun execute(p0: String?, p1: String?, p2: String?, p3: String?, p4: Properties?): Properties {
        TODO("not implemented")
    }

    @Deprecated("Not used in Micro service Implementation")
    override fun execute(p0: String?, p1: String?, p2: String?, p3: String?, p4: Properties?, p5: DOMDataBroker?): Properties {
        TODO("not implemented")
    }
}


@Service
class DefaultBlueprintSvcLogicService : BlueprintSvcLogicService {

    private val log = LoggerFactory.getLogger(DefaultBlueprintSvcLogicService::class.java)

    private val nodeExecutors: MutableMap<String, SvcLogicNodeExecutor> = hashMapOf()

    @Autowired
    private lateinit var context: ApplicationContext

    @PostConstruct
    override fun registerDefaultExecutors() {

        val executeNodeExecutor = context.getBean(ExecuteNodeExecutor::class.java)
        registerExecutors("execute", executeNodeExecutor)
        registerExecutors("block", BlockNodeExecutor())
        registerExecutors("return", ReturnNodeExecutor())
        registerExecutors("break", BreakNodeExecutor())
        registerExecutors("exit", ExitNodeExecutor())
    }

    override fun registerExecutors(name: String, svcLogicNodeExecutor: SvcLogicNodeExecutor) {
        log.info("Registering executors($name) with type(${svcLogicNodeExecutor.javaClass}")
        nodeExecutors.put(name, svcLogicNodeExecutor)
    }

    override fun unRegisterExecutors(name: String) {
        if (nodeExecutors.containsKey(name)) {
            log.info("UnRegistering executors($name)")
            nodeExecutors.remove(name)
        }
    }

    override fun execute(graph: SvcLogicGraph, bluePrintRuntimeService: BluePrintRuntimeService<*>, input: Any): Any {
        //Initialise BlueprintSvcLogic Context with Blueprint Runtime Service and Input Request
        val blueprintSvcLogicContext = BlueprintSvcLogicContext()
        blueprintSvcLogicContext.setBluePrintRuntimeService(bluePrintRuntimeService)
        blueprintSvcLogicContext.setRequest(input)
        // Execute the Graph
        execute(graph, blueprintSvcLogicContext)
        // Get the Response
        return blueprintSvcLogicContext.getResponse()
    }

    override fun executeNode(node: SvcLogicNode?, ctx: SvcLogicContext): SvcLogicNode? {
        if (node == null) {
            return null
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Executing node {}", node.getNodeId())
            }

            val executor = this.nodeExecutors[node.getNodeType()]

            if (executor != null) {
                log.debug("Executing node executor for node type {} - {}", node.getNodeType(), executor.javaClass.name)
                return executor.execute(this, node, ctx)
            } else {
                throw SvcLogicException("Attempted to execute a node of type " + node.getNodeType() + ", but no executor was registered for this type")
            }
        }
    }

    override fun execute(graph: SvcLogicGraph, svcLogicContext: SvcLogicContext): SvcLogicContext {
        MDC.put("currentGraph", graph.toString())

        var curNode: SvcLogicNode? = graph.getRootNode()
        log.info("About to execute graph {}", graph.toString())

        try {
            while (curNode != null) {
                MDC.put("nodeId", curNode.nodeId.toString() + " (" + curNode.nodeType + ")")
                log.info("About to execute node # {} ({})", curNode.nodeId, curNode.nodeType)
                val nextNode = this.executeNode(curNode, svcLogicContext)
                curNode = nextNode
            }
        } catch (var5: ExitNodeException) {
            log.debug("SvcLogicServiceImpl caught ExitNodeException")
        }

        MDC.remove("nodeId")
        MDC.remove("currentGraph")
        return svcLogicContext
    }
}