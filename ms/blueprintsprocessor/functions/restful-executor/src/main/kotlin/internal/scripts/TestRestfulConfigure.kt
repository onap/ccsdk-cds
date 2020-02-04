/*
 *  Copyright © 2019 IBM.
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
@file:Suppress("unused")

package internal.scripts

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restful.executor.RestfulCMComponentFunction
import org.slf4j.LoggerFactory

/**
 * This is for used for Testing only
 */
open class TestRestfulConfigure : RestfulCMComponentFunction() {

    val log = LoggerFactory.getLogger(TestRestfulConfigure::class.java)!!

    override fun getName(): String {
        return "TestRestfulConfigure"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        log.info("processing request..")
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("recovering..")
    }
}
