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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit.db.BlueprintWorkflowAuditStatus

/**
 * Workflow request and response details are persisted to database
 */

interface StoreAuditService {

    /**
     * store the blueprint workflow input details to database
     * @param executionServiceInput {@link ExecutionServiceInput}
     * @throws {@link BluePrintException}
     */
    suspend fun storeExecutionInput(
        executionServiceInput: ExecutionServiceInput
    ): Long

    /**
     * store the blueprint workflow output to database
     * @param auditStoreId
     * @param correlationUUID
     * @param executionServiceOutput {@link ExecutionServiceOutput}
     */
    suspend fun storeExecutionOutput(
        auditStoreId: Long,
        correlationUUID: String,
        executionServiceOutput: ExecutionServiceOutput
    )

    /**
     * retrive workflow records based on request ID
     * @param requestId
     * @return list of {@link BlueprintWorkflowAuditStatus}
     */
    suspend fun getWorkflowStatusByRequestId(
        requestId: String
    ): List<BlueprintWorkflowAuditStatus>

    /**
     * Retrive workflow records based on request ID and sub request ID
     * @param requestId
     * @param subRequestId
     * @return list of {@link BlueprintWorkflowAuditStatus}
     */
    suspend fun getWorkflowStatusByRequestIdAndSubRequestId(
        requestId: String,
        subRequestId: String
    ): List<BlueprintWorkflowAuditStatus>
}
