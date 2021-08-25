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
import org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit.db.BlueprintAuditStatusRepository
import org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit.db.BlueprintWorkflowAuditStatus
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

/**
 * Workflow request and response details are persisted to database
 */
@ConditionalOnProperty(
    name = ["blueprintsprocessor.workflow.self-service-api.audit.storeEnable"],
    havingValue = "false"
)
@Service
class NoStoreAuditService(
    private val blueprintAuditStatusRepository: BlueprintAuditStatusRepository
) : StoreAuditService {

    private val log =
        LoggerFactory.getLogger(NoStoreAuditService::class.toString())

    @PostConstruct
    fun init() {
        log.info("Workflow Audit store is disabled")
    }
    /**
     * store the blueprint workflow input details to database
     * @param executionServiceInput {@link ExecutionServiceInput}
     * @throws {@link BluePrintException}
     */
    override suspend fun storeExecutionInput(
        executionServiceInput: ExecutionServiceInput
    ): Long {
        log.info(
            "storeExecutionInput called not to store the Workflow action " +
                "input details "
        )
        val resturnId: Long = -1
        return resturnId
    }

    /**
     * store the blueprint workflow output to database
     * @param auditStoreId
     * @param correlationUUID
     * @param executionServiceOutput {@link ExecutionServiceOutput}
     * @throws {@link BluePrintException}
     */
    override suspend fun storeExecutionOutput(
        auditStoreId: Long,
        correlationUUID: String,
        executionServiceOutput: ExecutionServiceOutput
    ) {
        log.info(
            "storeExecutionOutput called not to store the Workflow action " +
                "output details correlationUUID $correlationUUID " +
                "auditStoreId $auditStoreId"
        )
    }

    /**
     * retrive workflow records based on request ID
     * @param requestId
     * @return list of {@link BlueprintWorkflowAuditStatus}
     */
    override suspend fun getWorkflowStatusByRequestId(
        requestId: String
    ): List<BlueprintWorkflowAuditStatus> {
        log.info(
            "getWorkflowStatusByRequestId placeholer , this doesn't return " +
                "any records"
        )

        var results: List<BlueprintWorkflowAuditStatus> = ArrayList<BlueprintWorkflowAuditStatus>()
        return results
    }

    /**
     * Retrive workflow records based on request ID and sub request ID
     * @param requestId
     * @param subRequestId
     * @return list of {@link BlueprintWorkflowAuditStatus}
     */
    override suspend fun getWorkflowStatusByRequestIdAndSubRequestId(
        requestId: String,
        subRequestId: String
    ): List<BlueprintWorkflowAuditStatus> {
        log.info(
            "getWorkflowStatusByRequestIdAndSubRequestId  placeholer , this doesn't return  " +
                "any records"
        )

        var results: List<BlueprintWorkflowAuditStatus> = ArrayList<BlueprintWorkflowAuditStatus>()

        return results
    }
}
