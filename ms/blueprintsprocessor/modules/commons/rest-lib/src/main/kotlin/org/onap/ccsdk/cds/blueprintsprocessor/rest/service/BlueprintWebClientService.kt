/*
 * Copyright © 2017-2019 AT&T, Bell Canada, Nordix Foundation
 * Modifications Copyright © 2018-2019 IBM.
 * Modifications Copyright © 2019 Huawei.
 * Modifications Copyright © 2022 Deutsche Telekom AG.
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

package org.onap.ccsdk.cds.blueprintsprocessor.rest.service

import org.apache.http.message.BasicHeader
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintRetryException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintIOUtils

interface BlueprintWebClientService {
    fun defaultHeaders(): Map<String, String>
    fun convertToBasicHeaders(
        mergedDefaultAndSuppliedHeaders: Map<String, String>
    ): Array<BasicHeader>

    fun exchangeResource(
        methodType: String,
        path: String,
        request: String,
        headers: Map<String, String>
    ): WebClientResponse<String>

    fun exchangeResource(
        methodType: String,
        path: String,
        request: String
    ): WebClientResponse<String>

    suspend fun exchangeNB(methodType: String, path: String, request: Any): WebClientResponse<String>

    suspend fun exchangeNB(methodType: String, path: String, request: Any, additionalHeaders: Map<String, String>?):
        WebClientResponse<String>

    suspend fun <T> exchangeNB(
        methodType: String,
        path: String,
        request: Any,
        additionalHeaders: Map<String, String>?,
        responseType: Class<T>
    ): WebClientResponse<T>

    /** High performance non blocking Retry function, If execution block [block] throws BluePrintRetryException
     * exception then this will perform wait and retrigger accoring to times [times] with delay [delay]
     */
    suspend fun <T> retry(
        times: Int = 1,
        initialDelay: Long = 0,
        delay: Long = 1000,
        block: suspend (Int) -> T
    ): T {
        val exceptionBlock = { e: Exception ->
            if (e !is BluePrintRetryException) {
                throw e
            }
        }
        return BluePrintIOUtils.retry(times, initialDelay, delay, block, exceptionBlock)
    }

    // TODO maybe there could be cases where we care about return headers?
    data class WebClientResponse<T>(val status: Int, val body: T)
}
