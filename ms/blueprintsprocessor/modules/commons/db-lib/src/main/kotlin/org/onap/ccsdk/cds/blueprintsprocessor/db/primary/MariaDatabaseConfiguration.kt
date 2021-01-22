/*
 * Copyright © 2019 Bell Canada Intellectual Property.
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

import org.onap.ccsdk.cds.blueprintsprocessor.db.BlueprintDBLibGenericService
import org.onap.ccsdk.cds.blueprintsprocessor.db.MariaDataSourceProperties
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

class MariaDatabaseConfiguration(private val mariaDataSourceProperties: MariaDataSourceProperties) : BlueprintDBLibGenericService {

    val log = LoggerFactory.getLogger(MariaDatabaseConfiguration::class.java)!!

    override fun namedParameterJdbcTemplate(): NamedParameterJdbcTemplate {
        return mariaNamedParameterJdbcTemplate(mariaDataSource())
    }

    override fun query(sql: String, params: Map<String, Any>): List<Map<String, Any>> {
        return mariaNamedParameterJdbcTemplate(mariaDataSource()).queryForList(sql, params)
    }

    override fun update(sql: String, params: Map<String, Any>): Int {
        return mariaNamedParameterJdbcTemplate(mariaDataSource()).update(sql, params)
    }

    fun mariaDataSource(): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName(mariaDataSourceProperties.driverClassName)
        dataSource.url = mariaDataSourceProperties.url
        dataSource.username = mariaDataSourceProperties.username
        dataSource.password = mariaDataSourceProperties.password
        return dataSource
    }

    fun mariaNamedParameterJdbcTemplate(mariaDataSource: DataSource): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(mariaDataSource)
    }
}
