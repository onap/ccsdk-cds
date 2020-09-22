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

package org.onap.ccsdk.cds.controllerblueprints.core.utils

import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.TopologyTemplate

/**
 *
 *
 * @author Brinda Santh
 */
object ServiceTemplateUtils {

    suspend fun getServiceTemplate(fileName: String): ServiceTemplate {
        val content: String = JacksonReactorUtils.getContent(fileName)
        return getServiceTemplateFromContent(content)
    }

    fun getServiceTemplateFromContent(content: String): ServiceTemplate {
        return JacksonUtils.readValue(content)
    }

    fun merge(
        parentServiceTemplate: ServiceTemplate,
        toMerge: ServiceTemplate,
        removeImports: Boolean? = true
    ): ServiceTemplate {
        if (removeImports!!) {
            parentServiceTemplate.imports = null
            toMerge.imports = null
        }

        toMerge.metadata?.let {
            parentServiceTemplate.metadata = parentServiceTemplate.metadata ?: hashMapOf()
            parentServiceTemplate.metadata?.putAll(toMerge.metadata as MutableMap)
        }

        toMerge.dslDefinitions?.let {
            parentServiceTemplate.dslDefinitions = parentServiceTemplate.dslDefinitions ?: hashMapOf()
            parentServiceTemplate.dslDefinitions?.putAll(toMerge.dslDefinitions as MutableMap)
        }

        toMerge.dataTypes?.let {
            parentServiceTemplate.dataTypes = parentServiceTemplate.dataTypes ?: hashMapOf()
            parentServiceTemplate.dataTypes?.putAll(toMerge.dataTypes as MutableMap)
        }

        toMerge.nodeTypes?.let {
            parentServiceTemplate.nodeTypes = parentServiceTemplate.nodeTypes ?: hashMapOf()
            parentServiceTemplate.nodeTypes?.putAll(toMerge.nodeTypes as MutableMap)
        }

        toMerge.relationshipTypes?.let {
            parentServiceTemplate.relationshipTypes = parentServiceTemplate.relationshipTypes ?: hashMapOf()
            parentServiceTemplate.relationshipTypes?.putAll(toMerge.relationshipTypes as MutableMap)
        }

        toMerge.artifactTypes?.let {
            parentServiceTemplate.artifactTypes = parentServiceTemplate.artifactTypes ?: hashMapOf()
            parentServiceTemplate.artifactTypes?.putAll(toMerge.artifactTypes as MutableMap)
        }

        toMerge.repositories?.let {
            parentServiceTemplate.repositories = parentServiceTemplate.repositories ?: hashMapOf()
            parentServiceTemplate.repositories?.putAll(toMerge.repositories as MutableMap)
        }

        parentServiceTemplate.topologyTemplate = parentServiceTemplate.topologyTemplate ?: TopologyTemplate()

        toMerge.topologyTemplate?.inputs?.let {
            parentServiceTemplate.topologyTemplate?.inputs = parentServiceTemplate.topologyTemplate?.inputs
                ?: hashMapOf()
            parentServiceTemplate.topologyTemplate?.inputs?.putAll(parentServiceTemplate.topologyTemplate?.inputs as MutableMap)
        }

        toMerge.topologyTemplate?.nodeTemplates?.let {
            parentServiceTemplate.topologyTemplate?.nodeTemplates =
                parentServiceTemplate.topologyTemplate?.nodeTemplates
                ?: hashMapOf()
            parentServiceTemplate.topologyTemplate?.nodeTemplates?.putAll(parentServiceTemplate.topologyTemplate?.nodeTemplates as MutableMap)
        }

        toMerge.topologyTemplate?.relationshipTemplates?.let {
            parentServiceTemplate.topologyTemplate?.relationshipTemplates =
                parentServiceTemplate.topologyTemplate?.relationshipTemplates
                ?: hashMapOf()
            parentServiceTemplate.topologyTemplate?.relationshipTemplates?.putAll(parentServiceTemplate.topologyTemplate?.relationshipTemplates as MutableMap)
        }

        toMerge.topologyTemplate?.policies?.let {
            parentServiceTemplate.topologyTemplate?.policies = parentServiceTemplate.topologyTemplate?.policies
                ?: hashMapOf()
            parentServiceTemplate.topologyTemplate?.policies?.putAll(parentServiceTemplate.topologyTemplate?.policies as MutableMap)
        }

        toMerge.topologyTemplate?.workflows?.let {
            parentServiceTemplate.topologyTemplate?.workflows = parentServiceTemplate.topologyTemplate?.workflows
                ?: hashMapOf()
            parentServiceTemplate.topologyTemplate?.workflows?.putAll(parentServiceTemplate.topologyTemplate?.workflows as MutableMap)
        }
        return parentServiceTemplate
    }
}
