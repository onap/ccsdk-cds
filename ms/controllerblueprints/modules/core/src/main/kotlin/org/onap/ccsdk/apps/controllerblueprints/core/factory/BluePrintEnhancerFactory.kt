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

package org.onap.ccsdk.apps.controllerblueprints.core.factory

import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintEnhancerService
import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager


/**
 * BluePrintEnhancerFactory
 * @author Brinda Santh
 *
 */

object BluePrintEnhancerFactory {
    private val log: EELFLogger = EELFManager.getInstance().getLogger(this::class.toString())

    var bluePrintEnhancerServices: MutableMap<String, BluePrintEnhancerService> = HashMap()

    fun register(key: String, bluePrintEnhancerService: BluePrintEnhancerService) {
        bluePrintEnhancerServices[key] = bluePrintEnhancerService
    }

    /**
     * Called by clients to get a Blueprint Parser for the Blueprint parser type
     */
    fun instance(key: String): BluePrintEnhancerService? {
        return bluePrintEnhancerServices.get(key)
    }
}
