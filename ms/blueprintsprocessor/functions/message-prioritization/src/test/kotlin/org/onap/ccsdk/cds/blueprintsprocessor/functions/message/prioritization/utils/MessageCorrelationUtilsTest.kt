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
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.orderByHighestPriority
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MessageCorrelationUtilsTest {

    @Test
    fun testCorrelationKeysReordered() {

        val message1 = MessagePrioritizationSample.createMessage(
            "sample-group", MessageState.NEW.name,
            "type-0", "key1=value1,key2=value2"
        )
        val message2 = MessagePrioritizationSample.createMessage(
            "sample-group", MessageState.NEW.name,
            "type-0", "key2=value2,key1=value1"
        )

        val multipleMessages: MutableList<MessagePrioritization> = arrayListOf()
        multipleMessages.add(message1)
        multipleMessages.add(message2)
        val multipleMessagesResponse = MessageCorrelationUtils.correlatedMessages(multipleMessages)
        assertTrue(multipleMessagesResponse.correlated, "failed in multipleMessages correlated keys reordered")
    }

    @Test
    fun differentTypesWithSameCorrelationMessages() {
        /** With Types **/
        /* Assumption is Same group with different types */
        val differentTypesWithSameCorrelationMessages = MessagePrioritizationSample
            .sampleMessageWithDifferentTypeSameCorrelation("sample-group", MessageState.NEW.name, 3)
        val differentTypesWithSameCorrelationMessagesResponse = MessageCorrelationUtils.correlatedMessagesWithTypes(
            differentTypesWithSameCorrelationMessages,
            arrayListOf("type-0", "type-1", "type-2")
        )
        assertTrue(
            differentTypesWithSameCorrelationMessagesResponse.correlated,
            "failed to correlate differentTypesWithSameCorrelationMessagesResponse"
        )

        /* Assumption is Same group with different types and one missing expected types,
        In this case type-3 message is missing */
        val differentTypesWithSameCorrelationMessagesResWithMissingType =
            MessageCorrelationUtils.correlatedMessagesWithTypes(
                differentTypesWithSameCorrelationMessages,
                arrayListOf("type-0", "type-1", "type-2", "type-3")
            )
        assertTrue(
            !differentTypesWithSameCorrelationMessagesResWithMissingType.correlated,
            "failed to correlate differentTypesWithSameCorrelationMessagesResWithMissingType"
        )
    }

    @Test
    fun withSameCorrelationMessagesWithIgnoredTypes() {
        /** With ignoring Types */
        /** Assumption is only one message received */
        val withSameCorrelationOneMessages = MessagePrioritizationSample
            .sampleMessageWithSameCorrelation("sample-group", MessageState.NEW.name, 1)
        val withSameCorrelationOneMessagesResp = MessageCorrelationUtils.correlatedMessagesWithTypes(
            withSameCorrelationOneMessages, null
        )
        assertTrue(
            !withSameCorrelationOneMessagesResp.correlated,
            "failed to correlate withSameCorrelationMessagesResp"
        )

        /** Assumption is two message received for same group with same correlation */
        val withSameCorrelationMessages = MessagePrioritizationSample
            .sampleMessageWithSameCorrelation("sample-group", MessageState.NEW.name, 2)
        val withSameCorrelationMessagesResp = MessageCorrelationUtils.correlatedMessagesWithTypes(
            withSameCorrelationMessages, null
        )
        assertTrue(
            withSameCorrelationMessagesResp.correlated,
            "failed to correlate withSameCorrelationMessagesResp"
        )
    }

    @Test
    fun differentTypesWithDifferentCorrelationMessage() {
        /** Assumption is two message received for same group with different expected types and different correlation */
        val message1 = MessagePrioritizationSample.createMessage(
            "sample-group", MessageState.NEW.name,
            "type-0", "key1=value1,key2=value2"
        )
        val message2 = MessagePrioritizationSample.createMessage(
            "sample-group", MessageState.NEW.name,
            "type-1", "key1=value1,key2=value3"
        )
        val differentTypesWithDifferentCorrelationMessage: MutableList<MessagePrioritization> = arrayListOf()
        differentTypesWithDifferentCorrelationMessage.add(message1)
        differentTypesWithDifferentCorrelationMessage.add(message2)
        val differentTypesWithDifferentCorrelationMessageResp = MessageCorrelationUtils.correlatedMessagesWithTypes(
            differentTypesWithDifferentCorrelationMessage,
            arrayListOf("type-0", "type-1")
        )
        assertTrue(
            !differentTypesWithDifferentCorrelationMessageResp.correlated,
            "failed to correlate differentTypesWithDifferentCorrelationMessageResp"
        )
    }

    @Test
    fun testPrioritizationOrdering() {
        val differentPriorityMessages = MessagePrioritizationSample
            .sampleMessageWithSameCorrelation("sample-group", MessageState.NEW.name, 5)
        val orderedPriorityMessages = differentPriorityMessages.orderByHighestPriority()
        assertNotNull(orderedPriorityMessages, "failed to order the priority messages")
    }
}
