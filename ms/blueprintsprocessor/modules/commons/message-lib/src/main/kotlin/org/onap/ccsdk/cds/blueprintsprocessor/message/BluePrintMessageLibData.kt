/*
 *  Copyright © 2019 IBM.
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


open class MessageProducerProperties
open class MessageConsumerProperties

open class KafkaBasicAuthMessageProducerProperties : MessageProducerProperties() {
    lateinit var bootstrapServers: String
    var topic: String? = null
    var clientId: String? = null
}

open class KafkaBasicAuthMessageConsumerProperties : MessageConsumerProperties() {
    lateinit var bootstrapServers: String
    var topic: String? = null
    var groupId: String? = null
    // TODO May need to add max.poll.records to increase number of messages
    //  read by kafka consumer, partition or consumer offsets.
}