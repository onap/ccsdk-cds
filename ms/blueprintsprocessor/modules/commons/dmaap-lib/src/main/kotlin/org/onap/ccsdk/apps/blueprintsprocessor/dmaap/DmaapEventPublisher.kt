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

package org.onap.ccsdk.apps.blueprintsprocessor.dmaap

import com.att.nsa.mr.client.MRBatchingPublisher
import com.att.nsa.mr.client.MRClientFactory
import com.att.nsa.mr.client.MRPublisher
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.ConfigurationPropertySources
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment
import org.springframework.core.io.support.ResourcePropertySource
import java.io.IOException
import java.util.Properties
import java.util.concurrent.TimeUnit

/**
 * Representation of DMaap event publisher, to create a session with the
 * message router and send messages when asked for. The producer.properties
 * is used for creating a session. In order to overwrite the parameters such
 * as host, topic, username and password, the event.properties can be used.
 *
 * compName : Name of the component appended in the event.properties file
 * to overwrite.
 * (E.g., so.topic=cds_so : In this "so" is the component name)
 */
@Configuration
@PropertySources(PropertySource("classpath:event.properties",
        "classpath:producer.properties"))
open class DmaapEventPublisher(compName: String = ""): EventPublisher {

    /**
     * Static variable for logging.
     */
    companion object {
        var log = LoggerFactory.getLogger(DmaapEventPublisher::class.java)!!
    }

    /**
     * The component name used in defining the event.properties file.
     */
    private var cName:String? = null

    /**
     * List of topics for a given message to be sent.
     */
    var topics = mutableListOf<String>()

    /**
     * List of clients formed for the list of topics where the messages has to
     * be sent.
     */
    var clients = mutableListOf<MRBatchingPublisher>()

    /**
     * The populated values from producer.properties which are overwritten
     * by the event.properties values according to the component information.
     */
    var prodProps: Properties = Properties()


    init {
        cName = compName
    }

    /**
     * Loads the producer.properties file and populates all the parameters
     * and then loads the event.properties file and populates the finalized
     * parameters such as host, topic, username and password if available for
     * the specified component. With this updated producer.properties, for
     * each topic a client will be created.
     */
    private fun loadPropertiesInfo() {
        if (prodProps.isEmpty) {
            parseEventProps(cName!!)
            addClients()
        }
    }

    /**
     * Adds clients for each topic into a client list.
     */
    private fun addClients() {
        for (topic in topics) {
            prodProps.setProperty("topic", topic)
            val client = MRClientFactory.createBatchingPublisher(prodProps)
            clients.add(client)
        }
    }

    /**
     * Parses the event.properties file and update it into the producer
     * .properties, where both the files are loaded and stored.
     */
    private fun parseEventProps(cName: String) {
        val env = EnvironmentContext.env as Environment
        val propSrc = ConfigurationPropertySources.get(env)
        val proProps = (env as ConfigurableEnvironment).propertySources.get(
                "class path resource [producer.properties]")

        if (proProps != null) {
            val entries = (proProps as ResourcePropertySource).source.entries
            for (e in entries) {
                prodProps.put(e.key, e.value)
            }
        } else {
            log.info("Unable to load the producer.properties file")
        }

        val eProps = Binder(propSrc).bind(cName, Properties::class.java).get()
        val top = eProps.get("topic").toString()
        if (top != "") {
            topics.addAll(top.split(","))
        }
        prodProps.putAll(eProps)
    }

    /**
     * Sends message to the sessions created by the information provided in
     * the producer.properties file.
     */
    override fun sendMessage(partition: String , messages: Collection<String>):
            Boolean {
        loadPropertiesInfo()
        var success = true
        val dmaapMsgs = mutableListOf<MRPublisher.message>()
        for (m in messages) {
            dmaapMsgs.add(MRPublisher.message(partition, m))
        }
        for (client in clients) {
            log.info("Sending messages to the DMaap Server")
            try {
                client.send(dmaapMsgs)
            } catch (e: IOException) {
                log.error(e.message, e)
                success = false
            }
        }
        return success
    }

    /**
     * Closes the opened session that was used for sending messages.
     */
    override fun close(timeout: Long) {
        log.debug("Closing the DMaap producer clients")
        if (!clients.isEmpty()) {
            for (client in clients) {
                try {
                    client.close(timeout, TimeUnit.SECONDS)
                } catch (e : IOException) {
                    log.warn("Unable to cleanly close the connection from " +
                            "the client $client", e)
                }
            }
        }
    }
    
}