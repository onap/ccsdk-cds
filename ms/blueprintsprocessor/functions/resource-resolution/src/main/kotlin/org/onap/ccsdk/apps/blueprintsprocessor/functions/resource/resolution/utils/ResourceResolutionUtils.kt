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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.utils

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils

class ResourceResolutionUtils {
    companion object {

        fun <T> transformResourceSource(properties: MutableMap<String, JsonNode>, classType: Class<T>): T {
            val content = JacksonUtils.getJson(properties)
            return JacksonUtils.readValue(content, classType)
                    ?: throw BluePrintProcessorException("failed to transform content($content) to type($classType)")
        }
    }
}