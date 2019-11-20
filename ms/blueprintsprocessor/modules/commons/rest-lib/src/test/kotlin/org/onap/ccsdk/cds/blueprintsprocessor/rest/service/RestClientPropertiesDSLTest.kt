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

package org.onap.ccsdk.cds.blueprintsprocessor.rest.service

import kotlin.test.assertNotNull
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.rest.dslBasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.dslSSLRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.dslTokenAuthRestClientProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes

class RestClientPropertiesDSLTest {

    @Test
    fun testBasicAuthRestClientProperties() {
        val properties = BluePrintTypes.dslBasicAuthRestClientProperties {
            url("http://localhost:8080")
            username("xxxxx")
            password("******")
        }
        assertNotNull(properties, "failed to get dslBasicAuthRestClientProperties")
    }

    @Test
    fun testBasicTokenAuthRestClientProperties() {
        val properties = BluePrintTypes.dslTokenAuthRestClientProperties {
            url("http://localhost:8080")
            token("sdfgfsadgsgf")
        }
        assertNotNull(properties, "failed to get dslTokenAuthRestClientProperties")
    }

    @Test
    fun testDslSSLRestClientProperties() {
        val properties = BluePrintTypes.dslSSLRestClientProperties {
            url("http://localhost:8080")
            keyStoreInstance("instance")
            sslTrust("sample-trust")
            sslTrustPassword("sample-trust-password")
            sslKey("sample-sslkey")
            sslKeyPassword("sample-key-password")
        }
        assertNotNull(properties, "failed to get dslSSLRestClientProperties")
    }
}
