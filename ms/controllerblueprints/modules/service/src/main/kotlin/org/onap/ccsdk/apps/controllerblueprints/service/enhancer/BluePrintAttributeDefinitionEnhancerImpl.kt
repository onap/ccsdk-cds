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

package org.onap.ccsdk.apps.controllerblueprints.service.enhancer

import org.onap.ccsdk.apps.controllerblueprints.core.data.AttributeDefinition
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintAttributeDefinitionEnhancer
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintRepoService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService

class BluePrintAttributeDefinitionEnhancerImpl(private val bluePrintRepoService: BluePrintRepoService,
                                               private val bluePrintTypeEnhancerService: BluePrintTypeEnhancerService)
    : BluePrintAttributeDefinitionEnhancer {

    override fun enhance(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, type: AttributeDefinition) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}