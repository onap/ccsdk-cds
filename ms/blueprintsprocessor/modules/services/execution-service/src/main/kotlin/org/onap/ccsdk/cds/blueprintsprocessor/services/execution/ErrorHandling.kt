/*
 * Copyright © 2020 IBM, Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution

object ExecutionServiceDomains {

    // ExecutionService Domains Constants
    const val BLUEPRINT_PROCESSOR = "org.onap.ccsdk.cds.blueprintsprocessor"
    const val NETCONF_EXECUTOR = "org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor"
    const val RESOURCE_RESOLUTION = "org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution"
    const val RESTCONF_EXECUTOR = "org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor"
    const val CLI_EXECUTOR = "org.onap.ccsdk.cds.blueprintsprocessor.functions.cli.executor"
    const val PYTHON_EXECUTOR = "org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor"
    const val SDC_LISTENER = "org.onap.ccsdk.cds.sdclistener"
}

object ExecutionServiceHttpErrorCodes {
    init {
        // Register HttpErrorCodes
        // HttpErrorCodes.register("", 200)
    }
}

object ExecutionServiceGrpcErrorCodes {
    init {
        // Register GrpcErrorCodes
        // GrpcErrorCodes.register("", 3)
    }
}
