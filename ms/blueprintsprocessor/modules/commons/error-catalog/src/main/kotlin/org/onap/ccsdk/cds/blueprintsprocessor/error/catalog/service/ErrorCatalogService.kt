package org.onap.ccsdk.cds.blueprintsprocessor.error.catalog.service

import org.onap.ccsdk.cds.blueprintsprocessor.error.catalog.ErrorCatalogProperties
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalog.ErrorMessageLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalog.data.ErrorModel
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalog.utils.ErrorCatalogUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service(ErrorMessageLibConstants.SERVICE_ERROR_MESSAGE_LIB)
open class ErrorCatalogService {
    @Autowired
    lateinit var errorCatalogProperties: ErrorCatalogProperties

    private var errorMessagesLibService: ErrorMessagesLibService = if (errorCatalogProperties.type == ErrorMessageLibConstants.ERROR_MESSAGE_LIB_DB)
        ErrorMessagesLibDBService(errorCatalogProperties)
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
        return ErrorModel(errorId, domain, ErrorCatalogUtils.readErrorCauseFromMessage(errorMessage), ErrorCatalogUtils.readErrorActionFromMessage(errorMessage))
    }
}
