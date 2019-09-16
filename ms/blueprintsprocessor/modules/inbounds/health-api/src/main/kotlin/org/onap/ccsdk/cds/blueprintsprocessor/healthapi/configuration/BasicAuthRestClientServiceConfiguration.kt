/*
 * Copyright © 2019-2020 Orange.
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

package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration

import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BasicAuthRestClientService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:application.properties")
open class BasicAuthRestClientServiceConfiguration {

    @Value("\${endpoints.user.name}")
    private val username: String? = null

    @Value("\${endpoints.user.password}")
    private val password: String? = null

    @Bean
    open fun getBasicAuthRestClientProperties(): BasicAuthRestClientProperties {
        val basicAuthRestClientProperties = BasicAuthRestClientProperties()
        basicAuthRestClientProperties.username = username.toString()
        basicAuthRestClientProperties.password = password.toString()
        return basicAuthRestClientProperties
    }

    @Bean
    open fun getBasicAuthRestClientService(): BasicAuthRestClientService {
        return BasicAuthRestClientService(getBasicAuthRestClientProperties())
    }


}
