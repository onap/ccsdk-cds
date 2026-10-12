/*
 * Copyright © 2026 Deutsche Telekom AG.
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

import org.onap.ccsdk.cds.blueprintsprocessor.db.BluePrintDBLibGenericService
import org.onap.ccsdk.cds.blueprintsprocessor.db.PostgresDataSourceProperties
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

class PostgresDatabaseConfiguration(private val postgresDataSourceProperties: PostgresDataSourceProperties) : BluePrintDBLibGenericService {

    val log = LoggerFactory.getLogger(PostgresDatabaseConfiguration::class.java)!!

    override fun namedParameterJdbcTemplate(): NamedParameterJdbcTemplate {
        return postgresNamedParameterJdbcTemplate(postgresDataSource())
    }

    override fun query(sql: String, params: Map<String, Any>): List<Map<String, Any>> {
        return postgresNamedParameterJdbcTemplate(postgresDataSource()).queryForList(sql, params)
    }

    override fun update(sql: String, params: Map<String, Any>): Int {
        return postgresNamedParameterJdbcTemplate(postgresDataSource()).update(sql, params)
    }

    fun postgresDataSource(): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName(postgresDataSourceProperties.driverClassName)
        dataSource.url = postgresDataSourceProperties.url
        dataSource.username = postgresDataSourceProperties.username
        dataSource.password = postgresDataSourceProperties.password
        return dataSource
    }

    fun postgresNamedParameterJdbcTemplate(postgresDataSource: DataSource): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(postgresDataSource)
    }
}
