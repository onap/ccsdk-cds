/*
 *  Copyright © 2020 Deutsche Telekom AG.
 *  Modifications Copyright © 2020 Orange.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.profile.upload

import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintCoreConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    BlueprintPropertyConfiguration::class,
    BlueprintPropertiesService::class,
    BlueprintCoreConfiguration::class
)
@EnableConfigurationProperties
open class K8sProfileUploadConfiguration(private var bluePrintPropertiesService: BlueprintPropertiesService) {

    @Bean("k8s-plugin-properties")
    open fun getProperties(): K8sProfileUploadProperties {
        return bluePrintPropertiesService.propertyBeanType(
            K8sProfileUploadConstants.PREFIX_K8S_PLUGIN,
            K8sProfileUploadProperties::class.java
        )
    }
}

class K8sProfileUploadConstants {
    companion object {

        const val PREFIX_K8S_PLUGIN: String = "blueprintprocessor.k8s.plugin"
    }
}
