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

package org.onap.ccsdk.apps.controllerblueprints.core.service

import org.apache.commons.io.FileUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.data.*
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import java.io.File
import java.io.Serializable
import java.nio.charset.Charset

/**
 * BluePrintEnhancerRepoFileService
 * @author Brinda Santh
 *
 */

interface BluePrintEnhancerRepoService : Serializable {

    @Throws(BluePrintException::class)
    fun getNodeType(nodeTypeName: String): NodeType?

    @Throws(BluePrintException::class)
    fun getDataType(dataTypeName: String): DataType?

    @Throws(BluePrintException::class)
    fun getArtifactType(artifactTypeName: String): ArtifactType?

    @Throws(BluePrintException::class)
    fun getRelationshipType(relationshipTypeName: String): RelationshipType?

    @Throws(BluePrintException::class)
    fun getCapabilityDefinition(capabilityDefinitionName: String): CapabilityDefinition?

}


class BluePrintEnhancerRepoFileService(val basePath: String) : BluePrintEnhancerRepoService {

    val dataTypePath = basePath.plus(BluePrintConstants.PATH_DIVIDER).plus(BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE)
    val nodeTypePath = basePath.plus(BluePrintConstants.PATH_DIVIDER).plus(BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TYPE)
    val artifactTypePath = basePath.plus(BluePrintConstants.PATH_DIVIDER).plus(BluePrintConstants.MODEL_DEFINITION_TYPE_ARTIFACT_TYPE)
    val capabilityTypePath = basePath.plus(BluePrintConstants.PATH_DIVIDER).plus(BluePrintConstants.MODEL_DEFINITION_TYPE_CAPABILITY_TYPE)
    val relationshipTypePath = basePath.plus(BluePrintConstants.PATH_DIVIDER).plus(BluePrintConstants.MODEL_DEFINITION_TYPE_RELATIONSHIP_TYPE)
    val extension = ".json"

    override fun getDataType(dataTypeName: String): DataType? {
        val content = FileUtils.readFileToString(File(dataTypePath.plus(BluePrintConstants.PATH_DIVIDER)
                .plus(dataTypeName).plus(extension)), Charset.defaultCharset())
        return JacksonUtils.readValue(content)
    }

    override fun getNodeType(nodeTypeName: String): NodeType? {
        val content = FileUtils.readFileToString(File(nodeTypePath.plus(BluePrintConstants.PATH_DIVIDER)
                .plus(nodeTypeName).plus(extension)), Charset.defaultCharset())
        return JacksonUtils.readValue(content)
    }

    override fun getArtifactType(artifactTypeName: String): ArtifactType? {
        val content = FileUtils.readFileToString(File(artifactTypePath.plus(BluePrintConstants.PATH_DIVIDER)
                .plus(artifactTypeName).plus(extension)), Charset.defaultCharset())
        return JacksonUtils.readValue(content)
    }

    override fun getRelationshipType(relationshipTypeName: String): RelationshipType? {
        val content = FileUtils.readFileToString(File(relationshipTypePath.plus(BluePrintConstants.PATH_DIVIDER)
                .plus(relationshipTypeName).plus(extension)), Charset.defaultCharset())
        return JacksonUtils.readValue(content)
    }

    override fun getCapabilityDefinition(capabilityDefinitionName: String): CapabilityDefinition? {
        val content = FileUtils.readFileToString(File(capabilityTypePath.plus(BluePrintConstants.PATH_DIVIDER)
                .plus(capabilityDefinitionName).plus(extension)), Charset.defaultCharset())
        return JacksonUtils.readValue(content)
    }
}