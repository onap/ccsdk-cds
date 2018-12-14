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

package org.onap.ccsdk.apps.controllerblueprints.service.enhancer

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.apps.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.apps.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintArtifactDefinitionEnhancer
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintRepoService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.springframework.stereotype.Service

@Service
open class BluePrintArtifactDefinitionEnhancerImpl(private val bluePrintRepoService: BluePrintRepoService,
                                                   private val bluePrintTypeEnhancerService: BluePrintTypeEnhancerService,
                                                   private val resourceAssignmentEnhancerService: ResourceAssignmentEnhancerService)
    : BluePrintArtifactDefinitionEnhancer {

    companion object {
        const val ARTIFACT_TYPE_MAPPING_SOURCE: String = "artifact-mapping-resource"
    }


    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintArtifactDefinitionEnhancerImpl::class.toString())

    lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>
    lateinit var bluePrintContext: BluePrintContext


    override fun enhance(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, artifactDefinition: ArtifactDefinition) {
        log.info("enhancing ArtifactDefinition($name)")

        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val artifactTypeName = artifactDefinition.type
                ?: throw BluePrintException("Artifact type is missing for ArtifactDefinition($name)")

        // Populate Artifact Type
        populateArtifactType(artifactTypeName)

        when (artifactTypeName) {
            ARTIFACT_TYPE_MAPPING_SOURCE -> {
                enhanceMappingType(name, artifactDefinition)
            }
        }
    }

    // Enhance Resource Mapping
    open fun enhanceMappingType(name: String, artifactDefinition: ArtifactDefinition) {

        val artifactFilePath = "${bluePrintContext.rootPath}/${artifactDefinition.file}"

        val alreadyEnhancedKey = "enhanced-${artifactDefinition.file}"
        val alreadyEnhanced = bluePrintRuntimeService.check(alreadyEnhancedKey)

        log.info("enhancing resource mapping file(${artifactDefinition.file}) already enhanced($alreadyEnhanced)")

        if (!alreadyEnhanced) {
            val resourceAssignments: MutableList<ResourceAssignment> = JacksonUtils.getListFromFile(artifactFilePath, ResourceAssignment::class.java)
                    as? MutableList<ResourceAssignment>
                    ?: throw BluePrintProcessorException("couldn't get ResourceAssignment definitions for the file($artifactFilePath)")

            // Call Resource Assignment Enhancer
            resourceAssignmentEnhancerService.enhanceBluePrint(bluePrintTypeEnhancerService, bluePrintRuntimeService, resourceAssignments)

            bluePrintRuntimeService.put(alreadyEnhancedKey, true.asJsonPrimitive())
        }
    }

    open fun populateArtifactType(artifactTypeName: String): ArtifactType {

        val artifactType = bluePrintContext.serviceTemplate.artifactTypes?.get(artifactTypeName)
                ?: bluePrintRepoService.getArtifactType(artifactTypeName)
                ?: throw BluePrintException("couldn't get ArtifactType($artifactTypeName) from repo.")
        bluePrintContext.serviceTemplate.artifactTypes?.put(artifactTypeName, artifactType)
        return artifactType
    }
}