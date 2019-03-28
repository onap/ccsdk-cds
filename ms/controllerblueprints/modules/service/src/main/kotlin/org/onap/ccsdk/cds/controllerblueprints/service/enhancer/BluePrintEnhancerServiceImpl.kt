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

package org.onap.ccsdk.cds.controllerblueprints.service.enhancer

import org.slf4j.LoggerFactory
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintFileUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.utils.ResourceDictionaryUtils
import org.springframework.stereotype.Service
import java.util.*

@Service
open class BluePrintEnhancerServiceImpl(private val bluePrintTypeEnhancerService: BluePrintTypeEnhancerService,
                                        private val resourceDefinitionEnhancerService: ResourceDefinitionEnhancerService) : BluePrintEnhancerService {

    private val log= LoggerFactory.getLogger(BluePrintEnhancerServiceImpl::class.toString())

    override suspend fun enhance(basePath: String, enrichedBasePath: String): BluePrintContext {

        // Copy the Blueprint Content to Target Location
        BluePrintFileUtils.copyBluePrint(basePath, enrichedBasePath)

        // Enhance the Blueprint
        return enhance(enrichedBasePath)
    }

    @Throws(BluePrintException::class)
    override suspend fun enhance(basePath: String): BluePrintContext {

        log.info("Enhancing blueprint($basePath)")
        val blueprintRuntimeService = BluePrintMetadataUtils
                .getBaseEnhancementBluePrintRuntime(UUID.randomUUID().toString(), basePath)

        try {

            bluePrintTypeEnhancerService.enhanceServiceTemplate(blueprintRuntimeService, "service_template",
                    blueprintRuntimeService.bluePrintContext().serviceTemplate)

            log.info("##### Enhancing blueprint Resource Definitions")
            val resourceDefinitions = resourceDefinitionEnhancerService.enhance(bluePrintTypeEnhancerService,
                    blueprintRuntimeService)

            // Write the Enhanced Blueprint Definitions
            BluePrintFileUtils.writeEnhancedBluePrint(blueprintRuntimeService.bluePrintContext())

            // Write the Enhanced Blueprint Resource Definitions
            ResourceDictionaryUtils.writeResourceDefinitionTypes(basePath, resourceDefinitions)

            if (blueprintRuntimeService.getBluePrintError().errors.isNotEmpty()) {
                throw BluePrintException(blueprintRuntimeService.getBluePrintError().errors.toString())
            }

        } catch (e: Exception) {
            throw e
        }
        return blueprintRuntimeService.bluePrintContext()
    }

}

