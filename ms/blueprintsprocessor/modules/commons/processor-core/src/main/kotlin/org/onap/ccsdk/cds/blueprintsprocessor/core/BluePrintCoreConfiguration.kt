/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.core

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.config.BluePrintLoadConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.ConfigurationPropertySources
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Configuration
open class BluePrintCoreConfiguration(private val bluePrintPropertiesService: BluePrintPropertiesService) {

    companion object {
        const val PREFIX_BLUEPRINT_PROCESSOR = "blueprintsprocessor"
    }

    @Bean
    open fun bluePrintLoadConfiguration(): BluePrintLoadConfiguration {
        return bluePrintPropertiesService
            .propertyBeanType(PREFIX_BLUEPRINT_PROCESSOR, BluePrintLoadConfiguration::class.java)
    }
}

@Configuration
open class BluePrintPropertyConfiguration {

    @Autowired
    lateinit var environment: Environment

    @Bean
    open fun bluePrintPropertyBinder(): Binder {
        val configurationPropertySource = ConfigurationPropertySources.get(environment)
        return Binder(configurationPropertySource)
    }
}

@Service
open class BluePrintPropertiesService(private var bluePrintPropertyBinder: Binder) {
    private val log = logger(BluePrintPropertiesService::class)

    fun <T> propertyBeanType(prefix: String, type: Class<T>): T {
        return try {
            bluePrintPropertyBinder.bind(prefix, Bindable.of(type)).get()
        } catch (e: NoSuchElementException) {
            val errMsg = "Error: missing property \"$prefix\"... Check the application.properties file."
            log.error(errMsg)
            throw BluePrintProcessorException(e, errMsg)
        }
    }
}

@Configuration
// Add Conditional property , If we try to manage on Application level
open class BlueprintDependencyConfiguration : ApplicationContextAware {

    private val log = LoggerFactory.getLogger(BlueprintDependencyConfiguration::class.java)!!

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        BluePrintDependencyService.inject(applicationContext)
        log.info("Dependency Management module created...")
    }
}
