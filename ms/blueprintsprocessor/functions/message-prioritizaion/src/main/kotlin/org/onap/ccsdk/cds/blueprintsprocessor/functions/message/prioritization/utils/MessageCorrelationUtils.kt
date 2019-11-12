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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.utils

import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.CorrelationCheckResponse
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.toFormatedCorrelation
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.toTypeNCorrelation

object MessageCorrelationUtils {

    /** Assumption is message is of same group **/
    fun correlatedMessages(collectedMessages: List<MessagePrioritization>): CorrelationCheckResponse {
        val correlationCheckResponse = CorrelationCheckResponse(message = "not correlated")
        val filteredMessage = collectedMessages.filter { !it.correlationId.isNullOrBlank() }
        if (filteredMessage.isNotEmpty()) {
            val groupedMessage = filteredMessage.groupBy { it.toFormatedCorrelation() }
            if (groupedMessage.size == 1) {
                correlationCheckResponse.correlated = true
                correlationCheckResponse.message = null
            }
        }
        return correlationCheckResponse
    }

    /** Assumption is message is of same group and checking for required types **/
    fun correlatedMessagesWithTypes(collectedMessages: List<MessagePrioritization>, types: List<String>?)
            : CorrelationCheckResponse {
        return if (!types.isNullOrEmpty()) {
            val copyTypes = types!!.toTypedArray().copyOf().toMutableList()

            val filteredMessage = collectedMessages.filter {
                !it.correlationId.isNullOrBlank()
                        && types.contains(it.type)
            }
            if (filteredMessage.isNotEmpty()) {
                val correlatedMap = filteredMessage.groupBy { it.toTypeNCorrelation() }
                val foundType = correlatedMap.keys.map { it.type }
                copyTypes.removeAll(foundType)
            }

            return if (copyTypes.isNotEmpty()) {
                CorrelationCheckResponse(message = "couldn't find types($copyTypes)")
            } else {
                CorrelationCheckResponse(correlated = true)
            }
        } else {
            return correlatedMessages(collectedMessages)
        }
    }
}