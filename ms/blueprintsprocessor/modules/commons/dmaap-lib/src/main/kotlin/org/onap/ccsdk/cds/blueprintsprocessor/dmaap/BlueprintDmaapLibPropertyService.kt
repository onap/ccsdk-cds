/*-
 * ============LICENSE_START=======================================================
 * ONAP - CDS
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.cds.blueprintsprocessor.dmaap

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.dmaap.DmaapLibConstants.Companion.SERVICE_BLUEPRINT_DMAAP_LIB_PROPERTY
import org.onap.ccsdk.cds.blueprintsprocessor.dmaap.DmaapLibConstants.Companion.TYPE_HTTP_AAF_AUTH
import org.onap.ccsdk.cds.blueprintsprocessor.dmaap.DmaapLibConstants.Companion.TYPE_HTTP_NO_AUTH
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment
import org.springframework.core.io.support.ResourcePropertySource
import org.springframework.stereotype.Service
import java.util.Properties

/**
 * Representation of DMAAP lib property service to load the properties
 * according to the connection type to the DMAAP server and returning back
 * the appropriate DMAAP client to send messages DMAAP client.
 */
@Service(SERVICE_BLUEPRINT_DMAAP_LIB_PROPERTY)
@Configuration
@PropertySources(PropertySource("classpath:event.properties"))
open class BlueprintDmaapLibPropertyService(private var bluePrintPropertiesService: BlueprintPropertiesService) {

    /**
     * Static variable for logging.
     */
    companion object {

        var log = LoggerFactory.getLogger(
            BlueprintDmaapLibPropertyService::class.java
        )!!
    }

    /**
     * Environment entity to derive it from the system to load a specific
     * property file.
     */
    @Autowired
    lateinit var env: Environment

    /**
     * Returns the DMAAP client by providing the input properties as a JSON
     * node.
     */
    fun blueprintDmaapClientService(jsonNode: JsonNode):
        BlueprintDmaapClientService {
            val dmaapProps = dmaapClientProperties(jsonNode)
            return blueprintDmaapClientService(dmaapProps)
        }

    /**
     * Returns the DMAAP client by providing the input properties as a
     * selector string.
     */
    fun blueprintDmaapClientService(selector: String):
        BlueprintDmaapClientService {
            val prefix = "blueprintsprocessor.dmaapclient.$selector"
            val dmaapProps = dmaapClientProperties(prefix)
            return blueprintDmaapClientService(dmaapProps)
        }

    /**
     * Returns the DMAAP client properties from the type of connection it
     * requires.
     */
    fun dmaapClientProperties(prefix: String): DmaapClientProperties {
        val type = bluePrintPropertiesService.propertyBeanType(
            "$prefix.type", String::class.java
        )
        val clientProps: DmaapClientProperties

        when (type) {
            TYPE_HTTP_NO_AUTH -> {
                clientProps = bluePrintPropertiesService.propertyBeanType(
                    prefix, HttpNoAuthDmaapClientProperties::class.java
                )
                clientProps.props = parseEventProps()
            }

            TYPE_HTTP_AAF_AUTH -> {
                clientProps = bluePrintPropertiesService.propertyBeanType(
                    prefix, AafAuthDmaapClientProperties::class.java
                )
                clientProps.props = parseEventProps()
            }

            else -> {
                throw BlueprintProcessorException(
                    "DMAAP adaptor($type) is " +
                        "not supported"
                )
            }
        }
        return clientProps
    }

    /**
     * Returns the DMAAP client properties from the type of connection it
     * requires.
     */
    fun dmaapClientProperties(jsonNode: JsonNode): DmaapClientProperties {
        val type = jsonNode.get("type").textValue()
        val clientProps: DmaapClientProperties

        when (type) {
            TYPE_HTTP_NO_AUTH -> {
                clientProps = JacksonUtils.readValue(
                    jsonNode,
                    HttpNoAuthDmaapClientProperties::class.java
                )!!
                clientProps.props = parseEventProps()
            }

            TYPE_HTTP_AAF_AUTH -> {
                clientProps = JacksonUtils.readValue(
                    jsonNode,
                    AafAuthDmaapClientProperties::class.java
                )!!
                clientProps.props = parseEventProps()
            }

            else -> {
                throw BlueprintProcessorException(
                    "DMAAP adaptor($type) is " +
                        "not supported"
                )
            }
        }
        return clientProps
    }

    /**
     * Returns DMAAP client service according to the type of client properties.
     */
    private fun blueprintDmaapClientService(clientProps: DmaapClientProperties):
        BlueprintDmaapClientService {
            when (clientProps) {
                is HttpNoAuthDmaapClientProperties -> {
                    return HttpNoAuthDmaapClientService(clientProps)
                }

                is AafAuthDmaapClientProperties -> {
                    return AafAuthDmaapClientService(clientProps)
                }

                else -> {
                    throw BlueprintProcessorException(
                        "Unable to get the DMAAP " +
                            "client"
                    )
                }
            }
        }

    /**
     * Parses the event.properties file which contains the default values for
     * the connection required.
     */
    private fun parseEventProps(): Properties {
        val prodProps = Properties()
        val proProps = (env as ConfigurableEnvironment).propertySources.get(
            "class path resource [event.properties]"
        )

        if (proProps != null) {
            val entries = (proProps as ResourcePropertySource).source.entries
            for (e in entries) {
                prodProps.put(e.key, e.value)
            }
        } else {
            log.error("Unable to load the event.properties file")
        }
        return prodProps
    }
}
