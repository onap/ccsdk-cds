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

package org.onap.ccsdk.error.catalog.service

import org.onap.ccsdk.error.catalog.domain.Domain
import org.onap.ccsdk.error.catalog.domain.ErrorMessageModel
import org.onap.ccsdk.error.catalog.repository.DomainRepository
import org.onap.ccsdk.error.catalog.repository.ErrorMessageModelRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
open class ErrorCatalogDatabaseHandler {
    @Autowired
    lateinit var domainRepository: DomainRepository
    @Autowired
    lateinit var errorMessageModelRepository: ErrorMessageModelRepository

    /**
     * This is a getAllDomains method to retrieve all the Domain in Error Catalog Database by pages
     *
     * @return Page<Domain> list of the domains by page
    </Domain> */
    open fun getAllDomains(pageRequest: Pageable): Page<Domain> {
        return domainRepository.findAll(pageRequest)
    }

    /**
     * This is a getAllDomains method to retrieve all the Domain in Error Catalog Database
     *
     * @return List<Domain> list of the domains
    </Domain> */
    open fun getAllDomains(): List<Domain> {
        return domainRepository.findAll()
    }

    /**
     * This is a getAllDomainsByApplication method to retrieve all the Domain that belong to an application in Error Catalog Database
     *
     * @return List<Domain> list of the domains
    </Domain> */
    open fun getAllDomainsByApplication(applicationId: String): List<Domain> {
        return domainRepository.findAllByApplicationId(applicationId)
    }

    /**
     * This is a getAllErrorMessagesByApplication method to retrieve all the Messages that belong to an application in Error Catalog Database
     *
     * @return MutableMap<String, ErrorCode> list of the abstractErrorModel
    </Domain> */
    open fun getAllErrorMessagesByApplication(applicationId: String): MutableMap<String, ErrorMessageModel> {
        val domains = domainRepository.findAllByApplicationId(applicationId)
        val errorMessages = mutableMapOf<String, ErrorMessageModel>()
        for (domain in domains){
            val errorMessagesFound = errorMessageModelRepository.findByDomainsId(domain.id)
            for (errorMessageFound in errorMessagesFound) {
                errorMessages[(domain.name).plus(MESSAGE_KEY_SEPARATOR.plus(errorMessageFound.messageID))] = errorMessageFound
            }
        }
        return errorMessages
    }

    open fun saveDomain(domain: String, applicationId: String, description: String = "", errorMessageList: List<ErrorMessageModel>) {
        val domainModel = Domain(domain, applicationId, description)
        domainModel.errorMessages.addAll(errorMessageList)
        domainRepository.saveAndFlush(domainModel)
    }

    companion object {
        private const val MESSAGE_KEY_SEPARATOR = "."
    }
}