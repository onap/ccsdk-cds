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

package org.onap.ccsdk.apps.blueprintsprocessor.core.factory

import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignmentProcessor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Service

@Service
class ResourceAssignmentProcessorFactory : ApplicationContextAware {

    private val log = EELFManager.getInstance().getLogger(ResourceAssignmentProcessorFactory::class.java)

    var resourceAssignmentProcessors: MutableMap<String, ResourceAssignmentProcessor> = hashMapOf()

    fun getInstance(instanceName: String): ResourceAssignmentProcessor? {
        log.trace("looking for Resource Assignment Processor : {}", instanceName)
        return resourceAssignmentProcessors.get(instanceName)
    }

    fun injectInstance(instanceName: String, resourceAssignmentProcessor: ResourceAssignmentProcessor) {
        this.resourceAssignmentProcessors[instanceName] = resourceAssignmentProcessor
    }

    override fun setApplicationContext(context: ApplicationContext) {
        resourceAssignmentProcessors = context.getBeansOfType(ResourceAssignmentProcessor::class.java)
        log.info("Injected Resource Assignment Processor : {}", resourceAssignmentProcessors)
    }
}