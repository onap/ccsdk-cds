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

import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessageState
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import kotlin.test.assertTrue

class MessageCorrelationUtilsTest {

    @Test
    fun testMatchingCorrelation() {
        val multipleMessages: MutableList<MessagePrioritization> = arrayListOf()
        multipleMessages.addAll(sampleWithCorrelation("type1", "id=1234,hostname=one", 1))
        multipleMessages.addAll(sampleWithCorrelation("type1", "hostname=one,id=1234", 1))
        val multipleMessagesResponse = MessageCorrelationUtils.correlatedMessages(multipleMessages)
        assertTrue(multipleMessagesResponse.correlated, "failed in multipleMessages correlate")


        val correlatedMessagesWithTypes: MutableList<MessagePrioritization> = arrayListOf()
        correlatedMessagesWithTypes.addAll(sampleWithCorrelation("type2", "id=1234,hostname=one", 1))
        correlatedMessagesWithTypes.addAll(sampleWithCorrelation("type3", "hostname=one,id=1234", 1))
        val types: List<String> = arrayListOf("type2", "type3")
        val correlatedMessagesWithTypesResponse = MessageCorrelationUtils
                .correlatedMessagesWithTypes(correlatedMessagesWithTypes, types)
        assertTrue(correlatedMessagesWithTypesResponse.correlated, "failed in correlatedMessagesWithTypes correlate")

        val checkMissingTypes: List<String> = arrayListOf("type2", "type3", "type4")
        val checkMissingTypesResponse = MessageCorrelationUtils.correlatedMessagesWithTypes(correlatedMessagesWithTypes, checkMissingTypes)
        assertTrue(!checkMissingTypesResponse.correlated, "failed in checkMissingTypes correlate")
    }

    private fun sampleWithCorrelation(type: String, correlationId: String?, count: Int): List<MessagePrioritization> {
        return MessagePrioritizationSample.sampleMessages(MessageState.NEW.name, count)
                .map {
                    it.type = type
                    it.correlationId = correlationId
                    it
                }
    }
}