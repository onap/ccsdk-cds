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

package org.onap.ccsdk.apps.controllerblueprints.core.validation

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintError
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext


open class BluePrintValidatorServiceImpl(private val bluePrintTypeValidatorService: BluePrintTypeValidatorService) : BluePrintValidatorService {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintValidatorServiceImpl::class.toString())

    override fun validateBluePrints(bluePrintContext: BluePrintContext, properties: MutableMap<String, Any>): Boolean {
        val error = BluePrintError()
        bluePrintTypeValidatorService.validateServiceTemplate(bluePrintContext, error, "default", bluePrintContext.serviceTemplate)
        if (error.errors.size > 0) {
            throw BluePrintException("failed in blueprint validation : ${error.errors.joinToString("\n")}")
        }
        return true
    }
}
