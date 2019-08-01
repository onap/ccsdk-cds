/*
 *  Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import kotlin.test.Test
import kotlin.test.assertNotNull

class ResourceSourceDSLTest {

    @Test
    fun testNodeTypeSourceInput() {
        val nodeTemplate = BluePrintTypes.nodeTypeSourceInput()
        //println(nodeTemplate.asJsonString(true))
        assertNotNull(nodeTemplate, "failed to generate nodeTypeSourceInput")
    }

    @Test
    fun testNodeTypeSourceDefault() {
        val nodeTemplate = BluePrintTypes.nodeTypeSourceDefault()
        //println(nodeTemplate.asJsonString(true))
        assertNotNull(nodeTemplate, "failed to generate nodeTypeSourceDefault")
    }

    @Test
    fun testNodeTypeSourceDb() {
        val nodeTemplate = BluePrintTypes.nodeTypeSourceDb()
        //println(nodeTemplate.asJsonString(true))
        assertNotNull(nodeTemplate, "failed to generate nodeTypeSourceDb")
    }

    @Test
    fun testNodeTypeSourceRest() {
        val nodeTemplate = BluePrintTypes.nodeTypeSourceRest()
        //println(nodeTemplate.asJsonString(true))
        assertNotNull(nodeTemplate, "failed to generate nodeTypeSourceRest")
    }

    @Test
    fun testNodeTypeSourceCapability() {
        val nodeTemplate = BluePrintTypes.nodeTypeSourceCapability()
        //println(nodeTemplate.asJsonString(true))
        assertNotNull(nodeTemplate, "failed to generate nodeTypeSourceCapability")
    }
}