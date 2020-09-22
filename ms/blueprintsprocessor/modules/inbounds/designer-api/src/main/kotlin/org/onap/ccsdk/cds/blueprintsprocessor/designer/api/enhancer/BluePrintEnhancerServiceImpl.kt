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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.enhancer

import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.DesignerApiDomains
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.updateErrorMessage
import org.onap.ccsdk.cds.controllerblueprints.core.httpProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintFileUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.utils.ResourceDictionaryUtils
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogCodes
import org.onap.ccsdk.cds.error.catalog.core.utils.errorCauseOrDefault
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.UUID

@Service
open class BluePrintEnhancerServiceImpl(
    private val bluePrintTypeEnhancerService: BluePrintTypeEnhancerService,
    private val resourceDefinitionEnhancerService: ResourceDefinitionEnhancerService
) : BluePrintEnhancerService {

    private val log = logger(BluePrintEnhancerServiceImpl::class)

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

            bluePrintTypeEnhancerService.enhanceServiceTemplate(
                blueprintRuntimeService, "service_template",
                blueprintRuntimeService.bluePrintContext().serviceTemplate
            )

            log.info("##### Enhancing blueprint Resource Definitions")
            val resourceDefinitions = resourceDefinitionEnhancerService.enhance(
                bluePrintTypeEnhancerService,
                blueprintRuntimeService
            )

            // Write the Enhanced Blueprint Definitions
            BluePrintFileUtils.writeEnhancedBluePrint(blueprintRuntimeService.bluePrintContext())

            // Write the Enhanced Blueprint Resource Definitions
            ResourceDictionaryUtils.writeResourceDefinitionTypes(basePath, resourceDefinitions)

            if (blueprintRuntimeService.getBluePrintError().errors.isNotEmpty()) {
                throw BluePrintException(blueprintRuntimeService.getBluePrintError().errors.toString())
            }
        } catch (e: BluePrintProcessorException) {
            val errorMsg = "Error while enriching the CBA package."
            throw e.updateErrorMessage(
                DesignerApiDomains.DESIGNER_API, errorMsg,
                "Wrong blueprint definitions or resource definitions."
            )
        } catch (e: IOException) {
            throw httpProcessorException(
                ErrorCatalogCodes.IO_FILE_INTERRUPT, DesignerApiDomains.DESIGNER_API,
                "IO Error: CBA file failed enrichment - ${e.message}", e.errorCauseOrDefault()
            )
        } catch (e: Exception) {
            throw httpProcessorException(
                ErrorCatalogCodes.IO_FILE_INTERRUPT, DesignerApiDomains.DESIGNER_API,
                "Error in Enriching CBA: ${e.message}", e.errorCauseOrDefault()
            )
        }
        return blueprintRuntimeService.bluePrintContext()
    }
}
