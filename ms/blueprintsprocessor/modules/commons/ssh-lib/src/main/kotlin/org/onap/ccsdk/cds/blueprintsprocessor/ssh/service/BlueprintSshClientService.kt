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

package org.onap.ccsdk.cds.blueprintsprocessor.ssh.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.sshd.client.session.ClientSession

interface BlueprintSshClientService {

    fun startSession(): ClientSession

    fun executeCommands(commands: List<String>, timeOut: Long): String

    fun executeCommand(command: String, timeOut: Long): String

    fun closeSession()

    suspend fun startSessionNB(): ClientSession = withContext(Dispatchers.IO) {
        startSession()
    }

    suspend fun executeCommandsNB(commands: List<String>, timeOut: Long): String = withContext(Dispatchers.IO) {
        executeCommands(commands, timeOut)
    }

    suspend fun executeCommandNB(command: String, timeOut: Long): String = withContext(Dispatchers.IO) {
        executeCommand(command, timeOut)
    }

    suspend fun closeSessionNB() = withContext(Dispatchers.IO) {
        closeSession()
    }
}