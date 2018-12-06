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

import org.onap.ccsdk.apps.blueprintsprocessor.db.PrimaryDataSourceProperties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import java.util.*
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
        basePackages = ["org.onap.ccsdk.apps.blueprintsprocessor.db.primary"],
        entityManagerFactoryRef = "primaryEntityManager",
        transactionManagerRef = "primaryTransactionManager"
)
open class PrimaryDatabaseConfiguration(private val primaryDataSourceProperties: PrimaryDataSourceProperties) {
    val log = LoggerFactory.getLogger(PrimaryDatabaseConfiguration::class.java)!!

    @Primary
    @Bean("primaryEntityManager")
    open fun primaryEntityManager(): LocalContainerEntityManagerFactoryBean {
        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = primaryDataSource()
        em.setPackagesToScan(
                "org.onap.ccsdk.apps.blueprintsprocessor.db.primary")

        val vendorAdapter = HibernateJpaVendorAdapter()
        em.jpaVendorAdapter = vendorAdapter
        val properties = HashMap<String, Any>()
        properties["hibernate.hbm2ddl.auto"] = primaryDataSourceProperties.hibernateHbm2ddlAuto
        properties["hibernate.dialect"] = primaryDataSourceProperties.hibernateDialect
        em.jpaPropertyMap = properties
        return em
    }

    @Primary
    @Bean("primaryDataSource")
    open fun primaryDataSource(): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName(primaryDataSourceProperties.driverClassName)
        dataSource.url = primaryDataSourceProperties.url
        dataSource.username = primaryDataSourceProperties.username
        dataSource.password = primaryDataSourceProperties.password
        return dataSource
    }

    @Primary
    @Bean("primaryTransactionManager")
    open fun primaryTransactionManager(): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = primaryEntityManager().getObject()
        log.info("Initialised Primary Transaction Manager for url ${primaryDataSourceProperties.url}")
        return transactionManager
    }

    @Bean("primaryNamedParameterJdbcTemplate")
    open fun primaryNamedParameterJdbcTemplate(primaryDataSource: DataSource): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(primaryDataSource)
    }

}