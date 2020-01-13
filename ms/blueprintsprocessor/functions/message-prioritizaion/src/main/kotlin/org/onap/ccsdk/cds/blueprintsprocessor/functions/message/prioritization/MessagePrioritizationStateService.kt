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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization

import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import java.util.Date

interface MessagePrioritizationStateService {

    suspend fun saveMessage(message: MessagePrioritization): MessagePrioritization

    suspend fun getMessage(id: String): MessagePrioritization

    suspend fun getMessages(ids: List<String>): List<MessagePrioritization>?

    suspend fun getExpiryEligibleMessages(count: Int): List<MessagePrioritization>?

    suspend fun getMessageForStatesNotExpiredIn(group: String, states: List<String>, count: Int):
        List<MessagePrioritization>?

    suspend fun getMessageForStatesExpired(group: String, states: List<String>, count: Int):
        List<MessagePrioritization>?

    suspend fun getExpiredMessages(expiryDate: Date, count: Int): List<MessagePrioritization>?

    suspend fun getExpiredMessages(group: String, expiryDate: Date, count: Int): List<MessagePrioritization>?

    suspend fun getCorrelatedMessages(
        group: String,
        states: List<String>,
        types: List<String>?,
        correlationIds: String
    ): List<MessagePrioritization>?

    suspend fun updateMessagesState(ids: List<String>, state: String)

    suspend fun updateMessageState(id: String, state: String): MessagePrioritization

    suspend fun setMessageState(id: String, state: String)

    suspend fun setMessagesPriority(ids: List<String>, priority: String)

    suspend fun setMessagesState(ids: List<String>, state: String)

    suspend fun setMessageStateANdError(id: String, state: String, error: String)

    suspend fun setMessageStateAndAggregatedIds(id: String, state: String, aggregatedIds: List<String>)

    suspend fun deleteMessage(id: String)

    suspend fun deleteMessages(id: List<String>)

    suspend fun deleteExpiredMessage(retentionDays: Int)

    suspend fun deleteMessageByGroup(group: String)

    suspend fun deleteMessageStates(group: String, states: List<String>)
}
