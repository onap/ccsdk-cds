/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *  Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.core.factory

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

/**
 * ComponentNode
 *
 * @author Brinda Santh
 */
interface ComponentNode {

    @Throws(BlueprintProcessorException::class)
    fun validate(context: MutableMap<String, Any>, componentContext: MutableMap<String, Any?>)

    @Throws(BlueprintProcessorException::class)
    fun process(context: MutableMap<String, Any>, componentContext: MutableMap<String, Any?>)

    @Throws(BlueprintProcessorException::class)
    fun errorHandle(context: MutableMap<String, Any>, componentContext: MutableMap<String, Any?>)

    @Throws(BlueprintProcessorException::class)
    fun reTrigger(context: MutableMap<String, Any>, componentContext: MutableMap<String, Any?>)
}

/**
 * ComponentNodeFactory
 *
 * @author Brinda Santh
 */
open class ComponentNodeFactory : ApplicationContextAware {

    private val log = LoggerFactory.getLogger(ComponentNodeFactory::class.java)

    var componentNodes: MutableMap<String, ComponentNode> = hashMapOf()

    fun getInstance(instanceName: String): ComponentNode? {
        log.trace("looking for Component Nodes : {}", instanceName)
        return componentNodes.get(instanceName)
    }

    fun injectInstance(instanceName: String, componentNode: ComponentNode) {
        this.componentNodes[instanceName] = componentNode
    }

    override fun setApplicationContext(context: ApplicationContext) {
        componentNodes = context.getBeansOfType(ComponentNode::class.java)
        log.info("Injected Component Nodes : {}", componentNodes)
    }
}
