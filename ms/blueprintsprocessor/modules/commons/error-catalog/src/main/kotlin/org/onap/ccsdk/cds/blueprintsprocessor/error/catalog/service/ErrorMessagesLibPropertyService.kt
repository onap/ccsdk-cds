/*
 *  Copyright © 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.error.catalog.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment
import org.springframework.core.io.support.ResourcePropertySource
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import java.util.Properties

/**
 * Representation of Blueprint Error Message lib property service to load the properties
 */
@Configuration
@PropertySources(PropertySource("classpath:error-messages_en.properties"), PropertySource("classpath:application.properties"))
@ConditionalOnProperty(
        name = ["blueprintprocessor.error.catalog.type"],
        havingValue = "properties"
)
open class ErrorMessagesLibPropertyService(private var env: Environment) :
        ErrorMessagesLibService {

    /**
     * Static variable for logging.
     */
    companion object {
        var log = LoggerFactory.getLogger(
            ErrorMessagesLibPropertyService::class.java
        )!!
        const val propertyFileName = "error-messages_en.properties"
    }

    private val properties: Properties

    init {
        properties = parseErrorMessagesProps()
    }

    /**
     * Parses the error-messages.properties file which contains error messages
     */
    private fun parseErrorMessagesProps(): Properties {
        val prodProps = Properties()
        val proProps = (env as ConfigurableEnvironment).propertySources.get(
            "class path resource [$propertyFileName]"
        )

        if (proProps != null) {
            val entries = (proProps as ResourcePropertySource).source.entries
            for (e in entries) {
                prodProps[e.key] = e.value
            }
        } else {
            log.error("Unable to load the error messages $propertyFileName file")
        }
        return prodProps
    }

    override fun getErrorMessage(domain: String, key: String): String? {
        return findErrorMessage(domain.plus(key.toLowerCase()))
    }

    override fun getErrorMessage(attribute: String): String? {
        return findErrorMessage(attribute)
    }

    private fun findErrorMessage(attribute: String): String? {
        return properties.getProperty(attribute)
    }
}
