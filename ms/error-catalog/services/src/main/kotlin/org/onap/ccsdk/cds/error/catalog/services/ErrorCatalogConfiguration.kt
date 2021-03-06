/*
 *  Copyright © 2020 IBM, Bell Canada.
 *  Modifications Copyright © 2019-2020 AT&T Intellectual Property.
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
package org.onap.ccsdk.cds.error.catalog.services

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Configuration
@EnableConfigurationProperties(ErrorCatalogProperties::class)
open class ErrorCatalogConfiguration

@Component
@ConfigurationProperties(prefix = "error.catalog")
open class ErrorCatalogProperties {

    lateinit var type: String
    lateinit var applicationId: String
    var errorDefinitionDir: String? = null
}
