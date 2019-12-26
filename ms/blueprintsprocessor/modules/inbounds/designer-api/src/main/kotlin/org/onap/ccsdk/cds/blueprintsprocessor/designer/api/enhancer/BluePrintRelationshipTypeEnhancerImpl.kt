/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.enhancer

import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.utils.BluePrintEnhancerUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipType
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintRelationshipTypeEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintRepoService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BluePrintRelationshipTypeEnhancerImpl(
    private val bluePrintRepoService: BluePrintRepoService,
    private val bluePrintTypeEnhancerService: BluePrintTypeEnhancerService
) : BluePrintRelationshipTypeEnhancer {

    private val log = logger(BluePrintRelationshipTypeEnhancerImpl::class)

    lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>
    lateinit var bluePrintContext: BluePrintContext

    override fun enhance(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        name: String,
        relationshipType: RelationshipType
    ) {
        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val derivedFrom = relationshipType.derivedFrom

        if (!BluePrintTypes.rootRelationshipTypes().contains(derivedFrom)) {
            val derivedFromRelationshipType =
                BluePrintEnhancerUtils.populateRelationshipType(bluePrintContext, bluePrintRepoService, name)
            // Enrich RelationshipType
            enhance(bluePrintRuntimeService, derivedFrom, derivedFromRelationshipType)
        }

        // NodeType Attribute Definitions
        enrichRelationshipTypeAttributes(name, relationshipType)

        // NodeType Property Definitions
        enrichRelationshipTypeProperties(name, relationshipType)

        // TODO("Interface Enrichment, If needed")
    }

    open fun enrichRelationshipTypeAttributes(nodeTypeName: String, relationshipType: RelationshipType) {
        relationshipType.attributes?.let {
            bluePrintTypeEnhancerService.enhanceAttributeDefinitions(
                bluePrintRuntimeService,
                relationshipType.attributes!!
            )
        }
    }

    open fun enrichRelationshipTypeProperties(nodeTypeName: String, relationshipType: RelationshipType) {
        relationshipType.properties?.let {
            bluePrintTypeEnhancerService.enhancePropertyDefinitions(
                bluePrintRuntimeService,
                relationshipType.properties!!
            )
        }
    }
}
