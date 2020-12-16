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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.kafka

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import java.nio.charset.Charset

open class MessagePrioritizationSerde : Serde<MessagePrioritization> {

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
    }

    override fun close() {
    }

    override fun deserializer(): Deserializer<MessagePrioritization> {
        return object : Deserializer<MessagePrioritization> {
            override fun deserialize(topic: String, data: ByteArray): MessagePrioritization {
                return JacksonUtils.readValue(String(data), MessagePrioritization::class.java)
                    ?: throw BluePrintProcessorException("failed to convert")
            }

            override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
            }

            override fun close() {
            }
        }
    }

    override fun serializer(): Serializer<MessagePrioritization> {
        return object : Serializer<MessagePrioritization> {
            override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
            }

            override fun serialize(topic: String?, data: MessagePrioritization): ByteArray {
                return data.asJsonString().toByteArray(Charset.defaultCharset())
            }

            override fun close() {
            }
        }
    }
}
