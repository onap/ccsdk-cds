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

package org.onap.ccsdk.cds.controllerblueprints.resource.dict.factory

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.format
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceSourceMapping

/**
 * ResourceSourceMappingFactory.
 *
 * @author Brinda Santh
 */
object ResourceSourceMappingFactory {

    private val resourceSourceMappings: MutableMap<String, String> = hashMapOf()

    fun registerSourceMapping(sourceInstance: String, nodeTypeName: String) {
        resourceSourceMappings[sourceInstance] = nodeTypeName
    }

    fun getRegisterSourceMapping(sourceInstance: String): String {
        return resourceSourceMappings[sourceInstance]
            ?: throw BlueprintException(format("failed to get source({}) mapping", sourceInstance))
    }

    fun getRegisterSourceMapping(): ResourceSourceMapping {
        val resourceSourceMapping = ResourceSourceMapping()
        resourceSourceMapping.resourceSourceMappings = resourceSourceMappings
        return resourceSourceMapping
    }
}
