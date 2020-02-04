/*
 *  Copyright © 2020 IBM, Bell Canada.
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

package org.onap.ccsdk.error.catalog.config

import org.onap.ccsdk.error.catalog.ErrorCatalogDatabaseProperties
import org.onap.ccsdk.error.catalog.ErrorMessageLibConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import javax.sql.DataSource
import kotlin.collections.HashMap

@Configuration
@ConditionalOnProperty(
        name = [ErrorMessageLibConstants.ERROR_CATALOG_TYPE],
        havingValue = ErrorMessageLibConstants.ERROR_CATALOG_TYPE_DB
)
@EnableJpaRepositories(
        basePackages = [ErrorMessageLibConstants.ERROR_CATALOG_REPOSITORY],
        entityManagerFactoryRef = "errorCatalogEntityManager",
        transactionManagerRef = "errorCatalogTransactionManager"
)
@EnableConfigurationProperties(ErrorCatalogDatabaseProperties::class)
open class ErrorCatalogDatabaseConfiguration {
    @Autowired
    lateinit var errorCatalogDatabaseProperties: ErrorCatalogDatabaseProperties

    @Bean("errorCatalogEntityManager")
    open fun errorCatalogEntityManager(): LocalContainerEntityManagerFactoryBean {
        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = errorCatalogDataSource()
        em.setPackagesToScan(ErrorMessageLibConstants.ERROR_CATALOG_MODELS)
        val vendorAdapter = HibernateJpaVendorAdapter()
        em.jpaVendorAdapter = vendorAdapter
        val properties = HashMap<String, String>()
        properties["hibernate.hbm2ddl.auto"] = errorCatalogDatabaseProperties.hibernateHbm2ddlAuto
        properties["hibernate.dialect"] = errorCatalogDatabaseProperties.hibernateDialect
        em.setJpaPropertyMap(properties)
        return em
    }

    @Bean("errorCatalogDataSource")
    open fun errorCatalogDataSource(): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName(errorCatalogDatabaseProperties.driverClassName)
        dataSource.url = errorCatalogDatabaseProperties.url
        dataSource.username = errorCatalogDatabaseProperties.username
        dataSource.password = errorCatalogDatabaseProperties.password

        return dataSource
    }

    @Bean("errorCatalogTransactionManager")
    open fun errorCatalogTransactionManager(): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = errorCatalogEntityManager().getObject()
        return transactionManager
    }
}
