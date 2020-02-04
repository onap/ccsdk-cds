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
import kotlin.test.BeforeTest
import kotlin.test.assertTrue
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.mockk.MockErrorCatalogConfiguration
import org.onap.ccsdk.error.catalog.data.ErrorCatalog
import org.onap.ccsdk.error.catalog.data.ErrorMessageLibConstants
import org.onap.ccsdk.error.catalog.interfaces.EnumErrorCatalogInterface
import org.onap.ccsdk.error.catalog.service.ErrorCatalogService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ContextConfiguration(
        classes = [MockErrorCatalogConfiguration::class]
)
class ErrorCatalogServiceTest {
    private lateinit var errorCatalogHttp: ErrorCatalog
    private lateinit var errorCatalogGrpc: ErrorCatalog
    private lateinit var enumError: EnumErrorCatalogInterface
    @Autowired
    lateinit var errorCatalogService: ErrorCatalogService

    @BeforeTest
    fun setup() {
        enumError = BlueprintProcessorErrorCodes.GENERIC_FAILURE
        errorCatalogHttp = ErrorCatalog(enumError.getErrorName(), enumError.getErrorDomain(), enumError.getErrorHttpCode(),
                "Contact CDS administrator team.", "Internal error in Blueprint Processor run time.")
        errorCatalogGrpc = ErrorCatalog(enumError.getErrorName(), enumError.getErrorDomain(), enumError.getErrorGrpcCode(),
                "Contact CDS administrator team.", "Internal error in Blueprint Processor run time.")
    }

    @Test
    fun getErrorCatalogWithHttpCode() {
        val errorCatalog = errorCatalogService.getErrorCatalog(enumError,
                ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_HTTP, null,
                "error catalog with http code", Throwable())
        assertTrue { errorCatalog == errorCatalogHttp }
    }

    @Test
    fun getErrorCatalogWithGrpcCode() {
        val errorCatalog = errorCatalogService.getErrorCatalog(enumError,
                ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, null,
                "error catalog with grpc code", Throwable())
        assertTrue { errorCatalog == errorCatalogGrpc }
    }
}
