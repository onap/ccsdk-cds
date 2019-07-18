/*
 *  Copyright © 2018 IBM.
 *  Modifications Copyright © 2018 - 2019 IBM, Bell Canada.
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

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor.RestconfScriptComponentFunction
import org.slf4j.LoggerFactory

open class EditConfigure : RestconfScriptComponentFunction() {

    val log = LoggerFactory.getLogger(EditConfigure::class.java)!!

    override fun getName(): String {
        return "EditConfigure"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        //val webClientService = restClientService("odlparent")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

open class MountNEditConfigure : RestconfScriptComponentFunction() {

    val log = LoggerFactory.getLogger(MountNEditConfigure::class.java)!!

    override fun getName(): String {
        return "MountNEditConfigure"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        log.info("Processing Restconf Request : ${executionRequest.payload}")
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        addError("failed in restconf execution : ${runtimeException.message}")
    }

}

/**
 * This is for used for Testing only
 */
open class TestRestconfConfigure : RestconfScriptComponentFunction() {

    val log = LoggerFactory.getLogger(TestRestconfConfigure::class.java)!!

    override fun getName(): String {
        return "TestRestconfConfigure"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        log.info("processing request..")
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("recovering..")
    }
}
