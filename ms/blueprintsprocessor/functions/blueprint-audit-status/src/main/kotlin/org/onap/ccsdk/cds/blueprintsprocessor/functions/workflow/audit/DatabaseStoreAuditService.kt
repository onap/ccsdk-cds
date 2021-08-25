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

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit.db.BlueprintAuditStatusRepository
import org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit.db.BlueprintWorkflowAuditStatus
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.controllerDate
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.util.Date

/**
 * Workflow request and response details are persisted to database
 */
@ConditionalOnProperty(
    name = ["blueprintsprocessor.workflow.self-service-api.audit.storeEnable"],
    havingValue = "true"
)
@Service
class DatabaseStoreAuditService(
    private val blueprintAuditStatusRepository: BlueprintAuditStatusRepository
) : StoreAuditService {

    private val log =
        LoggerFactory.getLogger(DatabaseStoreAuditService::class.toString())

    /**
     * store the blueprint workflow input details to database
     * @param executionServiceInput {@link ExecutionServiceInput}
     * @throws {@link BluePrintException}
     */
    override suspend fun storeExecutionInput(
        executionServiceInput: ExecutionServiceInput
    ): Long {
        log.info(
            "storeExecutionInput called to store the Workflow action " +
                "input details "
        )
        var storedAuditStatus: BlueprintWorkflowAuditStatus = BlueprintWorkflowAuditStatus()

        storedAuditStatus = write(
            0, executionServiceInput.commonHeader.originatorId,
            executionServiceInput.commonHeader.requestId,
            executionServiceInput.commonHeader.subRequestId,
            executionServiceInput.actionIdentifiers.actionName,
            executionServiceInput.actionIdentifiers.blueprintName,
            executionServiceInput.actionIdentifiers.blueprintVersion,
            executionServiceInput.payload.toString(),
            DatabaseStoreAuditConstants.WORKFLOW_STATUS_INPROGRESS, controllerDate(),
            controllerDate(), controllerDate(),
            DatabaseStoreAuditConstants.WORKFLOW_STATUS_UPDATEDBY,
            executionServiceInput.actionIdentifiers.mode, ""
        )

        return storedAuditStatus.id
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
            "storeExecutionOutput called to store the Workflow action " +
                "output details correlationUUID $correlationUUID " +
                "auditStoreId $auditStoreId"
        )
        try {
            var storedAuditStatus: BlueprintWorkflowAuditStatus

            storedAuditStatus =
                blueprintAuditStatusRepository.findById(auditStoreId)
            if (storedAuditStatus == null) {
                throw BluePrintException("Record not found exception")
            }
            storedAuditStatus.endDate = controllerDate()
            storedAuditStatus.status = DatabaseStoreAuditConstants.WORKFLOW_STATUS_COMPLETED
            storedAuditStatus.updatedDate = controllerDate()
            ObjectMapper().writeValueAsString(executionServiceOutput.status)
            storedAuditStatus.workflowResponseContent = ObjectMapper()
                .writeValueAsString(executionServiceOutput)

            log.info(
                "Update the Audit status record Id ${storedAuditStatus.id}  " +
                    "bluePrintName ${storedAuditStatus.blueprintName}"
            )
            blueprintAuditStatusRepository.saveAndFlush(storedAuditStatus)
        } catch (ex: DataIntegrityViolationException) {
            log.error(
                "Error writing out BLUEPRINT_WORKFLOW_AUDIT_STATUS result: " +
                    "bpName:" +
                    " $auditStoreId" +
                    "correlationUUID $correlationUUID  error: {}",
                ex.message
            )
            throw BluePrintException("Failed to store resource api result.", ex)
        }
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
            "getWorkflowStatusByRequestId called to retrieve all the records " +
                "based on request Id"
        )

        var results: List<BlueprintWorkflowAuditStatus> =
            blueprintAuditStatusRepository.findByRequestId(requestId)
        log.info(
            "getWorkflowStatusByRequestId results count  " +
                "${results.size}"
        )
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
            "getWorkflowStatusByRequestIdAndSubRequestId called to retrieve all the records " +
                "based on request Id"
        )

        var results: List<BlueprintWorkflowAuditStatus> =
            blueprintAuditStatusRepository.findByRequestIdAndSubRequestId(requestId, subRequestId)
        log.info(
            "getWorkflowStatusByRequestIdAndSubRequestId results count  " +
                "${results.size}"
        )
        return results
    }

    /**
     * method to save input details to database
     */
    suspend fun write(
        id: Long,
        originatorId: String,
        requestId: String,
        subRequestId: String,
        workflowName: String,
        blueprintName: String,
        blueprintVersion: String,
        workflowTaskContent: String,
        status: String,
        startDate: Date,
        endDate: Date,
        updatedDate: Date,
        updatedBy: String,
        requestMode: String,
        workflowResponseContent: String
    ): BlueprintWorkflowAuditStatus =
        withContext(Dispatchers.IO) {

            val blueprintAuditStatusResult = BlueprintWorkflowAuditStatus()

            blueprintAuditStatusResult.originatorId = originatorId
            blueprintAuditStatusResult.requestId = requestId
            blueprintAuditStatusResult.subRequestId = subRequestId
            blueprintAuditStatusResult.workflowName = workflowName
            blueprintAuditStatusResult.blueprintName = blueprintName
            blueprintAuditStatusResult.blueprintVersion = blueprintVersion
            blueprintAuditStatusResult.workflowTaskContent = workflowTaskContent
            blueprintAuditStatusResult.status = status
            blueprintAuditStatusResult.startDate = startDate
            blueprintAuditStatusResult.endDate = endDate
            blueprintAuditStatusResult.updatedDate = updatedDate
            blueprintAuditStatusResult.updatedBy = updatedBy
            blueprintAuditStatusResult.requestMode = requestMode
            blueprintAuditStatusResult.workflowResponseContent =
                workflowResponseContent

            var storedAuditStatus: BlueprintWorkflowAuditStatus
            try {
                log.info(
                    "Writing out BLUEPRINT_AUDIT_STATUS result: bpName: " +
                        "$blueprintName bpVer $blueprintVersion " +
                        "id:$id" +
                        " (originatorId: $originatorId requestId: " +
                        "$requestId) subRequestId:$subRequestId"
                )
                storedAuditStatus = blueprintAuditStatusRepository.saveAndFlush(
                    blueprintAuditStatusResult
                )
            } catch (ex: DataIntegrityViolationException) {
                log.error(
                    "Error writing out BLUEPRINT_AUDIT_STATUS result: bpName:" +
                        " $blueprintName bpVer $blueprintVersion " +
                        "id:$id" +
                        " (originatorId: $originatorId requestId:" +
                        " $requestId) subRequestId:$subRequestId error: {}",
                    ex.message
                )
                throw BluePrintException(
                    "Failed to store resource api result.",
                    ex
                )
            }
            storedAuditStatus
        }
}
