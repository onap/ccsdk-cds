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

package org.onap.ccsdk.cds.controllerblueprints.core.interfaces

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.cds.controllerblueprints.core.data.CapabilityDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipType
import java.io.Serializable

/**
 * BlueprintRepoFileService
 * @author Brinda Santh
 *
 */

interface BlueprintRepoService : Serializable {

    @Throws(BlueprintException::class)
    fun getNodeType(nodeTypeName: String): NodeType

    @Throws(BlueprintException::class)
    fun getDataType(dataTypeName: String): DataType

    @Throws(BlueprintException::class)
    fun getArtifactType(artifactTypeName: String): ArtifactType

    @Throws(BlueprintException::class)
    fun getRelationshipType(relationshipTypeName: String): RelationshipType

    @Throws(BlueprintException::class)
    fun getCapabilityDefinition(capabilityDefinitionName: String): CapabilityDefinition
}
