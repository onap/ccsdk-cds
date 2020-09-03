/*
 *  Copyright © 2019 IBM.
 *
 *  Modifications Copyright © 2018-2020 IBM, Bell Canada.
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

import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ChannelExec
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier
import org.apache.sshd.client.session.ClientSession
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.BasicAuthSshClientProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.util.Collections
import java.util.EnumSet
import java.util.Scanner
import java.util.ArrayList

open class BasicAuthSshClientService(private val basicAuthSshClientProperties: BasicAuthSshClientProperties) :
        BlueprintSshClientService {

    private val log = LoggerFactory.getLogger(BasicAuthSshClientService::class.java)!!
    private val newLine = "\n".toByteArray()
    private var channel: ChannelExec? = null

    private lateinit var sshClient: SshClient
    private lateinit var clientSession: ClientSession

    override suspend fun startSessionNB(): ClientSession {
        sshClient = SshClient.setUpDefaultClient()
        sshClient.serverKeyVerifier = AcceptAllServerKeyVerifier.INSTANCE
        sshClient.start()
        log.debug("SSH Client Service started successfully")

        clientSession = sshClient.connect(
                basicAuthSshClientProperties.username, basicAuthSshClientProperties.host,
                basicAuthSshClientProperties.port).verify(basicAuthSshClientProperties.connectionTimeOut).session

        clientSession.addPasswordIdentity(basicAuthSshClientProperties.password)
        clientSession.auth().verify(basicAuthSshClientProperties.connectionTimeOut)

        log.info("SSH client session($clientSession) created")
        return clientSession
    }

    private fun startChannel(command: String) {
        try {
            channel = clientSession.createExecChannel(command)
            channel!!.out = ByteArrayOutputStream()
            channel!!.err = ByteArrayOutputStream()
            channel!!.open().await()
        } catch (e: Exception) {
            throw BluePrintProcessorException("Failed to start execution channel: ${e.message}")
        }
    }

    override suspend fun executeCommandsNB(commands: List <String>, timeOut: Long): List<CommandResult> {
        val response = ArrayList<CommandResult>()
        try {
            var stopLoop = false
            val commandsIterator = commands.iterator()
            while (commandsIterator.hasNext() && !stopLoop) {
                val command = commandsIterator.next()
                log.debug("Executing host command($command) \n")
                val result = executeCommand(command, timeOut)
                response.add(result)
                // Once a command in the template has failed break out of the loop to stop executing further commands
                if (!result.successful) {
                    log.debug("Template execution will stop because command ({}) has failed.", command)
                    stopLoop = true
                }
            }
        } catch (e: Exception) {
            throw BluePrintProcessorException("Failed to execute commands, below the error message : ${e.message}")
        }
        return response
    }

    override suspend fun executeCommandNB(command: String, timeOut: Long): CommandResult {
        startChannel(command)

        val deviceOutput = waitForPrompt(timeOut)
        val isSuccessful = isSuccessful(deviceOutput)

        val commandResult = CommandResult(command, deviceOutput, isSuccessful)
        log.info("Command Response: ({}) $newLine", commandResult)
        return commandResult
    }

    private fun waitForPrompt(timeOut: Long): String {
        val waitMask = channel!!.waitFor(
                Collections.unmodifiableSet(EnumSet.of(ClientChannelEvent.CLOSED)), timeOut)
        if (channel!!.out.toString().indexOfAny(arrayListOf("$", ">", "#")) <= 0 && waitMask.contains(ClientChannelEvent.TIMEOUT)) {
            throw BluePrintProcessorException("Timeout: Failed to retrieve commands result in $timeOut ms")
        }
        val outputResult = channel!!.out.toString()
        channel!!.out.flush()
        return outputResult
    }

    override suspend fun closeSessionNB() {
        if (channel != null) {
            channel!!.close()
        }

        if (clientSession.isOpen && !clientSession.isClosing) {
            clientSession.close()
        }

        if (sshClient.isStarted) {
            sshClient.stop()
        }
        log.debug("SSH Client Service stopped successfully")
    }

    // TODO filter output to check error message
    private fun isSuccessful(output: String): Boolean {
        if (output.isNotBlank()) {
            // Output can be multiline, need to check if any of the line starts with %
            Scanner(output).use { scanner ->
                while (scanner.hasNextLine()) {
                    val temp = scanner.nextLine()
                    if (temp.isNotBlank() && (temp.trim { it <= ' ' }.startsWith("%") ||
                                    temp.trim { it <= ' ' }.startsWith("syntax error"))) {
                        return false
                    }
                }
            }
        }
        return true
    }
}

data class CommandResult(val command: String, val deviceOutput: String, val successful: Boolean)
