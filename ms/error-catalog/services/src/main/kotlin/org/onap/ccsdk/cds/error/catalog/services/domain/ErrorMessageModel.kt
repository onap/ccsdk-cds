/*
 *  Copyright © 2020 IBM, Bell Canada.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.error.catalog.services.domain

import java.io.Serializable
import java.util.UUID
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/**
 * Provide Error Message Model Entity
 *
 * @author Steve Siani
 * @version 1.0
 */
@Entity
@Table(name = "ERROR_MESSAGES", uniqueConstraints = [UniqueConstraint(columnNames = ["message_id"])])
class ErrorMessageModel : Serializable {

    @Id
    var id: String = UUID.randomUUID().toString()

    @Column(name = "message_id", nullable = false)
    lateinit var messageID: String

    @Lob
    @Column(name = "cause", columnDefinition = "LONGTEXT")
    var cause: String = ""

    @Lob
    @Column(name = "action", columnDefinition = "LONGTEXT")
    lateinit var action: String

    @ManyToMany(mappedBy = "errorMessages", fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST])
    val domains: MutableSet<Domain> = mutableSetOf()

    companion object {

        private const val serialVersionUID = 1L
    }

    constructor()

    constructor(messageId: String, cause: String, action: String) {
        this.messageID = messageId
        this.cause = cause
        this.action = action
    }
}
