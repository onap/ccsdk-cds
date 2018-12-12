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

package org.onap.ccsdk.apps.controllerblueprints.service.enhancer

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintError
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintEnhancerService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintRepoService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintFileUtils
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.springframework.stereotype.Service

@Service
open class BluePrintEnhancerServiceImpl(private val bluePrintRepoService: BluePrintRepoService,
                                        private val bluePrintTypeEnhancerService: BluePrintTypeEnhancerService) : BluePrintEnhancerService {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintEnhancerServiceImpl::class.toString())

    override fun enhance(basePath: String, enrichedBasePath: String): BluePrintContext {
        BluePrintFileUtils.copyBluePrint(basePath, enrichedBasePath)
        BluePrintFileUtils.deleteBluePrintTypes(enrichedBasePath)
        val enhancedBluePrintContext = enhance(enrichedBasePath)
        BluePrintFileUtils.writeBluePrintTypes(enhancedBluePrintContext)
        return enhancedBluePrintContext
    }

    @Throws(BluePrintException::class)
    override fun enhance(basePath: String): BluePrintContext {
        log.info("Enhancing blueprint($basePath)")
        val bluePrintContext = BluePrintMetadataUtils.getBluePrintContext(basePath)
        val error = BluePrintError()
        bluePrintTypeEnhancerService.enhanceServiceTemplate(bluePrintContext, error, "service_template",
                bluePrintContext.serviceTemplate)
        return bluePrintContext
    }

    @Throws(BluePrintException::class)
    override fun enhance(serviceTemplate: ServiceTemplate): ServiceTemplate {
        val bluePrintContext = BluePrintContext(serviceTemplate)
        val error = BluePrintError()
        bluePrintTypeEnhancerService.enhanceServiceTemplate(bluePrintContext, error, "service_template",
                bluePrintContext.serviceTemplate)
        return bluePrintContext.serviceTemplate
    }
}

