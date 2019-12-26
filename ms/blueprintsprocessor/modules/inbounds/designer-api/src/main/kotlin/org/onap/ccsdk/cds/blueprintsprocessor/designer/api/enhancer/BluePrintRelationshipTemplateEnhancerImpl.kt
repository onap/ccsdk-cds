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
import org.onap.ccsdk.cds.controllerblueprints.core.data.RelationshipTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintRelationshipTemplateEnhancer
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
open class BluePrintRelationshipTemplateEnhancerImpl(
    private val bluePrintRepoService: BluePrintRepoService,
    private val bluePrintTypeEnhancerService: BluePrintTypeEnhancerService
) :
    BluePrintRelationshipTemplateEnhancer {

    private val log = logger(BluePrintRelationshipTemplateEnhancerImpl::class)

    lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>
    lateinit var bluePrintContext: BluePrintContext

    override fun enhance(
        bluePrintRuntimeService: BluePrintRuntimeService<*>,
        name: String,
        relationshipTemplate: RelationshipTemplate
    ) {
        log.info("***** Enhancing RelationshipTemplate($name)")
        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val relationshipTypeName = relationshipTemplate.type
        // Get RelationshipType from Repo and Update Service Template
        val relationshipType =
            BluePrintEnhancerUtils.populateRelationshipType(
                bluePrintContext,
                bluePrintRepoService,
                relationshipTypeName
            )

        // Enrich NodeType
        bluePrintTypeEnhancerService.enhanceRelationshipType(
            bluePrintRuntimeService,
            relationshipTypeName,
            relationshipType
        )
    }
}
