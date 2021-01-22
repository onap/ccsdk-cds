/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution

import org.onap.ccsdk.cds.blueprintsprocessor.db.BlueprintDBLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.db.PrimaryDataSourceProperties
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.PrimaryDatabaseConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
@Import(BlueprintDBLibConfiguration::class)
@EnableJpaRepositories(
    basePackages = [
        "org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution",
        "org.onap.ccsdk.cds.blueprintsprocessor.db.primary"
    ],
    entityManagerFactoryRef = "primaryEntityManager",
    transactionManagerRef = "primaryTransactionManager"
)
@EnableJpaAuditing
open class TestDatabaseConfiguration(primaryDataSourceProperties: PrimaryDataSourceProperties) :
    PrimaryDatabaseConfiguration(primaryDataSourceProperties) {

    @Bean("primaryEntityManager")
    open fun primaryEntityManager(): LocalContainerEntityManagerFactoryBean {
        return primaryEntityManager(
            "org.onap.ccsdk.cds.blueprintsprocessor.db.primary",
            "org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.db"
        )
    }

    @Bean("primaryDataSource")
    override fun primaryDataSource(): DataSource {
        return super.primaryDataSource()
    }

    @Bean("primaryTransactionManager")
    override fun primaryTransactionManager(): PlatformTransactionManager {
        return super.primaryTransactionManager()
    }
}
