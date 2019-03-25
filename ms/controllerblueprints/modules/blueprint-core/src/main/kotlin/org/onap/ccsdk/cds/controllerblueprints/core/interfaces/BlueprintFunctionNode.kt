/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import java.util.function.Function


interface BlueprintFunctionNode<T, R> : Function<T, R> {

    fun getName(): String

    @Throws(BluePrintProcessorException::class)
    fun prepareRequest(executionRequest: T): T

    @Throws(BluePrintProcessorException::class)
    fun process(executionRequest: T)

    @Throws(BluePrintProcessorException::class)
    fun recover(runtimeException: RuntimeException, executionRequest: T)

    @Throws(BluePrintProcessorException::class)
    fun prepareResponse(): R

}