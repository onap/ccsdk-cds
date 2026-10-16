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

package org.onap.ccsdk.cds.blueprintsprocessor.db.primary

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.db.PrimaryDataSourceProperties
import org.onap.ccsdk.cds.blueprintsprocessor.db.TestDatabaseConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import javax.sql.DataSource
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [TestDatabaseConfiguration::class]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class PrimaryDatabaseConfigurationTest {

    @Autowired
    lateinit var primaryDataSource: DataSource

    @Test
    fun testPrimaryDatabaseConfiguration() {
        assertNotNull(primaryDataSource, " failed to create primary data source")
    }

    @Test
    fun `configured hibernate dialect is passed to hibernate`() {
        val properties = h2DataSourceProperties().apply { hibernateDialect = "org.hibernate.dialect.H2Dialect" }

        val jpaProperties = PrimaryDatabaseConfiguration(properties).primaryEntityManager().jpaPropertyMap

        assertEquals("org.hibernate.dialect.H2Dialect", jpaProperties["hibernate.dialect"])
    }

    @Test
    fun `hibernate dialect is left to hibernate when not configured`() {
        val jpaProperties = PrimaryDatabaseConfiguration(h2DataSourceProperties()).primaryEntityManager().jpaPropertyMap

        assertFalse(jpaProperties.containsKey("hibernate.dialect"))
    }

    private fun h2DataSourceProperties() = PrimaryDataSourceProperties().apply {
        url = "jdbc:h2:mem:dialecttest"
        username = "sa"
        password = ""
        driverClassName = "org.h2.Driver"
        hibernateHbm2ddlAuto = "none"
    }
}
