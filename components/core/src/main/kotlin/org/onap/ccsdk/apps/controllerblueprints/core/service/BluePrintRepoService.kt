/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.data.*
import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonReactorUtils
import reactor.core.publisher.Mono
import java.io.Serializable

/**
 * BluePrintRepoFileService
 * @author Brinda Santh
 *
 */

interface BluePrintRepoService : Serializable {

    @Throws(BluePrintException::class)
    fun getNodeType(nodeTypeName: String): Mono<NodeType>

    @Throws(BluePrintException::class)
    fun getDataType(dataTypeName: String): Mono<DataType>

    @Throws(BluePrintException::class)
    fun getArtifactType(artifactTypeName: String): Mono<ArtifactType>

    @Throws(BluePrintException::class)
    fun getRelationshipType(relationshipTypeName: String): Mono<RelationshipType>

    @Throws(BluePrintException::class)
    fun getCapabilityDefinition(capabilityDefinitionName: String): Mono<CapabilityDefinition>

}


open class BluePrintRepoFileService(modelTypePath: String) : BluePrintRepoService {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintRepoFileService::class.toString())

    private val dataTypePath = modelTypePath.plus(BluePrintConstants.PATH_DIVIDER).plus(BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE)
    private val nodeTypePath = modelTypePath.plus(BluePrintConstants.PATH_DIVIDER).plus(BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TYPE)
    private val artifactTypePath = modelTypePath.plus(BluePrintConstants.PATH_DIVIDER).plus(BluePrintConstants.MODEL_DEFINITION_TYPE_ARTIFACT_TYPE)
    private val capabilityTypePath = modelTypePath.plus(BluePrintConstants.PATH_DIVIDER).plus(BluePrintConstants.MODEL_DEFINITION_TYPE_CAPABILITY_TYPE)
    private val relationshipTypePath = modelTypePath.plus(BluePrintConstants.PATH_DIVIDER).plus(BluePrintConstants.MODEL_DEFINITION_TYPE_RELATIONSHIP_TYPE)
    private val extension = ".json"

    override fun getDataType(dataTypeName: String): Mono<DataType> {
        val fileName = dataTypePath.plus(BluePrintConstants.PATH_DIVIDER)
                .plus(dataTypeName).plus(extension)
        return getModelType(fileName, DataType::class.java)
    }

    override fun getNodeType(nodeTypeName: String): Mono<NodeType> {
        val fileName = nodeTypePath.plus(BluePrintConstants.PATH_DIVIDER).plus(nodeTypeName).plus(extension)
        return getModelType(fileName, NodeType::class.java)
    }

    override fun getArtifactType(artifactTypeName: String): Mono<ArtifactType> {
        val fileName = artifactTypePath.plus(BluePrintConstants.PATH_DIVIDER)
                .plus(artifactTypeName).plus(extension)
        return getModelType(fileName, ArtifactType::class.java)
    }

    override fun getRelationshipType(relationshipTypeName: String): Mono<RelationshipType> {
        val fileName = relationshipTypePath.plus(BluePrintConstants.PATH_DIVIDER)
                .plus(relationshipTypeName).plus(extension)
        return getModelType(fileName, RelationshipType::class.java)
    }

    override fun getCapabilityDefinition(capabilityDefinitionName: String): Mono<CapabilityDefinition> {
        val fileName = capabilityTypePath.plus(BluePrintConstants.PATH_DIVIDER)
                .plus(capabilityDefinitionName).plus(extension)
        return getModelType(fileName, CapabilityDefinition::class.java)
    }

    private fun <T> getModelType(fileName: String, valueType: Class<T>): Mono<T> {
        return JacksonReactorUtils.readValueFromFile(fileName, valueType)
    }
}