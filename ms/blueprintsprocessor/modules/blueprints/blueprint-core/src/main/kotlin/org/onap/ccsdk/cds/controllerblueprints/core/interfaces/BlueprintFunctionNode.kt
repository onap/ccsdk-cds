/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.core.interfaces

import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import java.util.function.Function

interface BlueprintFunctionNode<T, R> : Function<T, R> {

    fun getName(): String

    @Throws(BlueprintProcessorException::class)
    fun prepareRequest(executionRequest: T): T = runBlocking {
        prepareRequestNB(executionRequest)
    }

    @Throws(BlueprintProcessorException::class)
    fun process(executionRequest: T) = runBlocking {
        processNB(executionRequest)
    }

    @Throws(BlueprintProcessorException::class)
    fun recover(runtimeException: RuntimeException, executionRequest: T) = runBlocking {
        recoverNB(runtimeException, executionRequest)
    }

    @Throws(BlueprintProcessorException::class)
    fun prepareResponse(): R = runBlocking {
        prepareResponseNB()
    }

    override fun apply(executionServiceInput: T): R {
        try {
            prepareRequest(executionServiceInput)
            process(executionServiceInput)
        } catch (runtimeException: RuntimeException) {
            recover(runtimeException, executionServiceInput)
        }
        return prepareResponse()
    }

    @Throws(BlueprintProcessorException::class)
    suspend fun prepareRequestNB(executionRequest: T): T

    @Throws(BlueprintProcessorException::class)
    suspend fun processNB(executionRequest: T)

    @Throws(BlueprintProcessorException::class)
    suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: T)

    @Throws(BlueprintProcessorException::class)
    suspend fun prepareResponseNB(): R

    @Throws(BlueprintProcessorException::class)
    suspend fun applyNB(t: T): R
}
