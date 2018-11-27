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

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintValidationError
import org.onap.ccsdk.apps.controllerblueprints.core.data.Workflow
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintWorkflowValidator
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext

open class BluePrintWorkflowValidatorImpl(private val bluePrintTypeValidatorService: BluePrintTypeValidatorService) : BluePrintWorkflowValidator {

    override fun validate(bluePrintContext: BluePrintContext, error: BluePrintValidationError, name: String, type: Workflow) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}