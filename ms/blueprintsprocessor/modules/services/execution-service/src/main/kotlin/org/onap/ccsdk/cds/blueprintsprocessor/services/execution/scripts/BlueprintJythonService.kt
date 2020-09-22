/*
 * Copyright © 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.springframework.stereotype.Service

// TODO("After Jython depreciation, this interface will be removed")

@Deprecated("CDS won's support Jython services")
interface BlueprintJythonService {

    fun jythonComponentInstance(bluePrintContext: BluePrintContext, scriptClassReference: String):
        BlueprintFunctionNode<*, *>
}

@Service
open class DeprecatedBlueprintJythonService : BlueprintJythonService {

    override fun jythonComponentInstance(bluePrintContext: BluePrintContext, scriptClassReference: String):
        BlueprintFunctionNode<*, *> {
            throw BluePrintProcessorException("Include python-executor module for Jython support")
        }
}
