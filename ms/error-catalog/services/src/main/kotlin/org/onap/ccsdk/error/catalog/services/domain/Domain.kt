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

package org.onap.ccsdk.error.catalog.services.domain

import java.io.Serializable
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.UniqueConstraint
import javax.persistence.Id
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Column
import javax.persistence.Lob
import javax.persistence.ManyToMany
import javax.persistence.FetchType
import javax.persistence.CascadeType
import javax.persistence.JoinTable
import javax.persistence.JoinColumn

/**
 *  Provide ErrorCode Entity
 *
 * @author Steve Siani
 * @version 1.0
 */

@Entity
@Table(name = "ERROR_DOMAINS", uniqueConstraints = [UniqueConstraint(columnNames = ["name", "application_id"])])
class Domain : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: UUID = UUID.randomUUID()

    @Column(name = "name")
    var name: String = ""

    @Column(name = "application_id")
    var applicationId: String = ""

    @Lob
    @Column(name = "description")
    var description: String = ""

    @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinTable(
        name = "ERROR_DOMAINS_ERROR_MESSAGES",
        joinColumns = [JoinColumn(name = "domain_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "message_id", referencedColumnName = "id")]
    )
    @Column(name = "error_msg")
    val errorMessages: MutableSet<ErrorMessageModel> = mutableSetOf()

    constructor()

    constructor(name: String, applicationId: String, description: String) {
        this.name = name
        this.description = description
        this.applicationId = applicationId
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
