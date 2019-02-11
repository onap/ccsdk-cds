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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.core

import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.NetconfException
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.data.NetconfDeviceOutputEvent
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.DeviceInfo
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.NetconfSessionDelegate
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils.RpcConstants
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils.RpcMessageUtils
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture


class NetconfStreamThread(private var inputStream: InputStream, private var out : OutputStream,
                          private val netconfDeviceInfo: DeviceInfo, private val netconfSessionDelegate: NetconfSessionDelegate,
                          private var replies :MutableMap<String, CompletableFuture<String>> ) : Thread() {

    val log = LoggerFactory.getLogger(NetconfStreamThread::class.java)
    lateinit var state : NetconfMessageState
   // val outputStream = OutputStreamWriter(out, StandardCharsets.UTF_8)
   private var outputStream: OutputStreamWriter? = null

    override fun run() {
        var bufferReader: BufferedReader? = null
        while (bufferReader == null) {
            bufferReader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
        }

        try {
            var socketClosed = false
            val deviceReplyBuilder = StringBuilder()
            while (!socketClosed) {
                val cInt = bufferReader!!.read()
                if (cInt == -1) {
                    log.debug("Netconf device {} sent error char in session will need to be reopend",
                            netconfDeviceInfo)
                    NetconfDeviceOutputEvent(NetconfDeviceOutputEvent.Type.SESSION_CLOSED, null!!, null!!,
                            null !!, netconfDeviceInfo)
                    socketClosed = true
                    log.debug("Netconf device {} ERROR cInt == -1 socketClosed = true", netconfDeviceInfo)
                }
                val c = cInt.toChar()
                state = state.evaluateChar(c)
                deviceReplyBuilder.append(c)
                if (state === NetconfMessageState.END_PATTERN) {
                    var deviceReply = deviceReplyBuilder.toString()
                    if (deviceReply == RpcConstants.END_PATTERN) {
                        socketClosed = true
                        close(deviceReply)
                    } else {
                        deviceReply = deviceReply.replace(RpcConstants.END_PATTERN, "")
                        dealWithReply(deviceReply)
                        deviceReplyBuilder.setLength(0)
                    }
                } else if (state === NetconfMessageState.END_CHUNKED_PATTERN) {
                    var deviceReply = deviceReplyBuilder.toString()
                    if (!RpcMessageUtils.validateChunkedFraming(deviceReply)) {
                        log.debug("Netconf device {} send badly framed message {}", netconfDeviceInfo, deviceReply)
                        socketClosed = true
                        close(deviceReply)
                    } else {
                        deviceReply = deviceReply.replace(RpcConstants.MSGLEN_REGEX_PATTERN.toRegex(), "")
                        deviceReply = deviceReply.replace(RpcMessageUtils.CHUNKED_END_REGEX_PATTERN.toRegex(), "")
                        dealWithReply(deviceReply)
                        deviceReplyBuilder.setLength(0)
                    }
                }
            }
        } catch (e: IOException) {
            log.warn("Error in reading from the session for device {} ", netconfDeviceInfo, e)
            throw IllegalStateException(
                    NetconfException(message = "Error in reading from the session for device {}$netconfDeviceInfo"))
        }

    }

    enum class NetconfMessageState {

        NO_MATCHING_PATTERN {
            override fun evaluateChar(c: Char): NetconfMessageState {
                return if (c == ']') {
                    FIRST_BRACKET
                } else if (c == '\n') {
                    FIRST_LF
                } else {
                    this
                }
            }
        },
        FIRST_BRACKET {
            override fun evaluateChar(c: Char): NetconfMessageState {
                return if (c == ']') {
                    SECOND_BRACKET
                } else {
                    NO_MATCHING_PATTERN
                }
            }
        },
        SECOND_BRACKET {
            override fun evaluateChar(c: Char): NetconfMessageState {
                return if (c == '>') {
                    FIRST_BIGGER
                } else {
                    NO_MATCHING_PATTERN
                }
            }
        },
        FIRST_BIGGER {
            override fun evaluateChar(c: Char): NetconfMessageState {
                return if (c == ']') {
                    THIRD_BRACKET
                } else {
                    NO_MATCHING_PATTERN
                }
            }
        },
        THIRD_BRACKET {
            override fun evaluateChar(c: Char): NetconfMessageState {
                return if (c == ']') {
                    ENDING_BIGGER
                } else {
                    NO_MATCHING_PATTERN
                }
            }
        },
        ENDING_BIGGER {
            override fun evaluateChar(c: Char): NetconfMessageState {
                return if (c == '>') {
                    END_PATTERN
                } else {
                    NO_MATCHING_PATTERN
                }
            }
        },
        FIRST_LF {
            override fun evaluateChar(c: Char): NetconfMessageState {
                return if (c == '#') {
                    FIRST_HASH
                } else if (c == ']') {
                    FIRST_BRACKET
                } else if (c == '\n') {
                    this
                } else {
                    NO_MATCHING_PATTERN
                }
            }
        },
        FIRST_HASH {
            override fun evaluateChar(c: Char): NetconfMessageState {
                return if (c == '#') {
                    SECOND_HASH
                } else {
                    NO_MATCHING_PATTERN
                }
            }
        },
        SECOND_HASH {
            override fun evaluateChar(c: Char): NetconfMessageState {
                return if (c == '\n') {
                    END_CHUNKED_PATTERN
                } else {
                    NO_MATCHING_PATTERN
                }
            }
        },
        END_CHUNKED_PATTERN {
            override fun evaluateChar(c: Char): NetconfMessageState {
                return NO_MATCHING_PATTERN
            }
        },
        END_PATTERN {
            override fun evaluateChar(c: Char): NetconfMessageState {
                return NO_MATCHING_PATTERN
            }
        };

        internal abstract fun evaluateChar(c: Char): NetconfMessageState
    }

    private fun close(deviceReply: String) {
        log.debug("Netconf device {} socketClosed = true DEVICE_UNREGISTERED {}", netconfDeviceInfo, deviceReply)
        NetconfDeviceOutputEvent(NetconfDeviceOutputEvent.Type.DEVICE_UNREGISTERED, null!!, null!!, null!!,
                netconfDeviceInfo)
        this.interrupt()
    }

    private fun dealWithReply(deviceReply: String) {
        if (deviceReply.contains(RpcConstants.RPC_REPLY) || deviceReply.contains(RpcConstants.RPC_ERROR)
                || deviceReply.contains(RpcConstants.HELLO)) {
            log.info("From Netconf Device: {} \n for Message-ID: {} \n Device-Reply: \n {} \n ", netconfDeviceInfo,
                    RpcMessageUtils.getMsgId(deviceReply), deviceReply)
            val event = NetconfDeviceOutputEvent(NetconfDeviceOutputEvent.Type.DEVICE_REPLY,
                    null!!, deviceReply, RpcMessageUtils.getMsgId(deviceReply), netconfDeviceInfo)
            netconfSessionDelegate.notify(event)
        } else {
            log.debug("Error Reply: \n {} \n from Netconf Device: {}", deviceReply, netconfDeviceInfo)
        }
    }

    @SuppressWarnings("squid:S3655")
    @Override
    fun sendMessage(request: String): CompletableFuture<String> {
        val messageId = RpcMessageUtils.getMsgId(request)
        return sendMessage(request, messageId.get())
    }

    fun sendMessage(request: String, messageId: String): CompletableFuture<String> {
        log.info("Sending message: \n {} \n to NETCONF Device: {}", request, netconfDeviceInfo)
        val cf = CompletableFuture<String>()
        replies.put(messageId, cf)
       // outputStream = OutputStreamWriter(out, StandardCharsets.UTF_8)
        synchronized(OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            try {

                OutputStreamWriter(out, StandardCharsets.UTF_8).write(request)
                OutputStreamWriter(out, StandardCharsets.UTF_8).flush()
            } catch (e: IOException) {
                log.error("Writing to NETCONF Device {} failed", netconfDeviceInfo, e)
                cf.completeExceptionally(e)
            }

        }
        return cf
    }

}