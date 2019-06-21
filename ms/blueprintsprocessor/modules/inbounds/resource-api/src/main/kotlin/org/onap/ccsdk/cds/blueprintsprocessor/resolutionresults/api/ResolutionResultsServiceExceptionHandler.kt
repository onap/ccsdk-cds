/*
 * Copyright © 2018-2019 Bell Canada Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.resolutionresults.api

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
import org.springframework.web.server.ServerWebInputException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.Serializable
import java.util.*

/**
 * Handle exceptions in Resolution Results API and provide relevant HTTP status codes and messages
 *
 * @author Serge Simard
 * @version 1.0
 */
@RestControllerAdvice("org.onap.ccsdk.cds.blueprintsprocessor.resolutionresults")
open class ResolutionResultsServiceExceptionHandler {

    private val log = LoggerFactory.getLogger(ResolutionResultsServiceExceptionHandler::class.toString())

    private val debugMsg = "ResolutionResultsService_Error_Message"

    @ExceptionHandler
    fun ResolutionResultsServiceExceptionHandler(e: BluePrintProcessorException): ResponseEntity<ErrorMessage> {
        log.error(e.message, e)
        val errorCode = ErrorCode.BLUEPRINT_PATH_MISSING
        val errorMessage = ErrorMessage(errorCode.message(e.message!!), errorCode.value, debugMsg)
        return ResponseEntity(errorMessage, HttpStatus.resolve(errorCode.httpCode))
    }

    @ExceptionHandler
    fun ResolutionResultsServiceExceptionHandler(e: ServerWebInputException): ResponseEntity<ErrorMessage> {
        log.error(e.message, e)
        val errorCode = ErrorCode.INVALID_REQUEST_FORMAT
        val errorMessage = ErrorMessage(errorCode.message(e.message!!), errorCode.value, debugMsg)
        return ResponseEntity(errorMessage, HttpStatus.resolve(errorCode.httpCode))
    }

    @ExceptionHandler
    fun ResolutionResultsServiceExceptionHandler(e: EmptyResultDataAccessException): ResponseEntity<ErrorMessage> {
        log.error(e.message, e)
        var errorCode = ErrorCode.RESOURCE_NOT_FOUND
        val errorMessage = ErrorMessage(errorCode.message(e.message!!), errorCode.value, debugMsg)
        return ResponseEntity(errorMessage, HttpStatus.resolve(errorCode.httpCode))
    }

    @ExceptionHandler
    fun ResolutionResultsServiceExceptionHandler(e: JpaObjectRetrievalFailureException): ResponseEntity<ErrorMessage> {
        log.error(e.message, e)

        var errorCode = ErrorCode.RESOURCE_NOT_FOUND
        val errorMessage = ErrorMessage(errorCode.message(e.message!!), errorCode.value, debugMsg)
        return ResponseEntity(errorMessage, HttpStatus.resolve(errorCode.httpCode))
    }

    @ExceptionHandler
    fun ResolutionResultsServiceExceptionHandler(e: Exception): ResponseEntity<ErrorMessage> {
        log.error(e.message, e)
        var errorCode = ErrorCode.GENERIC_FAILURE
        val errorMessage = ErrorMessage(errorCode.message(e.message!!), errorCode.value, debugMsg)
        return ResponseEntity(errorMessage, HttpStatus.resolve(errorCode.httpCode))
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