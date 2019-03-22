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

package org.onap.ccsdk.apps.blueprintsprocessor.db.primary

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.apps.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.apps.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.apps.blueprintsprocessor.db.BluePrintDBLibConfiguration
import org.onap.ccsdk.apps.controllerblueprints.core.config.BluePrintLoadConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import javax.sql.DataSource
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [BlueprintPropertyConfiguration::class, BluePrintProperties::class,
    BluePrintDBLibConfiguration::class, BluePrintLoadConfiguration::class])
@TestPropertySource(locations = ["classpath:application-test.properties"])
@ComponentScan(basePackages = ["org.onap.ccsdk.apps.blueprintsprocessor", "org.onap.ccsdk.apps.controllerblueprints"])
@EnableAutoConfiguration
class PrimaryDatabaseConfigurationTest {

    @Autowired
    lateinit var primaryDataSource: DataSource

    @Test
    fun testPrimaryDatabaseConfiguration() {
        assertNotNull(primaryDataSource, " failed to create primary data source")
    }
}