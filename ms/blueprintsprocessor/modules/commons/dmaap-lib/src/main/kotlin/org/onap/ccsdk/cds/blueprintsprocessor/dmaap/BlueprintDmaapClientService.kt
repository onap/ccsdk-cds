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

import com.att.nsa.mr.client.MRBatchingPublisher
import com.att.nsa.mr.client.MRPublisher
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Abstraction of DMAAP client services that could form DMAAP client from the
 * properties provided. This abstraction also provides a mechanism to send
 * messages with the given partition in a session and closing the same.
 */
interface BlueprintDmaapClientService {

    /**
     * Static variable for logging.
     */
    companion object {

        var log = LoggerFactory.getLogger(
            BlueprintDmaapClientService::class.java
        )!!
    }

    /**
     * Returns the properly constructed DMAAP client with the type.
     */
    fun getDmaapClient(): MutableList<MRBatchingPublisher>

    /**
     * Sends messages to the sessions created by the information provided from
     * application.properties and event.properties file
     */
    fun sendMessage(msgs: Collection<String>): Boolean {
        var success = true
        val clients = getDmaapClient()
        val dmaapMsgs = mutableListOf<MRPublisher.message>()
        for (m in msgs) {
            dmaapMsgs.add(MRPublisher.message("1", m))
        }
        log.info("Sending messages to the DMAAP Server")
        for (client in clients) {
            try {
                client.send(dmaapMsgs)
            } catch (e: IOException) {
                success = false
                log.error(e.message, e)
            }
        }
        return success
    }

    /**
     * Sends message to the sessions created by the information provided from
     * application.properties and event.properties file
     */
    fun sendMessage(msg: String): Boolean {
        val msgs = mutableListOf<String>()
        msgs.add(msg)
        return sendMessage(msgs)
    }

    /**
     * Closes the opened session that was used for sending messages.
     */
    fun close(timeout: Long): MutableList<MutableList<MRPublisher.message>>? {
        log.debug("Closing the DMAAP producer clients")
        var msgs: MutableList<MutableList<MRPublisher.message>> =
            mutableListOf()
        val clients = getDmaapClient()
        for (client in clients) {
            try {
                var ms = client.close(timeout, TimeUnit.SECONDS)
                msgs.add(ms)
            } catch (e: IOException) {
                log.warn(
                    "Unable to cleanly close the connection from the " +
                        "client $client",
                    e
                )
            }
        }
        return msgs
    }
}
