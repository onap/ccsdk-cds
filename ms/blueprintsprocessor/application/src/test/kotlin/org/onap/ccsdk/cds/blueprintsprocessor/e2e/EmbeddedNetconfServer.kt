/*
 * Copyright © 2026 Deutsche Telekom AG.
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

package org.onap.ccsdk.cds.blueprintsprocessor.e2e

import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.channel.ChannelSession
import org.apache.sshd.server.command.Command
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.subsystem.SubsystemFactory
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.CopyOnWriteArrayList

/**
 * A NETCONF 1.0 device on the SSH `netconf` subsystem that records every RPC it receives and answers
 * `<ok/>`, or an `<rpc-error>` for the operation passed to [failOperation].
 */
class EmbeddedNetconfServer(val username: String, val password: String) {

    data class Rpc(val messageId: String, val operation: String, val xml: String)

    private val received = CopyOnWriteArrayList<Rpc>()

    @Volatile
    private var failingOperation: String? = null

    private val server = SshServer.setUpDefaultServer().apply {
        port = 0
        keyPairProvider = SimpleGeneratorHostKeyProvider()
        setPasswordAuthenticator { user, pass, _ -> user == username && pass == password }
        subsystemFactories = listOf(NetconfSubsystemFactory())
        start()
    }

    val port: Int get() = server.port

    val rpcs: List<Rpc> get() = received.toList()

    fun operations(): List<String> = received.map { it.operation }

    fun failOperation(operation: String) {
        failingOperation = operation
    }

    fun reset() {
        received.clear()
        failingOperation = null
    }

    private fun reply(rpc: String): String {
        val messageId = MESSAGE_ID.find(rpc)?.groupValues?.get(1) ?: ""
        val operation = OPERATION.find(rpc)?.groupValues?.get(1) ?: ""
        received.add(Rpc(messageId, operation, rpc))
        val body = if (operation == failingOperation) {
            "<rpc-error><error-type>application</error-type><error-tag>operation-failed</error-tag>" +
                "<error-severity>error</error-severity><error-message>$operation rejected</error-message></rpc-error>"
        } else {
            "<ok/>"
        }
        return "$XML_HEADER<rpc-reply message-id=\"$messageId\" xmlns=\"$BASE_NS\">$body</rpc-reply>$END_OF_MESSAGE"
    }

    private inner class NetconfSubsystemFactory : SubsystemFactory {

        override fun getName() = "netconf"

        override fun createSubsystem(channel: ChannelSession): Command = NetconfSession()
    }

    private inner class NetconfSession : Command, Runnable {

        private lateinit var input: InputStream
        private lateinit var output: OutputStream
        private lateinit var exitCallback: ExitCallback

        override fun setInputStream(input: InputStream) {
            this.input = input
        }

        override fun setOutputStream(output: OutputStream) {
            this.output = output
        }

        override fun setErrorStream(error: OutputStream) = Unit

        override fun setExitCallback(callback: ExitCallback) {
            exitCallback = callback
        }

        override fun start(channel: ChannelSession, env: Environment) {
            Thread(this, "e2e-netconf-session").apply { isDaemon = true }.start()
        }

        override fun destroy(channel: ChannelSession) = Unit

        override fun run() {
            try {
                while (true) {
                    val message = readMessage() ?: break
                    // Answer the client's hello rather than greeting first: NetconfSessionImpl only waits for
                    // the server hello once it has sent its own, and drops one that arrives earlier.
                    if (message.contains("<hello")) {
                        write(SERVER_HELLO)
                        continue
                    }
                    write(reply(message))
                    if (message.contains("<close-session")) {
                        break
                    }
                }
            } finally {
                exitCallback.onExit(0)
            }
        }

        private fun readMessage(): String? {
            val message = StringBuilder()
            while (!message.endsWith(END_OF_MESSAGE)) {
                val c = input.read()
                if (c == -1) {
                    return null
                }
                message.append(c.toChar())
            }
            return message.removeSuffix(END_OF_MESSAGE).toString()
        }

        private fun write(message: String) {
            output.write(message.toByteArray(StandardCharsets.UTF_8))
            output.flush()
        }
    }

    companion object {

        private const val END_OF_MESSAGE = "]]>]]>"
        private const val BASE_NS = "urn:ietf:params:xml:ns:netconf:base:1.0"
        private const val XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        private const val SERVER_HELLO = "$XML_HEADER<hello xmlns=\"$BASE_NS\"><capabilities>" +
            "<capability>urn:ietf:params:netconf:base:1.0</capability>" +
            "<capability>urn:ietf:params:netconf:capability:candidate:1.0</capability>" +
            "</capabilities><session-id>1</session-id></hello>$END_OF_MESSAGE"

        private val MESSAGE_ID = Regex("message-id=\"([^\"]*)\"")
        private val OPERATION = Regex("<rpc\\b[^>]*>\\s*<(?:[\\w-]+:)?([\\w-]+)")
    }
}
