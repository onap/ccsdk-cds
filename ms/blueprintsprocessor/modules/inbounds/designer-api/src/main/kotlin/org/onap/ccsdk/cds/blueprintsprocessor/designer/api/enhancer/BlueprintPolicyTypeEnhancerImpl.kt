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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.enhancer

import org.onap.ccsdk.cds.controllerblueprints.core.data.PolicyType
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintPolicyTypeEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintRepoService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class BlueprintPolicyTypeEnhancerImpl(
    private val bluePrintRepoService: BlueprintRepoService,
    private val bluePrintTypeEnhancerService: BlueprintTypeEnhancerService
) :
    BlueprintPolicyTypeEnhancer {

    lateinit var bluePrintRuntimeService: BlueprintRuntimeService<*>

    override fun enhance(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, type: PolicyType) {

        this.bluePrintRuntimeService = bluePrintRuntimeService

        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
