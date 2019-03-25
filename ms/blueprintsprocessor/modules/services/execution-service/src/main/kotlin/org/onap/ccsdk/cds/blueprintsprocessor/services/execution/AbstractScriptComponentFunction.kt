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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException

abstract class AbstractScriptComponentFunction : AbstractComponentFunction() {

    companion object {
        const val DYNAMIC_PROPERTIES = "dynamic-properties"
    }

    /**
     * Store Dynamic Script Dependency Instances, Objects present inside won't be persisted or state maintained.
     */
    var functionDependencyInstances: MutableMap<String, Any> = hashMapOf()

    /**
     * This will be called from the scripts to serve instance from runtime to scripts.
     */
    open fun <T> functionDependencyInstanceAsType(name: String): T {
        return functionDependencyInstances[name] as? T
                ?: throw BluePrintProcessorException("couldn't get script property instance ($name)")
    }

    fun checkDynamicProperties(key: String): Boolean {
        return operationInputs[DYNAMIC_PROPERTIES]?.has(key) ?: false
    }

    fun getDynamicProperties(key: String): JsonNode {
        return operationInputs[DYNAMIC_PROPERTIES]!!.get(key)
    }


}