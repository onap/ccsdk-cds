/*
 *  Copyright © 2018 IBM.
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

package org.onap.ccsdk.apps.controllerblueprints.resource.dict.utils

import org.apache.commons.collections.MapUtils
import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.apps.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDictionaryConstants
import org.slf4j.LoggerFactory


object ResourceDictionaryUtils {
    private val log = LoggerFactory.getLogger(ResourceDictionaryUtils::class.java)

    @JvmStatic
    fun populateSourceMapping(resourceAssignment: ResourceAssignment,
                              resourceDefinition: ResourceDefinition) {

        if (StringUtils.isBlank(resourceAssignment.dictionarySource)) {

            if (MapUtils.isNotEmpty(resourceDefinition.sources)) {
                val source = findFirstSource(resourceDefinition.sources)

                // Populate and Assign First Source
                if (StringUtils.isNotBlank(source)) {
                    // Set Dictionary Source
                    resourceAssignment.dictionarySource = source
                } else {
                    resourceAssignment.dictionarySource = ResourceDictionaryConstants.SOURCE_INPUT
                }
                log.info("auto map resourceAssignment : {}", resourceAssignment)
            }else {
                resourceAssignment.dictionarySource = ResourceDictionaryConstants.SOURCE_INPUT
            }
        }
    }

    @JvmStatic
    fun findFirstSource(sources: Map<String, NodeTemplate>): String? {
        var source: String? = null
        if (MapUtils.isNotEmpty(sources)) {
            source = sources.keys.stream().findFirst().get()
        }
        return source
    }
}