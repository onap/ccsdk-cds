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

import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintParserDefaultService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintParserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * BluePrintParserFactory
 * @author Brinda Santh
 */

object BluePrintParserFactory {
    private val logger: Logger = LoggerFactory.getLogger(this::class.toString())

    var bluePrintParserServices: MutableMap<String, BluePrintParserService> = HashMap()

    init {
        logger.info("Initialised default BluePrintParser Service ")
        bluePrintParserServices.put(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.TYPE_DEFAULT, BluePrintParserDefaultService())
    }

    fun register(key:String, bluePrintParserService: BluePrintParserService){
        bluePrintParserServices.put(key, bluePrintParserService)
    }

    /**
     * Called by clients to get a Blueprint Parser for the Blueprint parser type
     */
    fun instance(key : String) : BluePrintParserService? {
        return bluePrintParserServices.get(key)
    }
}

