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

package org.onap.ccsdk.cds.controllerblueprints.core.utils

object BlueprintIOUtils {

    suspend fun <T> retry(
        times: Int = 1,
        initialDelay: Long = 0,
        delay: Long = 1000,
        block: suspend (Int) -> T,
        exceptionBlock: (e: Exception) -> Unit
    ): T {
        var currentDelay = initialDelay
        val currentTimes = times - 1
        repeat(currentTimes) { count ->
            try {
                return block(count)
            } catch (e: Exception) {
                exceptionBlock(e)
            }
            kotlinx.coroutines.delay(currentDelay)
            currentDelay = delay
        }
        return block(currentTimes)
    }
}
