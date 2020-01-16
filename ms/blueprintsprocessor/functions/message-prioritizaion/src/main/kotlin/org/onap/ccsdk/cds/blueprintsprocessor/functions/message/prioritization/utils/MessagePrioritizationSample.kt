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

import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.CleanConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.ExpiryConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.KafkaConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.NatsConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.PrioritizationConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.ShutDownConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.controllerblueprints.core.utils.addDate
import org.onap.ccsdk.cds.controllerblueprints.core.utils.controllerDate
import java.util.Date
import java.util.UUID

object MessagePrioritizationSample {

    fun samplePrioritizationConfiguration(): PrioritizationConfiguration {
        return PrioritizationConfiguration().apply {
            kafkaConfiguration = KafkaConfiguration().apply {
                inputTopicSelector = "prioritize-input"
                outputTopic = "prioritize-output-topic"
                expiredTopic = "prioritize-expired-topic"
            }
            natsConfiguration = NatsConfiguration().apply {
                connectionSelector = "cds-controller"
                inputSubject = "prioritize-input"
                outputSubject = "prioritize-output"
                expiredSubject = "prioritize-expired"
            }
            expiryConfiguration = ExpiryConfiguration().apply {
                frequencyMilli = 10000L
                maxPollRecord = 2000
            }
            shutDownConfiguration = ShutDownConfiguration().apply {
                waitMill = 2000L
            }
            cleanConfiguration = CleanConfiguration().apply {
                frequencyMilli = 10000L
                expiredRecordsHoldDays = 5
            }
        }
    }

    fun sampleSchedulerPrioritizationConfiguration(): PrioritizationConfiguration {
        return PrioritizationConfiguration().apply {
            expiryConfiguration = ExpiryConfiguration().apply {
                frequencyMilli = 10L
                maxPollRecord = 2000
            }
            shutDownConfiguration = ShutDownConfiguration().apply {
                waitMill = 20L
            }
            cleanConfiguration = CleanConfiguration().apply {
                frequencyMilli = 10L
                expiredRecordsHoldDays = 5
            }
        }
    }

    private fun currentDatePlusDays(days: Int): Date {
        return controllerDate().addDate(days)
    }

    fun sampleMessages(messageState: String, count: Int): List<MessagePrioritization> {
        return sampleMessages("sample-group", messageState, count)
    }

    fun sampleMessages(groupName: String, messageState: String, count: Int): List<MessagePrioritization> {
        val messages: MutableList<MessagePrioritization> = arrayListOf()
        repeat(count) {
            val backPressureMessage = createMessage(
                groupName, messageState,
                "sample-type", null
            )
            messages.add(backPressureMessage)
        }
        return messages
    }

    fun sampleMessageWithSameCorrelation(
        groupName: String,
        messageState: String,
        count: Int
    ): List<MessagePrioritization> {
        val messages: MutableList<MessagePrioritization> = arrayListOf()
        repeat(count) {
            val backPressureMessage = createMessage(
                groupName, messageState, "sample-type",
                "key1=value1,key2=value2"
            )
            messages.add(backPressureMessage)
        }
        return messages
    }

    fun sampleMessageWithDifferentTypeSameCorrelation(
        groupName: String,
        messageState: String,
        count: Int
    ): List<MessagePrioritization> {
        val messages: MutableList<MessagePrioritization> = arrayListOf()
        repeat(count) {
            val backPressureMessage = createMessage(
                groupName, messageState, "type-$it",
                "key1=value1,key2=value2"
            )
            messages.add(backPressureMessage)
        }
        return messages
    }

    fun createMessage(
        groupName: String,
        messageState: String,
        messageType: String,
        messageCorrelationId: String?
    ): MessagePrioritization {

        return MessagePrioritization().apply {
            id = UUID.randomUUID().toString()
            group = groupName
            type = messageType
            state = messageState
            priority = (1..10).shuffled().first()
            correlationId = messageCorrelationId
            message = "I am the Message"
            createdDate = Date()
            updatedDate = Date()
            expiryDate = currentDatePlusDays(3)
        }
    }
}
