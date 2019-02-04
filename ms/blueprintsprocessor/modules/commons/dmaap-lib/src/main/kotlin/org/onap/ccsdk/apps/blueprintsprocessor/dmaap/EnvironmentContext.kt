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

package org.onap.ccsdk.apps.blueprintsprocessor.dmaap

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

/**
 * Abstraction of environment context information component.
 */
@Component
class EnvironmentContext {

    /**
     * Environment information.
     */
    companion object {
        var env: Environment? = null
    }

    /**
     * Environment auto-wired information.
     */
    @Autowired
    var environment: Environment? = null

    /**
     * Initiates the static variable after the instantiation takes place to
     * the auto-wired variable.
     */
    @PostConstruct
    private fun initStaticContext() {
        env = environment
    }

}