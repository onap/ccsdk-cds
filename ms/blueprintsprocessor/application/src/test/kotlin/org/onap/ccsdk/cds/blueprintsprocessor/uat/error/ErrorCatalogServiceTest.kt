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

package org.onap.ccsdk.cds.blueprintsprocessor.uat.error

import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.uat.ErrorCatalogTestConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.grpcProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.httpProcessorException
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalog
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogCodes
import org.onap.ccsdk.cds.error.catalog.core.ErrorMessage
import org.onap.ccsdk.cds.error.catalog.core.ErrorPayload
import org.onap.ccsdk.cds.error.catalog.services.ErrorCatalogService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [ErrorCatalogTestConfiguration::class]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class ErrorCatalogServiceTest {

    @Autowired
    lateinit var errorCatalogService: ErrorCatalogService

    private val domain = "org.onap.ccsdk.cds.blueprintsprocessor"
    private lateinit var errorType: String
    private lateinit var errorCatalogHttp: ErrorCatalog
    private lateinit var errorCatalogGrpc: ErrorCatalog
    private lateinit var errorPayloadHttp: ErrorPayload
    private lateinit var errorPayloadGrpc: ErrorPayload

    @BeforeTest
    fun setup() {
        errorType = ErrorCatalogCodes.GENERIC_FAILURE
        errorCatalogHttp = ErrorCatalog(
            errorType, domain, 500,
            "Contact CDS administrator team.", "Internal error in Blueprint Processor run time."
        )
        errorCatalogGrpc = ErrorCatalog(
            errorType, domain, 2,
            "Contact CDS administrator team.", "Internal error in Blueprint Processor run time."
        )

        errorPayloadHttp = ErrorPayload(
            500, ErrorCatalogCodes.GENERIC_FAILURE,
            "Cause: Internal error in Blueprint Processor run time. \n Action : Contact CDS administrator team.",
            errorMessage = ErrorMessage(
                "org.onap.ccsdk.cds.blueprintsprocessor",
                "Internal error in Blueprint Processor run time.", ""
            )
        )
        errorPayloadGrpc = ErrorPayload(
            2, ErrorCatalogCodes.GENERIC_FAILURE,
            "Cause: Internal error in Blueprint Processor run time. \n Action : Contact CDS administrator team.",
            errorMessage = ErrorMessage(
                "org.onap.ccsdk.cds.blueprintsprocessor",
                "Internal error in Blueprint Processor run time.", ""
            )
        )
    }

    @Test
    fun errorPayloadHttp() {
        val errorPayload = errorCatalogService.errorPayload(
            httpProcessorException(
                errorType, domain,
                "Internal error in Blueprint Processor run time."
            )
        )
        assertTrue { errorPayload.isEqualTo(errorPayloadHttp) }
    }

    @Test
    fun errorPayloadGrpc() {
        val errorPayload = errorCatalogService.errorPayload(
            grpcProcessorException(
                errorType, domain,
                "Internal error in Blueprint Processor run time."
            )
        )
        assertTrue { errorPayload.isEqualTo(errorPayloadGrpc) }
    }

    @Test
    fun getErrorCatalogHttp() {
        val errorCatalog = errorCatalogService.getErrorCatalog(
            httpProcessorException(
                errorType, domain,
                "Internal error in Blueprint Processor run time."
            )
        )
        assertTrue { errorCatalog == errorCatalogHttp }
    }

    @Test
    fun getErrorCatalogGrpc() {
        val errorCatalog = errorCatalogService.getErrorCatalog(
            grpcProcessorException(
                errorType, domain,
                "Internal error in Blueprint Processor run time."
            )
        )
        assertTrue { errorCatalog == errorCatalogGrpc }
    }
}
