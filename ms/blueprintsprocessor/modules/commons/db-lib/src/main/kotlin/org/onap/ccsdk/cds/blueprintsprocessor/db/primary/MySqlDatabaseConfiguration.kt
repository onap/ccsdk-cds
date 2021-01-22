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
import org.onap.ccsdk.cds.blueprintsprocessor.db.MySqlDataSourceProperties
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

class MySqlDatabaseConfiguration(private val mySqlDataSourceProperties: MySqlDataSourceProperties) : BlueprintDBLibGenericService {

    override fun namedParameterJdbcTemplate(): NamedParameterJdbcTemplate {
        return mySqlNamedParameterJdbcTemplate(mySqlDataSource())
    }

    override fun query(sql: String, params: Map<String, Any>): List<Map<String, Any>> {
        return mySqlNamedParameterJdbcTemplate(mySqlDataSource()).queryForList(sql, params)
    }

    override fun update(sql: String, params: Map<String, Any>): Int {
        return mySqlNamedParameterJdbcTemplate(mySqlDataSource()).update(sql, params)
    }

    val log = LoggerFactory.getLogger(PrimaryDatabaseConfiguration::class.java)!!

    fun mySqlDataSource(): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName(mySqlDataSourceProperties.driverClassName)
        dataSource.url = mySqlDataSourceProperties.url
        dataSource.username = mySqlDataSourceProperties.username
        dataSource.password = mySqlDataSourceProperties.password
        return dataSource
    }

    fun mySqlNamedParameterJdbcTemplate(mySqlDataSource: DataSource): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(mySqlDataSource)
    }
}
