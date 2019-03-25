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

package org.onap.ccsdk.cds.blueprintsprocessor.core

import org.onap.ccsdk.cds.controllerblueprints.core.config.BluePrintPathConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.ConfigurationPropertySources
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service


@Configuration
open class BluePrintCoreConfiguration(private val bluePrintProperties: BlueprintProcessorProperties) {

    companion object {
        const val PREFIX_BLUEPRINT_PROCESSOR = "blueprintsprocessor"
    }

    @Bean
    open fun bluePrintPathConfiguration(): BluePrintPathConfiguration {
        return bluePrintProperties
                .propertyBeanType(PREFIX_BLUEPRINT_PROCESSOR, BluePrintPathConfiguration::class.java)
    }

}

@Configuration
open class BlueprintPropertyConfiguration {
    @Autowired
    lateinit var environment: Environment

    @Bean
    open fun bluePrintPropertyBinder(): Binder {
        val configurationPropertySource = ConfigurationPropertySources.get(environment)
        return Binder(configurationPropertySource)
    }
}

@Service
open class BlueprintProcessorProperties(private var bluePrintPropertyBinder: Binder) {
    fun <T> propertyBeanType(prefix: String, type: Class<T>): T {
        return bluePrintPropertyBinder.bind(prefix, Bindable.of(type)).get()
    }
}