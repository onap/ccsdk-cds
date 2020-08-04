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

package org.onap.ccsdk.cds.blueprintsprocessor.message.service

import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.controllerblueprints.core.defaultToUUID

interface BlueprintMessageProducerService {

    fun sendMessage(key: String = defaultToUUID(), message: Any): Boolean {
        return sendMessage(key = key, message = message, headers = null)
    }

    fun sendMessage(key: String = defaultToUUID(), topic: String, message: Any): Boolean {
        return sendMessage(key, topic, message, null)
    }

    fun sendMessage(key: String = defaultToUUID(), message: Any, headers: MutableMap<String, String>?): Boolean = runBlocking {
        sendMessageNB(key = key, message = message, headers = headers)
    }

    fun sendMessage(key: String = defaultToUUID(), topic: String, message: Any, headers: MutableMap<String, String>?): Boolean = runBlocking {
        sendMessageNB(key, topic, message, headers)
    }

    suspend fun sendMessageNB(key: String, message: Any): Boolean {
        return sendMessageNB(key = key, message = message, headers = null)
    }

    suspend fun sendMessageNB(key: String, message: Any, headers: MutableMap<String, String>?): Boolean

    suspend fun sendMessageNB(key: String, topic: String, message: Any): Boolean {
        return sendMessageNB(key, topic, message, null)
    }

    suspend fun sendMessageNB(key: String, topic: String, message: Any, headers: MutableMap<String, String>?): Boolean
}
