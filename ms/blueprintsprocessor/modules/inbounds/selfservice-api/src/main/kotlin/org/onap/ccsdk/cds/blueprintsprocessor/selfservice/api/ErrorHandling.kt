/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

object SelfServiceApiDomains {

    // SelfServiceApi Domains Constants
    const val BLUEPRINT_PROCESSOR = "org.onap.ccsdk.cds.blueprintsprocessor"
    const val SELF_SERVICE_API = "org.onap.ccsdk.cds.blueprintsprocessor.resource.api"
    const val SELF_SERVICE_API_VALIDATOR = "org.onap.ccsdk.cds.blueprintsprocessor.resource.api.validator"
}

object SelfServiceApiHttpErrorCodes {
    init {
        // Register HttpErrorCodes
        // HttpErrorCodes.register("", 200)
    }
}

object SelfServiceGrpcErrorCodes {
    init {
        // Register GrpcErrorCodes
        // GrpcErrorCodes.register("", 3)
    }
}
