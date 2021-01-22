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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.service

import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationStateService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessageState
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.PrioritizationMessageRepository
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.toFormatedCorrelation
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.utils.addDate
import org.onap.ccsdk.cds.controllerblueprints.core.utils.controllerDate
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Date

@Service
open class MessagePrioritizationStateServiceImpl(
    private val prioritizationMessageRepository: PrioritizationMessageRepository
) : MessagePrioritizationStateService {

    private val log = logger(MessagePrioritizationStateServiceImpl::class)

    @Transactional
    override suspend fun saveMessage(message: MessagePrioritization): MessagePrioritization {
        if (!message.correlationId.isNullOrBlank()) {
            message.correlationId = message.toFormatedCorrelation()
        }
        message.updatedDate = Date()
        return prioritizationMessageRepository.save(message)
    }

    override suspend fun getMessage(id: String): MessagePrioritization {
        return prioritizationMessageRepository.findById(id).orElseGet(null)
            ?: throw BlueprintProcessorException("couldn't find message for id($id)")
    }

    override suspend fun getMessages(ids: List<String>): List<MessagePrioritization>? {
        return prioritizationMessageRepository.findAllById(ids)
    }

    override suspend fun getExpiryEligibleMessages(count: Int): List<MessagePrioritization>? {
        return prioritizationMessageRepository
            .findByStateInAndExpiredDate(
                arrayListOf(MessageState.NEW.name, MessageState.WAIT.name),
                Date(), PageRequest.of(0, count)
            )
    }

    override suspend fun getMessageForStatesNotExpiredIn(group: String, states: List<String>, count: Int):
        List<MessagePrioritization>? {
            return prioritizationMessageRepository.findByGroupAndStateInAndNotExpiredDate(
                group,
                states, Date(), PageRequest.of(0, count)
            )
        }

    override suspend fun getMessageForStatesExpired(group: String, states: List<String>, count: Int):
        List<MessagePrioritization>? {
            return prioritizationMessageRepository.findByGroupAndStateInAndExpiredDate(
                group,
                states, Date(), PageRequest.of(0, count)
            )
        }

    override suspend fun getExpiredMessages(expiryDate: Date, count: Int): List<MessagePrioritization>? {
        return prioritizationMessageRepository.findByExpiredDate(
            expiryDate, PageRequest.of(0, count)
        )
    }

    override suspend fun getExpiredMessages(group: String, expiryDate: Date, count: Int):
        List<MessagePrioritization>? {
            return prioritizationMessageRepository.findByGroupAndExpiredDate(
                group,
                expiryDate, PageRequest.of(0, count)
            )
        }

    override suspend fun getCorrelatedMessages(
        group: String,
        states: List<String>,
        types: List<String>?,
        correlationIds: String
    ): List<MessagePrioritization>? {
        return if (!types.isNullOrEmpty()) {
            prioritizationMessageRepository.findByGroupAndTypesAndCorrelationId(group, states, types, correlationIds)
        } else {
            prioritizationMessageRepository.findByGroupAndCorrelationId(group, states, correlationIds)
        }
    }

    @Transactional
    override suspend fun updateMessagesState(ids: List<String>, state: String) {
        ids.forEach {
            val updated = updateMessageState(it, state)
            log.info("message($it) update to state(${updated.state})")
        }
    }

    @Transactional
    override suspend fun setMessageState(id: String, state: String) {
        prioritizationMessageRepository.setStateForMessageId(id, state, Date())
    }

    @Transactional
    override suspend fun setMessagesPriority(ids: List<String>, priority: String) {
        prioritizationMessageRepository.setPriorityForMessageIds(ids, priority, Date())
    }

    @Transactional
    override suspend fun setMessagesState(ids: List<String>, state: String) {
        prioritizationMessageRepository.setStateForMessageIds(ids, state, Date())
    }

    @Transactional
    override suspend fun setMessageStateANdError(id: String, state: String, error: String) {
        prioritizationMessageRepository.setStateAndErrorForMessageId(id, state, error, Date())
    }

    @Transactional
    override suspend fun updateMessageState(id: String, state: String): MessagePrioritization {
        val updateMessage = getMessage(id).apply {
            this.updatedDate = Date()
            this.state = state
        }
        return saveMessage(updateMessage)
    }

    @Transactional
    override suspend fun setMessageStateAndAggregatedIds(id: String, state: String, aggregatedIds: List<String>) {
        val groupedIds = aggregatedIds.joinToString(",")
        prioritizationMessageRepository.setStateAndAggregatedMessageIds(id, state, groupedIds, Date())
    }

    override suspend fun deleteMessage(id: String) {
        prioritizationMessageRepository.deleteById(id)
        log.info("Prioritization Messages $id deleted successfully.")
    }

    override suspend fun deleteMessages(ids: List<String>) {
        prioritizationMessageRepository.deleteByIds(ids)
        log.info("Prioritization Messages $ids deleted successfully.")
    }

    override suspend fun deleteExpiredMessage(retentionDays: Int) {
        val expiryCheckDate = controllerDate().addDate(retentionDays)
        prioritizationMessageRepository.deleteByExpiryDate(expiryCheckDate)
    }

    override suspend fun deleteMessageByGroup(group: String) {
        prioritizationMessageRepository.deleteGroup(group)
        log.info("Prioritization Messages group($group) deleted successfully.")
    }

    override suspend fun deleteMessageStates(group: String, states: List<String>) {
        prioritizationMessageRepository.deleteGroupAndStateIn(group, states)
        log.info("Prioritization Messages group($group) with states($states) deleted successfully.")
    }
}
