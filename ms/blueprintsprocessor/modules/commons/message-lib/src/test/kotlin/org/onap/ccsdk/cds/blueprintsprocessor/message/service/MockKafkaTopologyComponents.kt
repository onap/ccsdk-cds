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

package org.onap.ccsdk.cds.blueprintsprocessor.message.service

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.streams.processor.Processor
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.state.KeyValueStore
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import java.io.Serializable
import java.nio.charset.Charset
import java.util.UUID

class PriorityMessage : Serializable {

    lateinit var id: String
    lateinit var requestMessage: String
}

open class PriorityMessageSerde : Serde<PriorityMessage> {

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
    }

    override fun close() {
    }

    override fun deserializer(): Deserializer<PriorityMessage> {
        return object : Deserializer<PriorityMessage> {
            override fun deserialize(topic: String, data: ByteArray): PriorityMessage {
                return JacksonUtils.readValue(String(data), PriorityMessage::class.java)
                    ?: throw BlueprintProcessorException("failed to convert")
            }

            override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
            }

            override fun close() {
            }
        }
    }

    override fun serializer(): Serializer<PriorityMessage> {
        return object : Serializer<PriorityMessage> {
            override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
            }

            override fun serialize(topic: String?, data: PriorityMessage): ByteArray {
                return data.asJsonString().toByteArray(Charset.defaultCharset())
            }

            override fun close() {
            }
        }
    }
}

class FirstProcessor : Processor<ByteArray, ByteArray> {

    private val log = logger(FirstProcessor::class)

    private lateinit var context: ProcessorContext
    private lateinit var kvStore: KeyValueStore<String, PriorityMessage>

    override fun process(key: ByteArray, value: ByteArray) {
        log.info("First Processor key(${String(key)} : value(${String(value)})")
        val newMessage = PriorityMessage().apply {
            id = UUID.randomUUID().toString()
            requestMessage = String(value)
        }
        kvStore.put(newMessage.id, newMessage)
        this.context.forward(newMessage.id, newMessage)
    }

    override fun init(context: ProcessorContext) {
        log.info("init... ${context.keySerde()}, ${context.valueSerde()}")
        this.context = context
        this.kvStore = context.getStateStore("PriorityMessageState") as KeyValueStore<String, PriorityMessage>
    }

    override fun close() {
        log.info("Close...")
    }
}
