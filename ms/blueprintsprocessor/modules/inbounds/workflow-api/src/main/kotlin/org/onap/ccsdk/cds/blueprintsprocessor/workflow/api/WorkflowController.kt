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

package org.onap.ccsdk.cds.blueprintsprocessor.workflow.api

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit.db.BlueprintWorkflowAuditStatus
import org.onap.ccsdk.cds.blueprintsprocessor.functions.workflow.audit.StoreAuditService
import org.onap.ccsdk.cds.controllerblueprints.core.httpProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogCodes
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * Rest API which handles retrieving the workflow store records
 */
@RestController
@RequestMapping("/api/v1/workflow")
@Tag(
    name = "/api/v1/workflow",
    description = "Interaction with blueprint workflow status resources"
)
class WorkflowController {
    private val log =
        LoggerFactory.getLogger(WorkflowController::class.toString())

    @Autowired
    lateinit var databaseStoreAuditService: StoreAuditService

    private val JSON_MIME_TYPE = "application/json"

    /**
     * workflow healthcheck api
     */
    @RequestMapping(
        path = ["/health-check"],
        method = [RequestMethod.GET],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    @Operation(summary = "Health Check", hidden = true)
    fun workflowControllerHealthCheck(): JsonNode = runBlocking {
        JacksonUtils.getJsonNode("Success")
    }

    /**
     * API to retreive records based on request and subrequest ID     *
     */
    @GetMapping(
        "/audit-status/{requestId}/{subRequestId}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Get all workflow status records using request and Subrequest",
        description = "Retrieve all stored workflow audit records based on " +
            "requestId and SubrequestId"
    )
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    suspend fun getWorkFlowByRequestAndSubRequest(
        @Parameter(description = "request ID", required = true)
        @PathVariable(value = "requestId") requestId: String,
        @Parameter(description = "sub request ID", required = true)
        @PathVariable(value = "subRequestId") subRequestId: String
    ): ResponseEntity<List<BlueprintWorkflowAuditStatus>> = runBlocking {
        var requestIdResults: List<BlueprintWorkflowAuditStatus>
        if (requestId.isNotEmpty()) {
            requestIdResults = databaseStoreAuditService
                .getWorkflowStatusByRequestIdAndSubRequestId(requestId, subRequestId)
        } else {
            throw httpProcessorException(
                ErrorCatalogCodes.REQUEST_NOT_FOUND, WorkflowApiDomains.WORKFLOW_API,
                "Missing param."
            )
        }
        val expectedMediaType: MediaType = MediaType.valueOf(JSON_MIME_TYPE)
        ResponseEntity.ok().contentType(expectedMediaType).body(requestIdResults)
    }
}
