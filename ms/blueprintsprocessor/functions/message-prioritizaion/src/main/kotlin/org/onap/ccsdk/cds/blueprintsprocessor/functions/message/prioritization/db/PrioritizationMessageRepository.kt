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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Date

@Repository
@Transactional(readOnly = true)
interface PrioritizationMessageRepository : JpaRepository<MessagePrioritization, String> {

    @Query("FROM MessagePrioritization pm WHERE pm.group = :group ORDER BY pm.createdDate asc")
    fun findByGroup(group: String, count: Pageable): List<MessagePrioritization>?

    @Query(
        "FROM MessagePrioritization pm WHERE pm.group = :group AND pm.state in :states " +
            "ORDER BY pm.createdDate asc"
    )
    fun findByGroupAndStateIn(group: String, states: List<String>, count: Pageable): List<MessagePrioritization>?

    @Query(
        "FROM MessagePrioritization pm WHERE pm.group = :group AND pm.state in :states " +
            "ORDER BY pm.updatedDate asc"
    )
    fun findByGroupAndStateInOrderByUpdatedDate(group: String, states: List<String>, count: Pageable):
        List<MessagePrioritization>?

    @Query(
        "FROM MessagePrioritization pm WHERE pm.group = :group AND pm.state in :states " +
            "AND pm.expiryDate > :expiryCheckDate ORDER BY pm.createdDate asc"
    )
    fun findByGroupAndStateInAndNotExpiredDate(
        group: String,
        states: List<String>,
        expiryCheckDate: Date,
        count: Pageable
    ): List<MessagePrioritization>?

    @Query(
        "FROM MessagePrioritization pm WHERE pm.state in :states " +
            "AND pm.expiryDate < :expiryCheckDate ORDER BY pm.createdDate asc"
    )
    fun findByStateInAndExpiredDate(
        states: List<String>,
        expiryCheckDate: Date,
        count: Pageable
    ): List<MessagePrioritization>?

    @Query(
        "FROM MessagePrioritization pm WHERE pm.group = :group AND pm.state in :states " +
            "AND pm.expiryDate < :expiryCheckDate ORDER BY pm.createdDate asc"
    )
    fun findByGroupAndStateInAndExpiredDate(
        group: String,
        states: List<String>,
        expiryCheckDate: Date,
        count: Pageable
    ): List<MessagePrioritization>?

    @Query(
        "FROM MessagePrioritization pm WHERE pm.group = :group " +
            "AND pm.expiryDate < :expiryCheckDate ORDER BY pm.createdDate asc"
    )
    fun findByGroupAndExpiredDate(group: String, expiryCheckDate: Date, count: Pageable): List<MessagePrioritization>?

    @Query(
        "FROM MessagePrioritization pm WHERE  pm.expiryDate < :expiryCheckDate ORDER BY pm.createdDate asc"
    )
    fun findByExpiredDate(expiryCheckDate: Date, count: Pageable): List<MessagePrioritization>?

    @Query(
        "FROM MessagePrioritization pm WHERE pm.group = :group AND pm.state in :states " +
            "AND pm.correlationId = :correlationId ORDER BY pm.createdDate asc"
    )
    fun findByGroupAndCorrelationId(group: String, states: List<String>, correlationId: String):
        List<MessagePrioritization>?

    @Query(
        "FROM MessagePrioritization pm WHERE pm.group = :group AND pm.state in :states " +
            "AND pm.type in :types AND pm.correlationId = :correlationId ORDER BY pm.createdDate asc"
    )
    fun findByGroupAndTypesAndCorrelationId(
        group: String,
        states: List<String>,
        types: List<String>,
        correlationId: String
    ): List<MessagePrioritization>?

    @Modifying
    @Transactional
    @Query(
        "UPDATE MessagePrioritization SET state = :state, updatedDate = :currentDate " +
            "WHERE id = :id"
    )
    fun setStateForMessageId(id: String, state: String, currentDate: Date): Int

    @Modifying
    @Transactional
    @Query(
        "UPDATE MessagePrioritization SET priority = :priority, updatedDate = :currentDate " +
            "WHERE id = :id"
    )
    fun setPriorityForMessageId(id: String, priority: String, currentDate: Date): Int

    @Modifying
    @Transactional
    @Query(
        "UPDATE MessagePrioritization SET state = :state, updatedDate = :currentDate " +
            "WHERE id IN :ids"
    )
    fun setStateForMessageIds(ids: List<String>, state: String, currentDate: Date): Int

    @Modifying
    @Transactional
    @Query(
        "UPDATE MessagePrioritization SET priority = :priority, updatedDate = :currentDate " +
            "WHERE id IN :ids"
    )
    fun setPriorityForMessageIds(ids: List<String>, priority: String, currentDate: Date): Int

    @Modifying
    @Transactional
    @Query(
        "UPDATE MessagePrioritization SET state = :state, error = :error, updatedDate = :currentDate " +
            "WHERE id = :id"
    )
    fun setStateAndErrorForMessageId(id: String, state: String, error: String, currentDate: Date): Int

    @Modifying
    @Transactional
    @Query(
        "UPDATE MessagePrioritization SET state = :state, " +
            "aggregatedMessageIds = :aggregatedMessageIds, updatedDate = :currentDate WHERE id = :id"
    )
    fun setStateAndAggregatedMessageIds(id: String, state: String, aggregatedMessageIds: String, currentDate: Date): Int

    @Modifying
    @Transactional
    @Query("DELETE FROM MessagePrioritization WHERE id IN :ids")
    fun deleteByIds(ids: List<String>)

    @Modifying
    @Transactional
    @Query("DELETE FROM MessagePrioritization WHERE expiryDate > :expiryCheckDate ")
    fun deleteByExpiryDate(expiryCheckDate: Date)

    @Modifying
    @Transactional
    @Query("DELETE FROM MessagePrioritization WHERE group = :group")
    fun deleteGroup(group: String)

    @Modifying
    @Transactional
    @Query("DELETE FROM MessagePrioritization WHERE group = :group AND state IN :states")
    fun deleteGroupAndStateIn(group: String, states: List<String>)
}
