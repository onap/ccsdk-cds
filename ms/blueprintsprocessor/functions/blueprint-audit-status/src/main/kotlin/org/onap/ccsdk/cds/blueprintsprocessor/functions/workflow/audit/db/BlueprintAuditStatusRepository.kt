/*
 * Copyright © 2021 Aarna Networks, Inc.
 *           All rights reserved.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

/**
 * JPA repository managing {@link BlueprintWorkflowAuditStatus} table.
 */
@Repository
interface BlueprintAuditStatusRepository :
    JpaRepository<BlueprintWorkflowAuditStatus, String> {

    /**
     * retireve records based on primary key ID.
     * @param id
     * @return {@link BlueprintWorkflowAuditStatus}
     */
    fun findById(id: Long): BlueprintWorkflowAuditStatus

    /**
     * retrieve records based on request ID
     * @param requestId
     * @return list {@link BlueprintWorkflowAuditStatus}
     */
    fun findByRequestId(
        requestId: String
    ): List<BlueprintWorkflowAuditStatus>

    /**
     * retrieve records based on request ID and subrequest ID
     * @param requestId
     * @param subRequestId
     * @return list {@link BlueprintWorkflowAuditStatus}
     */
    fun findByRequestIdAndSubRequestId(
        requestId: String,
        subRequestId: String
    ): List<BlueprintWorkflowAuditStatus>

    /**
     * retrieve records based on request id, blueprint name , blueprint version
     * @param requestId
     * @param blueprintName
     * @param blueprintVersion
     * @return {@link BlueprintWorkflowAuditStatus}
     */
    fun findByRequestIdAndBlueprintNameAndBlueprintVersion(
        requestId: String,
        blueprintName: String?,
        blueprintVersion: String?
    ): BlueprintWorkflowAuditStatus

    /**
     * retrieve records based on request id, blueprint name , blueprint version
     * @return {@link BlueprintWorkflowAuditStatus}
     */
    fun findByOriginatorIdAndRequestIdAndSubRequestIdAndWorkflowNameAndBlueprintNameAndBlueprintVersion(
        originatorId: String,
        requestId: String?,
        subRequestId: String?,
        workflowName: String,
        blueprintName: String?,
        blueprintVersion: String?
    ): BlueprintWorkflowAuditStatus

    /**
     * retrieve records based on request id, subrequest, originator, workflow,
     * blueprint version, blueprint Name
     * @return {@link BlueprintWorkflowAuditStatus}
     */
    fun findByIdAndOriginatorIdAndRequestIdAndSubRequestIdAndWorkflowNameAndBlueprintNameAndBlueprintVersion(
        id: Long,
        originatorId: String,
        requestId: String?,
        subRequestId: String?,
        workflowName: String,
        blueprintName: String?,
        blueprintVersion: String?
    ): BlueprintWorkflowAuditStatus

    @Transactional
    fun deleteByIdAndBlueprintNameAndBlueprintVersionAndOriginatorIdAndRequestIdAndSubRequestId(
        id: Long,
        blueprintName: String?,
        blueprintVersion: String?,
        originatorId: String,
        requestId: String?,
        subRequestId: String?
    )
}
