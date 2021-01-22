/*-
 * ============LICENSE_START=======================================================
 * ONAP - CDS
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.cds.blueprintsprocessor.dmaap

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * Representation of DMAAP lib configuration to load the required property
 * files into the application context.
 */
@Configuration
@ComponentScan
@EnableConfigurationProperties
open class BlueprintDmaapLibConfiguration

/**
 * Util constants required for DMAAP library to use.
 */
class DmaapLibConstants {

    companion object {

        const val SERVICE_BLUEPRINT_DMAAP_LIB_PROPERTY = "blueprint" +
            "-dmaap-lib-property-service"
        const val TYPE_HTTP_NO_AUTH = "HTTPNOAUTH"
        const val TYPE_HTTP_AAF_AUTH = "HTTPAAF"
    }
}
