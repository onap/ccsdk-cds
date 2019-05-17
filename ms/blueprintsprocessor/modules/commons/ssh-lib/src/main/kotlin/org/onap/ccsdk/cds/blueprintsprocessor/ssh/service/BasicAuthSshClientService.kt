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

import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ChannelExec
import org.apache.sshd.client.channel.ClientChannel
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier
import org.apache.sshd.client.session.ClientSession
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.BasicAuthSshClientProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.util.*


open class BasicAuthSshClientService(private val basicAuthSshClientProperties: BasicAuthSshClientProperties)
    : BlueprintSshClientService {

    private val log = LoggerFactory.getLogger(BasicAuthSshClientService::class.java)!!

    private lateinit var sshClient: SshClient
    private lateinit var clientSession: ClientSession
    var channel: ChannelExec? = null

    override suspend fun startSessionNB(): ClientSession {
        sshClient = SshClient.setUpDefaultClient()
        sshClient.serverKeyVerifier = AcceptAllServerKeyVerifier.INSTANCE
        sshClient.start()
        log.debug("SSH Client Service started successfully")
        clientSession = sshClient.connect(basicAuthSshClientProperties.username, basicAuthSshClientProperties.host,
                basicAuthSshClientProperties.port)
                .verify(basicAuthSshClientProperties.connectionTimeOut)
                .session

        clientSession.addPasswordIdentity(basicAuthSshClientProperties.password)
        clientSession.auth().verify(basicAuthSshClientProperties.connectionTimeOut)
        log.info("SSH client session($clientSession) created")
        return clientSession
    }

    override suspend fun executeCommandsNB(commands: List<String>, timeOut: Long): String {
        val buffer = StringBuffer()
        try {
            commands.forEach { command ->
                buffer.append("\nCommand : $command")
                buffer.append("\n" + executeCommandNB(command, timeOut))
            }
        } catch (e: Exception) {
            throw BluePrintProcessorException("Failed to execute commands, below the output : $buffer")
        }
        return buffer.toString()
    }

    override suspend fun executeCommandNB(command: String, timeOut: Long): String {
        log.debug("Executing host($clientSession) command($command)")

        channel = clientSession.createExecChannel(command)
        checkNotNull(channel) { "failed to create Channel for the command : $command" }

        //TODO("Convert to streaming ")
        val outputStream = ByteArrayOutputStream()
        channel!!.out = outputStream
        channel!!.err = outputStream
        channel!!.open().await()
        val waitMask = channel!!.waitFor(Collections.unmodifiableSet(EnumSet.of(ClientChannelEvent.CLOSED)), timeOut)
        if (waitMask.contains(ClientChannelEvent.TIMEOUT)) {
            throw BluePrintProcessorException("Failed to retrieve command result in time: $command")
        }
        val exitStatus = channel!!.exitStatus
        ClientChannel.validateCommandExitStatusCode(command, exitStatus!!)
        return outputStream.toString()
    }

    override suspend fun closeSessionNB() {
        if (channel != null)
            channel!!.close()
        if (sshClient.isStarted) {
            sshClient.stop()
        }
        log.debug("SSH Client Service stopped successfully")
    }
}
