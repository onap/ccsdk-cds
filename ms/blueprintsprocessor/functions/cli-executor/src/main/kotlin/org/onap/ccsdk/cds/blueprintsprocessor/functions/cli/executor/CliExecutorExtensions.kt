/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2018-2019 IBM, Bell Canada
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.cli.executor

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.service.BlueprintSshClientService
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.sshClientService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintDependencyService

/**
 * Register the CLI module exposed dependency
 */
fun AbstractComponentFunction.cliDeviceInfo(requirementName: String): JsonNode {
    return bluePrintRuntimeService.resolveDSLExpression(requirementName)
}

fun AbstractComponentFunction.getSshClientService(cliDeviceInfo: JsonNode): BlueprintSshClientService {
    return BlueprintDependencyService.sshClientService(cliDeviceInfo)
}
