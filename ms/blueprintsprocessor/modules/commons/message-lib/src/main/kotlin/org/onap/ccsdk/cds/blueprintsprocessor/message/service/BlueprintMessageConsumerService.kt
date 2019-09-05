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

package org.onap.ccsdk.cds.blueprintsprocessor.message.service

import kotlinx.coroutines.channels.Channel

interface BlueprintMessageConsumerService {

    /** Subscribe to the Kafka channel with [additionalConfig] */
    suspend fun subscribe(additionalConfig: Map<String, Any>?): Channel<String>

    /** Subscribe to the Kafka channel with [additionalConfig] for dynamic [topics]*/
    suspend fun subscribe(topics: List<String>, additionalConfig: Map<String, Any>? = null): Channel<String>

    /** close the channel, consumer and other resources */
    suspend fun shutDown()

}