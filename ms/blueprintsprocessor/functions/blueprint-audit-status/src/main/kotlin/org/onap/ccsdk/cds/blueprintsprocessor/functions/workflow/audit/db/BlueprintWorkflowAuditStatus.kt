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

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.Length
import org.hibernate.annotations.Proxy
import org.springframework.data.annotation.LastModifiedDate
import java.io.Serializable
import java.util.Date
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GenerationType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType

/**
 * BlueprintWorkflowAuditStatus Model.
 * Records stored and retrieved in table BLUEPRINT_WORKFLOW_AUDIT_STATUS is
 * done through this entity.
 */
@Entity
@Table(name = "BLUEPRINT_WORKFLOW_AUDIT_STATUS")
@Proxy(lazy = false)
class BlueprintWorkflowAuditStatus : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workflow_audit_id")
    var id: Long = 0

    @get:Schema(description = "Workflow payload.", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "workflow_task_content", nullable = false, length = Length.LONG32)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    lateinit var workflowTaskContent: String

    @get:Schema(description = "request originator Id", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "originator_Id", nullable = false)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    lateinit var originatorId: String

    @get:Schema(description = "request Id", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "request_Id", nullable = false)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    lateinit var requestId: String

    @get:Schema(description = "sub request Id", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "subRequest_Id", nullable = false)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    lateinit var subRequestId: String

    @get:Schema(description = "workflow name", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "workflow_name", nullable = false)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    lateinit var workflowName: String

    @get:Schema(description = "status", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "status", nullable = true)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    lateinit var status: String

    @get:Schema(
        description = "start time when request process started",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    var startDate: Date = Date()

    @get:Schema(
        description = "end time when request process completed",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time")
    var endDate: Date = Date()

    @get:Schema(description = "current date time", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_date")
    var updatedDate: Date = Date()

    @get:Schema(description = "updated by", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "updated_by", nullable = true)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    lateinit var updatedBy: String

    @get:Schema(description = "blueprint version", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "blueprint_version", nullable = false)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    lateinit var blueprintVersion: String

    @get:Schema(description = "blueprint name", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "blueprint_name", nullable = false)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    lateinit var blueprintName: String

    @get:Schema(description = "request mode", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "request_mode", nullable = true)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    lateinit var requestMode: String

    @get:Schema(description = "workflow response content", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Column(name = "workflow_response_content", nullable = true, length = Length.LONG32)
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    lateinit var workflowResponseContent: String

    @get:Schema(description = "bluprint model uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "blueprint_uuid", nullable = true)
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    var blueprintUuid: String = ""
}
