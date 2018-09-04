/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.controllerblueprints;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.service.common.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@RestController
@SuppressWarnings("unused")
public class ApplicationExceptionHandler {
    private static EELFLogger log = EELFManager.getInstance().getLogger(ApplicationExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorMessage> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Application Exception", ex);
        ErrorMessage exceptionResponse = new ErrorMessage(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getLocalizedMessage());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentNotValidException.class,
            HttpRequestMethodNotSupportedException.class})
    public final ResponseEntity<ErrorMessage> handleBadRequest(Exception ex, WebRequest request) {
        log.error("Bad Request Exception", ex);
        ErrorMessage exceptionResponse = new ErrorMessage(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), ex.getLocalizedMessage());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BluePrintException.class)
    public final ResponseEntity<ErrorMessage> handleBlueprintException(BluePrintException ex, WebRequest request) {
        log.error("Application Blueprint Exception", ex);
        ErrorMessage exceptionResponse = new ErrorMessage(ex.getMessage(), ex.getCode(), ex.getLocalizedMessage());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
