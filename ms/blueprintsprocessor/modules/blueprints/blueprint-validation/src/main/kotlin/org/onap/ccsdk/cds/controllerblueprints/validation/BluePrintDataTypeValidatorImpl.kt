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

package org.onap.ccsdk.cds.controllerblueprints.validation

import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintDataTypeValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.springframework.stereotype.Service

@Service("default-data-type-validator")
open class BluePrintDataTypeValidatorImpl(private val bluePrintTypeValidatorService: BluePrintTypeValidatorService) : BluePrintDataTypeValidator {

    override fun validate(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, dataType: DataType) {
        dataType.properties?.let {

            bluePrintTypeValidatorService.validatePropertyDefinitions(bluePrintRuntimeService, dataType.properties!!)
        }
    }
}
