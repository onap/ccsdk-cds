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
package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils


import org.apache.sshd.common.NamedFactory
import org.apache.sshd.server.Command
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class NetconfSubsystemFactory : NamedFactory<Command> {

    private val END_CHAR_SEQUENCE = "]]>]]>"

    override fun create(): Command {
        return NetconfSubsystem()
    }

    override fun getName(): String {
        return "netconf"
    }

    /**
     * Simple implementation of netconf reading 1 request, sending a 'hello' response and quitting
     */
    inner class NetconfSubsystem : Command {
        private var input: InputStream? = null
        private var out: OutputStream? = null
        private var clientThread: Thread? = null
        private var r: Int = 0

        @Throws(IOException::class)
        override fun start(env: Environment) {
            clientThread = Thread(object : Runnable {

                override fun run() {
                    try {
                        val message = StringBuilder()
                        while (true) {
                            process(createHelloString())
                            r = input!!.read()
                            if (r == -1) {
                                break
                            } else {
                                val c = r.toChar()
                                message.append(c)
                                val messageString = message.toString()
                                if (messageString.endsWith(END_CHAR_SEQUENCE)) {
                                    println("Detected end message:\n$messageString")
                                    process(createHelloString())
                                    message.setLength(0)
                                    break
                                }
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }

                @Throws(IOException::class)
                private fun process(xmlMessage: String) {
                    out!!.write(xmlMessage.toByteArray(charset("UTF-8")))
                    out!!.write((END_CHAR_SEQUENCE + "\n").toByteArray(charset("UTF-8")))
                    out!!.flush()
                }

                private fun createHelloString(): String {
                    val sessionId = "" + (Math.random() * Integer.MAX_VALUE).toInt()
                    return ("<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n"
                            + "<capabilities>\n<capability>urn:ietf:params:netconf:base:1.0</capability>\n"
                            + "<capability>urn:ietf:params:netconf:base:1.1</capability>\n</capabilities>\n"
                            + "<session-id>" + sessionId + "</session-id>\n</hello>")
                }
            })

            clientThread!!.start()
        }

        @Throws(Exception::class)
        override fun destroy() {
            try {
                clientThread!!.join(2000)
            } catch (e: InterruptedException) {
                // log.warn("Error joining Client thread" + e.getMessage());
            }

            clientThread!!.interrupt()
        }

        override fun setInputStream(input: InputStream) {
            this.input = input
        }

        override fun setOutputStream(out: OutputStream) {
            this.out = out
        }

        override fun setErrorStream(err: OutputStream) {}

        override fun setExitCallback(callback: ExitCallback) {}



    }
}