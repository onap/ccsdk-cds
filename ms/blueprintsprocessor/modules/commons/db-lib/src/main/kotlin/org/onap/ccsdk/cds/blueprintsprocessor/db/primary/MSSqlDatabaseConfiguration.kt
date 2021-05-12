/*
 * Copyright © 2021 Bell Canada Intellectual Property.
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
import org.onap.ccsdk.cds.blueprintsprocessor.db.MSSqlDataSourceProperties
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

class MSSqlDatabaseConfiguration(private val msSqlDataSourceProperties: MSSqlDataSourceProperties) : BlueprintDBLibGenericService {
    override fun namedParameterJdbcTemplate(): NamedParameterJdbcTemplate {
        return msSqlNamedParameterJdbcTemplate(msSqlDataSource())
    }

    override fun query(sql: String, params: Map<String, Any>): List<Map<String, Any>> {
        return msSqlNamedParameterJdbcTemplate(msSqlDataSource()).queryForList(sql, params)
    }

    override fun update(sql: String, params: Map<String, Any>): Int {
        return msSqlNamedParameterJdbcTemplate(msSqlDataSource()).update(sql, params)
    }

    val log = LoggerFactory.getLogger(PrimaryDatabaseConfiguration::class.java)!!

    fun msSqlDataSource(): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName(msSqlDataSourceProperties.driverClassName)
        dataSource.url = msSqlDataSourceProperties.url
        dataSource.username = msSqlDataSourceProperties.username
        dataSource.password = msSqlDataSourceProperties.password
        return dataSource
    }

    fun msSqlNamedParameterJdbcTemplate(msSqlDataSource: DataSource): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(msSqlDataSource)
    }
}
