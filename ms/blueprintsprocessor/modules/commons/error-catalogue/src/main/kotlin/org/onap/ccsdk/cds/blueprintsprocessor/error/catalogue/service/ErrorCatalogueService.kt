package org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.service

import org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.ErrorCatalogueProperties
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.ErrorMessageLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.data.ErrorModel
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.utils.ErrorCatalogueUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service(ErrorMessageLibConstants.SERVICE_ERROR_MESSAGE_LIB)
open class ErrorCatalogueService {
    @Autowired
    lateinit var errorCatalogueProperties: ErrorCatalogueProperties

    private var errorMessagesLibService: ErrorMessagesLibService = if (errorCatalogueProperties.type==ErrorMessageLibConstants.ERROR_MESSAGE_LIB_DB)
        ErrorMessagesLibDBService(errorCatalogueProperties)
    else
        ErrorMessagesLibPropertyService()

    /**
     * Exposed Dependency Service by this Error Message Lib Module
     */
    fun getErrorMessagesLibService(): ErrorMessagesLibService {
        return errorMessagesLibService
    }

    fun getErrorMessage(domain: String, key: String): String? {
        return errorMessagesLibService.getErrorMessage(domain, key)
    }

    fun getErrorMessage(attribute: String): String? {
        return errorMessagesLibService.getErrorMessage(attribute)
    }

    fun getErrorModel(errorId: String, domain: String): ErrorModel? {
        val errorMessage = getErrorMessage(domain, errorId) ?: return null
        return ErrorModel(errorId, domain, ErrorCatalogueUtils.readErrorCauseFromMessage(errorMessage), ErrorCatalogueUtils.readErrorActionFromMessage(errorMessage))
    }
}