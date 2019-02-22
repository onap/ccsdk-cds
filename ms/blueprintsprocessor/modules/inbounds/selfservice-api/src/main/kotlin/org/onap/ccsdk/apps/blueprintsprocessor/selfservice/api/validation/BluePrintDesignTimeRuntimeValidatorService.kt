/*
 *  Copyright © 2018 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.apps.blueprintsprocessor.selfservice.api.validation

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.apps.controllerblueprints.validation.BluePrintDesignTimeValidatorService
import org.springframework.stereotype.Service

@Service
open class BluePrintRuntimeValidatorService(
        private val bluePrintTypeValidatorService: BluePrintTypeValidatorService) : BluePrintDesignTimeValidatorService(bluePrintTypeValidatorService) {

    override fun validateBluePrints(bluePrintRuntimeService: BluePrintRuntimeService<*>): Boolean {

        bluePrintTypeValidatorService.validateServiceTemplate(bluePrintRuntimeService, "service_template",
                bluePrintRuntimeService.bluePrintContext().serviceTemplate)
        if (bluePrintRuntimeService.getBluePrintError().errors.size > 0) {
            throw BluePrintException("failed in blueprint validation : ${bluePrintRuntimeService.getBluePrintError().errors.joinToString("\n")}")
        }
        return true
    }
}