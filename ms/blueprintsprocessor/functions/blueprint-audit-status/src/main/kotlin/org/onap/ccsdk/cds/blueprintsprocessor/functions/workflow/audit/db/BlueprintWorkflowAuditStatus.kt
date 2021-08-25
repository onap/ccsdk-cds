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
import io.swagger.annotations.ApiModelProperty
import org.hibernate.annotations.Proxy
import org.springframework.data.annotation.LastModifiedDate
import java.io.Serializable
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GenerationType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType

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

    @get:ApiModelProperty(value = "Workflow payload.", required = true)
    @Lob
    @Column(name = "workflow_task_content", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var workflowTaskContent: String

    @get:ApiModelProperty(value = "request originator Id", required = true)
    @Column(name = "originator_Id", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var originatorId: String

    @get:ApiModelProperty(value = "request Id", required = true)
    @Column(name = "request_Id", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var requestId: String

    @get:ApiModelProperty(value = "sub request Id", required = true)
    @Column(name = "subRequest_Id", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var subRequestId: String

    @get:ApiModelProperty(value = "workflow name", required = true)
    @Column(name = "workflow_name", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var workflowName: String

    @get:ApiModelProperty(value = "status", required = true)
    @Column(name = "status", nullable = true)
    @ApiModelProperty(required = true)
    lateinit var status: String

    @get:ApiModelProperty(
        value = "start time when request process started", required = true
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    var startDate: Date = Date()

    @get:ApiModelProperty(
        value = "end time when request process completed", required = true
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time")
    var endDate: Date = Date()

    @get:ApiModelProperty(value = "current date time", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_date")
    var updatedDate: Date = Date()

    @get:ApiModelProperty(value = "updated by", required = true)
    @Column(name = "updated_by", nullable = true)
    @ApiModelProperty(required = true)
    lateinit var updatedBy: String

    @get:ApiModelProperty(value = "blueprint version", required = true)
    @Column(name = "blueprint_version", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var blueprintVersion: String

    @get:ApiModelProperty(value = "blueprint name", required = true)
    @Column(name = "blueprint_name", nullable = false)
    @ApiModelProperty(required = true)
    lateinit var blueprintName: String

    @get:ApiModelProperty(value = "request mode", required = true)
    @Column(name = "request_mode", nullable = true)
    @ApiModelProperty(required = true)
    lateinit var requestMode: String

    @get:ApiModelProperty(value = "workflow response content", required = false)
    @Lob
    @Column(name = "workflow_response_content", nullable = true)
    @ApiModelProperty(required = false)
    lateinit var workflowResponseContent: String

    @get:ApiModelProperty(value = "bluprint model uuid", required = true)
    @Column(name = "blueprint_uuid", nullable = true)
    @ApiModelProperty(required = false)
    var blueprintUuid: String = ""
}
