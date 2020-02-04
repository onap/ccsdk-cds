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

package org.onap.ccsdk.error.catalog.domain

import java.io.Serializable
import javax.persistence.*


/**
 * Provide Error Message Model Entity
 *
 * @author Steve Siani
 * @version 1.0
 */
@Entity
@Table(name = "error_messages", uniqueConstraints = [UniqueConstraint(columnNames = ["message_id", "cause", "action"])])
class ErrorMessage: Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 1L

    @Column(name = "message_id", nullable = false) var messageID: String = ""

    @Lob
    @Column(name = "cause") var cause: String = ""

    @Lob
    @Column(name = "action") var action: String = ""

    @ManyToMany(mappedBy = "errorMessages", fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST])
    val domains: MutableSet<Domain> = mutableSetOf()

    companion object {
        private const val serialVersionUID = 1L
    }

    constructor()

    constructor(messageId: String, cause: String, action: String){
        this.messageID =messageId
        this.cause = cause
        this.action = action
    }
}
