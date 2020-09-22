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

package org.onap.ccsdk.cds.controllerblueprints.resource.dict.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import org.apache.commons.collections.MapUtils
import org.apache.commons.lang3.StringUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintFileUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDictionaryConstants
import org.slf4j.LoggerFactory
import java.io.File

object ResourceDictionaryUtils {

    private val log = LoggerFactory.getLogger(ResourceDictionaryUtils::class.java)

    @JvmStatic
    fun populateSourceMapping(
        resourceAssignment: ResourceAssignment,
        resourceDefinition: ResourceDefinition
    ) {

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
            } else {
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

    @JvmStatic
    fun assignInputs(data: JsonNode, context: MutableMap<String, Any>) {
        log.trace("assignInputs from input JSON ({})", data.toString())
        data.fields().forEach { field ->
            val valueNode: JsonNode = data.at("/".plus(field.key)) ?: NullNode.getInstance()

            val path = BluePrintConstants.PATH_INPUTS.plus(BluePrintConstants.PATH_DIVIDER).plus(field.key)
            log.trace("setting path ({}), values ({})", path, valueNode)
            context[path] = valueNode
        }
    }

    fun getResourceAssignmentFromFile(filePath: String): List<ResourceAssignment> {
        return JacksonUtils.getListFromFile(filePath, ResourceAssignment::class.java)
            ?: throw BluePrintProcessorException("couldn't get ResourceAssignment definitions for the file($filePath)")
    }

    fun writeResourceDefinitionTypes(basePath: String, resourceDefinitions: List<ResourceDefinition>) {
        val resourceDefinitionMap = resourceDefinitions.map { it.name to it }.toMap()
        writeResourceDefinitionTypes(basePath, resourceDefinitionMap)
    }

    fun writeResourceDefinitionTypes(basePath: String, resourceDefinitionMap: Map<String, ResourceDefinition>) {
        val typePath = basePath.plus(File.separator).plus(BluePrintConstants.TOSCA_DEFINITIONS_DIR)
            .plus(File.separator).plus("${ResourceDictionaryConstants.PATH_RESOURCE_DEFINITION_TYPE}.json")
        val resourceDefinitionContent = JacksonUtils.getJson(resourceDefinitionMap.toSortedMap(), true)
        BluePrintFileUtils.writeDefinitionFile(typePath, resourceDefinitionContent)
    }
}
