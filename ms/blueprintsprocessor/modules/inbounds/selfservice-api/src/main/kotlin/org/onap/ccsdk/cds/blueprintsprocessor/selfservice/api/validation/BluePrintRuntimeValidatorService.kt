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

package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.validation

import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.validation.BluePrintDesignTimeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.validation.extension.ResourceDefinitionValidator
import org.springframework.stereotype.Service

@Service("bluePrintRuntimeValidatorService")
open class BluePrintRuntimeValidatorService(
    bluePrintTypeValidatorService: BluePrintTypeValidatorService,
    resourceDefinitionValidator: ResourceDefinitionValidator
) :
    BluePrintDesignTimeValidatorService(bluePrintTypeValidatorService, resourceDefinitionValidator)
