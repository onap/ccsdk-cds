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

import org.onap.ccsdk.apps.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintArtifactTypeValidator
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService

open class BluePrintArtifactTypeValidatorImpl(private val bluePrintTypeValidatorService: BluePrintTypeValidatorService) : BluePrintArtifactTypeValidator {

    override fun validate(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, artifactType: ArtifactType) {

        artifactType.properties?.let {
            bluePrintTypeValidatorService.validatePropertyDefinitions(bluePrintRuntimeService, artifactType.properties!!)
        }
        // TODO ("Files Present ")
    }
}