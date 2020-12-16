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

import com.fasterxml.jackson.annotation.JsonFormat
import org.hibernate.annotations.Proxy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType

@EnableJpaAuditing
@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "MESSAGE_PRIORITIZATION")
@Proxy(lazy = false)
open class MessagePrioritization {

    @Id
    @Column(name = "message_id", length = 50)
    lateinit var id: String

    @Column(name = "message_group", length = 50, nullable = false)
    lateinit var group: String

    @Column(name = "message_type", length = 50, nullable = false)
    lateinit var type: String

    /** States Defined by MessageState */
    @Column(name = "message_state", length = 20, nullable = false)
    lateinit var state: String

    @Column(name = "priority", nullable = false)
    var priority: Int = 5

    @Lob
    @Column(name = "message", nullable = false)
    var message: String? = null

    @Lob
    @Column(name = "error", nullable = true)
    var error: String? = null

    @Lob
    @Column(name = "aggregated_message_ids", nullable = true)
    var aggregatedMessageIds: String? = null

    @Lob
    @Column(name = "correlation_id", nullable = true)
    var correlationId: String? = null

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    var createdDate = Date()

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_date", nullable = false)
    var updatedDate: Date? = null

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiry_date", nullable = false)
    var expiryDate: Date? = null
}
