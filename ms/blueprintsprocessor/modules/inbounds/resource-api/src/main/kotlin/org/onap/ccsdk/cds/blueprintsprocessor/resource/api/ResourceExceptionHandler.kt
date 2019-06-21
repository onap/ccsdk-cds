/*
 * Copyright © 2019 Bell Canada
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

package org.onap.ccsdk.cds.blueprintsprocessor.resource.api

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.data.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.web.server.ServerWebInputException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.Serializable
import java.util.*

/**
 * Handle exceptions in Resolution API and provide relevant HTTP status codes and messages
 *
 * @author Serge Simard
 * @version 1.0
 */
@RestControllerAdvice("org.onap.ccsdk.cds.blueprintsprocessor.resource.api")
open class ResourceExceptionHandler {

    private val log = LoggerFactory.getLogger(ExceptionHandler::class.toString())

    private val debugMsg = "Resolution_Service_Error_Message"

    @ExceptionHandler
    fun resolutionResultsServiceExceptionHandler(e: BluePrintProcessorException): ResponseEntity<ErrorMessage> {
        val errorCode = ErrorCode.BLUEPRINT_PATH_MISSING
        return returnError(e, errorCode)
    }

    @ExceptionHandler
    fun resolutionResultsServiceExceptionHandler(e: ServerWebInputException): ResponseEntity<ErrorMessage> {
        val errorCode = ErrorCode.INVALID_REQUEST_FORMAT
        return returnError(e, errorCode)
    }

    @ExceptionHandler
    fun resolutionResultsServiceExceptionHandler(e: IncorrectResultSizeDataAccessException): ResponseEntity<ErrorMessage> {
        val errorCode = ErrorCode.DUPLICATE_DATA
        return returnError(e, errorCode)
    }

    @ExceptionHandler
    fun resolutionResultsServiceExceptionHandler(e: EmptyResultDataAccessException): ResponseEntity<ErrorMessage> {
        val errorCode = ErrorCode.RESOURCE_NOT_FOUND
        return returnError(e, errorCode)
    }

    @ExceptionHandler
    fun resolutionResultsServiceExceptionHandler(e: JpaObjectRetrievalFailureException): ResponseEntity<ErrorMessage> {
        val errorCode = ErrorCode.RESOURCE_NOT_FOUND
        return returnError(e, errorCode)
    }

    @ExceptionHandler
    fun resolutionResultsServiceExceptionHandler(e: Exception): ResponseEntity<ErrorMessage> {
        val errorCode = ErrorCode.GENERIC_FAILURE
        return returnError(e, errorCode)
    }

    fun returnError(e: Exception, errorCode: ErrorCode): ResponseEntity<ErrorMessage> {
        log.error(e.message, e)
        val errorMessage =
            ErrorMessage(errorCode.message(e.message!!),
                errorCode.value,
                debugMsg)
        return ResponseEntity(errorMessage, HttpStatus.resolve(errorCode.httpCode)!!)
    }

    @ExceptionHandler
    fun ResolutionResultsServiceExceptionHandler(e: ResourceException): ResponseEntity<ErrorMessage> {
        log.error(e.message, e)
        return ResponseEntity(ErrorMessage(e.message, e.code, debugMsg), HttpStatus.resolve(e.code))
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName("errorMessage")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
class ErrorMessage(var message: String?, var code: Int?, var debugMessage: String?) : Serializable {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    var timestamp = Date()
}