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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api

object DesignerApiDomains {

    // Designer Api Domains Constants
    const val DESIGNER_API = "org.onap.ccsdk.cds.blueprintsprocessor.designer.api"
    const val DESIGNER_API_ENHANCER = "org.onap.ccsdk.cds.blueprintsprocessor.designer.api.enhancer"
    const val DESIGNER_API_HANDLER = "org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler"
    const val DESIGNER_API_LOAD = "org.onap.ccsdk.cds.blueprintsprocessor.designer.api.load"
    const val DESIGNER_API_SERVICE = "org.onap.ccsdk.cds.blueprintsprocessor.designer.api.service"
}

object DesignerApiHttpErrorCodes {
    init {
        // Register HttpErrorCodes
        // HttpErrorCodes.register("", 200)
    }
}

object DesignerGrpcErrorCodes {
    init {
        // Register GrpcErrorCodes
        // GrpcErrorCodes.register("", 3)
    }
}
