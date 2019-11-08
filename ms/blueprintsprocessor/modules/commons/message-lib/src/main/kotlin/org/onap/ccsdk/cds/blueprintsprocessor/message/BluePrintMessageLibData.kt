/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.message

import org.apache.kafka.streams.StreamsConfig

/** Producer Properties **/
open class MessageProducerProperties


open class KafkaBasicAuthMessageProducerProperties : MessageProducerProperties() {
    lateinit var bootstrapServers: String
    var topic: String? = null
    var clientId: String? = null
    // strongest producing guarantee
    var acks: String = "all"
    var retries: Int = 0
    // ensure we don't push duplicates
    val enableIdempotence: Boolean = true
}

/** Consumer Properties **/

open class MessageConsumerProperties

open class KafkaStreamsConsumerProperties : MessageConsumerProperties() {
    lateinit var bootstrapServers: String
    lateinit var applicationId: String
    lateinit var topic: String
    var autoOffsetReset: String = "latest"
    var processingGuarantee: String = StreamsConfig.EXACTLY_ONCE
}

open class KafkaStreamsBasicAuthConsumerProperties : KafkaStreamsConsumerProperties()

open class KafkaMessageConsumerProperties : MessageConsumerProperties() {
    lateinit var bootstrapServers: String
    lateinit var groupId: String
    lateinit var clientId: String
    var topic: String? = null
    var autoCommit: Boolean = true
    var autoOffsetReset: String = "latest"
    var pollMillSec: Long = 1000
    var pollRecords: Int = -1
}

open class KafkaBasicAuthMessageConsumerProperties : KafkaMessageConsumerProperties()
