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

package org.onap.ccsdk.cds.blueprintsprocessor.services.workflow

import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.onap.ccsdk.sli.core.sli.SvcLogicContext

class BlueprintSvcLogicContext : SvcLogicContext() {

    private var bluePrintRuntimeService: BlueprintRuntimeService<*>? = null
    private var request: Any? = null
    private var response: Any? = null

    fun getBlueprintService(): BlueprintRuntimeService<*> {
        return this.bluePrintRuntimeService!!
    }

    fun setBlueprintRuntimeService(bluePrintRuntimeService: BlueprintRuntimeService<*>) {
        this.bluePrintRuntimeService = bluePrintRuntimeService
    }

    fun setRequest(request: Any) {
        this.request = request
    }

    fun getRequest(): Any {
        return this.request!!
    }

    fun setResponse(response: Any) {
        this.response = response
    }

    fun getResponse(): Any {
        return this.response!!
    }
}
