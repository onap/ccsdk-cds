/*
 *  Copyright © 2019 IBM.
 *
 *  Modifications Copyright © 2018-2019 IBM, Bell Canada
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

import org.apache.commons.io.output.TeeOutputStream
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ChannelExec
import org.apache.sshd.client.channel.ChannelShell
import org.apache.sshd.client.channel.ClientChannel
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier
import org.apache.sshd.client.session.ClientSession
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.BasicAuthSshClientProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.Collections
import java.util.EnumSet

open class BasicAuthSshClientService(private val basicAuthSshClientProperties: BasicAuthSshClientProperties)
    : BlueprintSshClientService {

    private val log = LoggerFactory.getLogger(BasicAuthSshClientService::class.java)!!

    private lateinit var sshClient: SshClient
    private lateinit var clientSession: ClientSession
    private var channel: ChannelExec? = null
    private var shellChannel: ChannelShell? = null

    override suspend fun startSessionNB(): ClientSession {
        sshClient = SshClient.setUpDefaultClient()
        sshClient.serverKeyVerifier = AcceptAllServerKeyVerifier.INSTANCE
        sshClient.start()
        log.debug("SSH Client Service started successfully")
        clientSession = sshClient.connect(
            basicAuthSshClientProperties.username, basicAuthSshClientProperties.host,
            basicAuthSshClientProperties.port
        )
            .verify(basicAuthSshClientProperties.connectionTimeOut)
            .session

        clientSession.addPasswordIdentity(basicAuthSshClientProperties.password)
        clientSession.auth().verify(basicAuthSshClientProperties.connectionTimeOut)
        log.info("SSH client session($clientSession) created")
        return clientSession
    }

    override suspend fun executeCommandsNB(commands: List<String>, timeOut: Long): String {

        if (!clientSession.isAuthenticated) {
            throw BluePrintProcessorException("Failed to authenticate user (${basicAuthSshClientProperties.username}) " +
                    "on the remote device ${basicAuthSshClientProperties.host}")
        }

        val buffer = StringBuffer()
        try {
           clientSession.use { session ->
               session.createShellChannel().use { channel ->
                   ByteArrayOutputStream().use { sent ->
                       PipedOutputStream().use { pipedIn ->
                           PipedInputStream(pipedIn).use { pipedOut ->
                               channel.setIn(pipedOut)
                               TeeOutputStream(sent, pipedIn).use { teeOut ->
                                   ByteArrayOutputStream().use { out ->
                                       ByteArrayOutputStream().use { err ->
                                           channel.out = out
                                           channel.err = err
                                           shellChannel = channel
                                           channel.open()
                                           buffer.append("\n")
                                           commands.forEach { command ->
                                               log.debug("Executing host($session) command($command) \n")
                                               val commandToExecute = command + "\n"
                                               buffer.append("Command : $commandToExecute")
                                               teeOut.write(commandToExecute.toByteArray())
                                               teeOut.flush()
                                               buffer.append("\n" + waitForPrompt(timeOut))
                                               out.reset()
                                               err.reset()
                                           }
                                       }
                                   }
                               }
                           }
                       }
                   }
               }
           }
        } catch (e: Exception) {
            throw BluePrintProcessorException("Failed to execute commands, below the output : $buffer Exception: \n $e")
        }

        shellChannel!!.close(false)
        return buffer.toString()
    }

    private fun waitForPrompt(timeOut: Long): String {
        val waitMask = shellChannel!!.waitFor(
                Collections.unmodifiableSet(EnumSet.of(ClientChannelEvent.CLOSED)), timeOut)
        if (shellChannel!!.out.toString().indexOf("$") <= 0 && waitMask.contains(ClientChannelEvent.TIMEOUT)) {
            throw BluePrintProcessorException("Failed to retrieve commands result in $timeOut ms")
        }

        return shellChannel!!.out.toString()
    }

    override suspend fun executeCommandsInDifferentContextsNB(commands: List<String>, timeOut: Long): String {
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
        log.debug("Executing host($clientSession) command($command) in a new context")

        if (channel != null) {
            channel!!.close()
        }
        channel = clientSession.createExecChannel(command)
        checkNotNull(channel) { "failed to create Channel for the command : $command" }

        // TODO("Convert to streaming ")
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
        return channel!!.out.toString()
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
}
