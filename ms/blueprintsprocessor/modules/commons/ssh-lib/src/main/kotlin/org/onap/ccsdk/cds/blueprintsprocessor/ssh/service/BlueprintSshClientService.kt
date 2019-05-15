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

import kotlinx.coroutines.runBlocking
import org.apache.sshd.client.session.ClientSession

interface BlueprintSshClientService {

    fun startSession(): ClientSession = runBlocking {
        startSessionNB()
    }

    fun executeCommands(commands: List<String>, timeOut: Long): String = runBlocking {
        executeCommandsNB(commands, timeOut)
    }

    fun executeCommand(command: String, timeOut: Long): String = runBlocking {
        executeCommandNB(command, timeOut)
    }

    fun closeSession() = runBlocking {
        closeSessionNB()
    }

    suspend fun startSessionNB(): ClientSession

    suspend fun executeCommandsNB(commands: List<String>, timeOut: Long): String

    suspend fun executeCommandNB(command: String, timeOut: Long): String

    suspend fun closeSessionNB()
}