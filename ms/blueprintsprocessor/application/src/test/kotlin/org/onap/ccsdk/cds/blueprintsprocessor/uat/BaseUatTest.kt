/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.cds.blueprintsprocessor.uat

import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.LogColor.COLOR_TEST_CLIENT
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.LogColor.resetContextColor
import org.onap.ccsdk.cds.blueprintsprocessor.uat.logging.LogColor.setContextColor
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.TestSecuritySettings
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.WorkingFoldersInitializer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@RunWith(SpringRunner::class)
// Also set blueprintsprocessor.httpPort=0
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
    initializers = [
        WorkingFoldersInitializer::class,
        TestSecuritySettings.ServerContextInitializer::class
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
abstract class BaseUatTest {

    @BeforeTest
    fun setScope() {
        setContextColor(COLOR_TEST_CLIENT)
    }

    @AfterTest
    fun clearScope() {
        resetContextColor()
    }

    companion object {

        const val UAT_BLUEPRINTS_BASE_DIR = "../../../components/model-catalog/blueprint-model/uat-blueprints"
    }
}
