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

package org.onap.ccsdk.apps.controllerblueprints.core.service

import org.junit.Test
import org.onap.ccsdk.apps.controllerblueprints.core.factory.BluePrintParserFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.assertNotNull

/**
 *
 *
 * @author Brinda Santh
 */
class BluePrintParserFactoryTest {
    private val logger: Logger = LoggerFactory.getLogger(this::class.toString())

    @Test
    fun testBluePrintJson() {
        val basepath = "load/blueprints"

        val bluePrintContext: BluePrintContext = BluePrintParserFactory.instance(org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants.TYPE_DEFAULT)!!
                .readBlueprintFile("baseconfiguration/Definitions/activation-blueprint.json", basepath)
        assertNotNull(bluePrintContext, "Failed to populate Blueprint context")
        logger.trace("Blue Print {}",bluePrintContext.blueprintJson(true))
    }
}