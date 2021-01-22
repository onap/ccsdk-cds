/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.nats.service

import io.nats.client.Nats
import io.nats.client.Options
import io.nats.streaming.NatsStreaming
import io.nats.streaming.StreamingConnection
import org.onap.ccsdk.cds.blueprintsprocessor.nats.TLSAuthNatsConnectionProperties
import org.onap.ccsdk.cds.controllerblueprints.core.splitCommaAsList
import javax.net.ssl.SSLContext

open class TLSAuthNatsService(private val natsConnectionProperties: TLSAuthNatsConnectionProperties) :
    BlueprintNatsService {

    lateinit var streamingConnection: StreamingConnection

    override suspend fun connection(): StreamingConnection {
        if (!::streamingConnection.isInitialized) {
            val serverList = natsConnectionProperties.host.splitCommaAsList()

            val options = Options.Builder()
                .connectionName(natsConnectionProperties.clientId)
                .servers(serverList.toTypedArray())
                // .sslContext(sslContext())
                .build()
            val natsConnection = Nats.connect(options)
            val streamingOptions = io.nats.streaming.Options.Builder().natsConn(natsConnection).build()
            streamingConnection = NatsStreaming.connect(
                natsConnectionProperties.clusterId,
                natsConnectionProperties.clientId, streamingOptions
            )
        }
        return streamingConnection
    }

    fun sslContext(): SSLContext {
        TODO("Implement NATS SSL Context")
    }
}
