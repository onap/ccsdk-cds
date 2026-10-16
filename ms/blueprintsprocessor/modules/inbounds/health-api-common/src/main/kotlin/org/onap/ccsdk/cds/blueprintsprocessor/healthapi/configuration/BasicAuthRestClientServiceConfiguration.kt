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
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:application.properties")
open class BasicAuthRestClientServiceConfiguration(private val securityConfiguration: SecurityEncryptionConfiguration) {

    private val log = LoggerFactory.getLogger(BasicAuthRestClientServiceConfiguration::class.java)

    @Value("\${endpoints.user.name}")
    private val username: String? = null

    @Value("\${endpoints.user.password}")
    private val password: String? = null

    @Bean
    open fun getBasicAuthRestClientProperties(): BasicAuthRestClientProperties {
        val basicAuthRestClientProperties = BasicAuthRestClientProperties()
        // The health checks pass the full service URL as path, so this client is shared by all of them.
        basicAuthRestClientProperties.url = ""
        basicAuthRestClientProperties.username = decrypt("endpoints.user.name", username!!)
        basicAuthRestClientProperties.password = decrypt("endpoints.user.password", password!!)
        basicAuthRestClientProperties.connectTimeout = CONNECT_TIMEOUT_MS
        basicAuthRestClientProperties.socketTimeout = SOCKET_TIMEOUT_MS
        return basicAuthRestClientProperties
    }

    @Bean
    open fun getBasicAuthRestClientService(): BasicAuthRestClientService {
        return BasicAuthRestClientService(getBasicAuthRestClientProperties())
    }

    private fun decrypt(property: String, value: String): String {
        return try {
            securityConfiguration.decrypt(value)
        } catch (e: IllegalArgumentException) {
            log.error(
                "Cannot decrypt {}, the health checks run without credentials and report DOWN. " +
                    "Since CDS 1.11.0 the value must be AES/GCM encrypted, values encrypted for 1.10.0 " +
                    "or earlier have to be re-encrypted (see the Upgrade notes in the release notes).",
                property, e
            )
            ""
        }
    }

    companion object {

        private const val CONNECT_TIMEOUT_MS = 5000
        private const val SOCKET_TIMEOUT_MS = 10000
    }
}
