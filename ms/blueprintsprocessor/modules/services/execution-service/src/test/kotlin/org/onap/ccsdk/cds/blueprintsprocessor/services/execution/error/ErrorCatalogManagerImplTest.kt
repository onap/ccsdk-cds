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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.error

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.error.data.CDSErrorException
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.mockk.MockErrorCatalogConfiguration
import org.onap.ccsdk.error.catalog.data.ErrorCatalog
import org.onap.ccsdk.error.catalog.data.ErrorMessageLibConstants.Companion.ERROR_CATALOG_PROTOCOL_GRPC
import org.onap.ccsdk.error.catalog.data.ErrorMessageLibConstants.Companion.ERROR_CATALOG_PROTOCOL_HTTP
import org.onap.ccsdk.error.catalog.interfaces.EnumErrorCatalogInterface
import org.onap.ccsdk.error.catalog.service.ErrorCatalogService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.BeforeTest
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@ContextConfiguration(
        classes = [MockErrorCatalogConfiguration::class]
)
class ErrorCatalogManagerImplTest{
    private lateinit var errorCatalogManagerImpl: ErrorCatalogManagerImpl
    private lateinit var errorCatalogHttp: ErrorCatalog
    private lateinit var errorCatalogGrpc: ErrorCatalog
    private lateinit var enumError: EnumErrorCatalogInterface
    @Autowired
    lateinit var errorCatalogService: ErrorCatalogService

    @BeforeTest
    fun setup(){
        errorCatalogManagerImpl = ErrorCatalogManagerImpl(errorCatalogService)
        enumError = BlueprintProcessorErrorCodes.GENERIC_FAILURE
        errorCatalogHttp = errorCatalogService.getErrorCatalog(enumError, ERROR_CATALOG_PROTOCOL_HTTP, null,
                "error catalog with http code", Throwable())
        errorCatalogGrpc = errorCatalogService.getErrorCatalog(enumError, ERROR_CATALOG_PROTOCOL_GRPC, null,
                "error catalog with grpc code", Throwable())
    }

    @Test
    fun generateExceptionFromErrorCatalogWithHttpCode() {
        val errorCatalogException = errorCatalogManagerImpl.generateException(errorCatalogHttp, "Generated exception from error catalog")
        val exception = CDSErrorException(errorCatalogHttp, "Generated exception from error catalog")
        assertTrue { errorCatalogException.isEqualTo(exception) }
    }

    @Test
    fun generateExceptionFromErrorCatalogWithGrpcCode() {
        val errorCatalogException = errorCatalogManagerImpl.generateException(errorCatalogGrpc, "Generated exception from error catalog")
        val exception = CDSErrorException(errorCatalogGrpc, "Generated exception from error catalog")
        assertTrue { errorCatalogException.isEqualTo(exception) }
    }

    @Test
    fun generateExceptionFromEnumErrorWithHttpCode() {
        val errorCatalogException = errorCatalogManagerImpl.generateException(enumErrorCatalog = enumError,
                protocol = ERROR_CATALOG_PROTOCOL_HTTP, errorMessage = "Generated exception from Enum error catalog")
        val exception = CDSErrorException(errorCatalogHttp, "Generated exception from Enum error catalog")
        exception.httpCode = enumError.getErrorHttpCode()
        exception.grpcCode = enumError.getErrorGrpcCode()
        assertTrue { errorCatalogException.isEqualTo(exception) }
    }

    @Test
    fun generateExceptionFromEnumErrorWithGrpcCode() {
        val errorCatalogException = errorCatalogManagerImpl.generateException(enumErrorCatalog = enumError,
                errorMessage = "Generated exception from Enum error catalog")
        val exception = CDSErrorException(errorCatalogGrpc, "Generated exception from Enum error catalog")
        exception.httpCode = enumError.getErrorHttpCode()
        exception.grpcCode = enumError.getErrorGrpcCode()
        assertTrue { errorCatalogException.isEqualTo(exception) }
    }
}