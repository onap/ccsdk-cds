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

package org.onap.ccsdk.cds.blueprintsprocessor.db

import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.BluePrintDBLibPropertyService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

@Configuration
@Import(
    BluePrintPropertyConfiguration::class,
    BluePrintPropertiesService::class,
    BluePrintCoreConfiguration::class
)
@EnableConfigurationProperties
open class BluePrintDBLibConfiguration(private var bluePrintPropertiesService: BluePrintPropertiesService) {

    @Bean("primary-database-properties")
    open fun getPrimaryProperties(): PrimaryDataSourceProperties {
        return bluePrintPropertiesService.propertyBeanType(
            DBLibConstants.PREFIX_DB,
            PrimaryDataSourceProperties::class.java
        )
    }

    @Bean("primaryNamedParameterJdbcTemplate")
    open fun primaryNamedParameterJdbcTemplate(primaryDataSource: DataSource): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(primaryDataSource)
    }
}

/**
 * Exposed Dependency Service by this SSH Lib Module
 */
fun BluePrintDependencyService.dbLibPropertyService(): BluePrintDBLibPropertyService =
    instance(BluePrintDBLibPropertyService::class)

fun BluePrintDependencyService.primaryDBLibGenericService(): BluePrintDBLibGenericService =
    instance(PrimaryDBLibGenericService::class)

class DBLibConstants {
    companion object {

        const val PREFIX_DB: String = "blueprintsprocessor.db"

        // list of database
        const val MARIA_DB: String = "maria-db"
        const val PROCESSOR_DB: String = "processor-db"
        const val MYSQL_DB: String = "mysql-db"
        const val ORACLE_DB: String = "oracle-db"
        const val POSTGRES_DB: String = "postgres-db"

        // List of database drivers
        const val DRIVER_MARIA_DB = "org.mariadb.jdbc.Driver"
        const val DRIVER_MYSQL_DB = "com.mysql.jdbc.Driver"
        const val DRIVER_ORACLE_DB = "oracle.jdbc.driver.OracleDriver"
        const val DRIVER_POSTGRES_DB = "org.postgresql.Driver"
    }
}
