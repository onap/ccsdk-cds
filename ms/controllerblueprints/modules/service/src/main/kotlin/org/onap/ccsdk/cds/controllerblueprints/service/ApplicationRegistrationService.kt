/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *  Modifications Copyright © 2018 IBM.
 *  Modifications Copyright © 2019 Huawei.
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

package org.onap.ccsdk.cds.controllerblueprints.service

import org.apache.commons.collections.CollectionUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.factory.ResourceSourceMappingFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import javax.annotation.PostConstruct

class ApplicationRegistrationService {

    private val log = LoggerFactory.getLogger(ApplicationRegistrationService::class.java)

    @Value("#{'\${resourceSourceMappings}'.split(',')}")
    private val resourceSourceMappings: List<String>? = null

    @PostConstruct
    fun register() {
        registerDictionarySources()
    }

    fun registerDictionarySources() {
        log.info("Registering Dictionary Sources : {}", resourceSourceMappings)
        if (CollectionUtils.isNotEmpty(resourceSourceMappings)) {
            resourceSourceMappings!!.forEach { resourceSourceMapping ->
                val mappingKeyValue = resourceSourceMapping.split(
                        "=".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                if (mappingKeyValue.size == 2) {
                    ResourceSourceMappingFactory.registerSourceMapping(
                            mappingKeyValue[0].trim { it <= ' ' },
                            mappingKeyValue[1].trim { it <= ' ' })
                } else {
                    log.warn("failed to get resource source mapping {}",
                            resourceSourceMapping)
                }
            }
        }
    }
}