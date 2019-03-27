/*
 *  Copyright (C) 2019 AT&T, Amdocs, Bell Canada
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core

import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfReceivedEvent
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfSessionListener
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.NetconfMessageUtils
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.RpcMessageUtils
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class NetconfDeviceCommunicator(private var inputStream: InputStream,
                                private var out: OutputStream,
                                private val deviceInfo: DeviceInfo,
                                private val sessionListener: NetconfSessionListener,
                                private var replies: MutableMap<String, CompletableFuture<String>>) : Thread() {

    private val log = LoggerFactory.getLogger(NetconfDeviceCommunicator::class.java)
    private var state = NetconfMessageState.NO_MATCHING_PATTERN
    private val sendMessageLockObject = Any()

    init {
        start()
    }

    override fun run() {
        var bufferReader: BufferedReader? = null
        while (bufferReader == null) {
            bufferReader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
        }

        try {
            var socketClosed = false
            val deviceReplyBuilder = StringBuilder()
            while (!socketClosed) {
                val cInt = bufferReader.read()
                if (cInt == -1) {
                    log.error("$deviceInfo: Received cInt = -1")
                    socketClosed = true
                }
                val c = cInt.toChar()
                state = state.evaluateChar(c)
                deviceReplyBuilder.append(c)
                if (state === NetconfMessageState.END_PATTERN) {
                    var deviceReply = deviceReplyBuilder.toString()
                    if (deviceReply == RpcMessageUtils.END_PATTERN) {
                        socketClosed = true
                        bufferReader.close()
                        sessionListener.notify(NetconfReceivedEvent(
                            NetconfReceivedEvent.Type.DEVICE_UNREGISTERED,
                            deviceInfo = deviceInfo))
                    } else {
                        deviceReply = deviceReply.replace(RpcMessageUtils.END_PATTERN, "")
                        receivedMessage(deviceReply)
                        deviceReplyBuilder.setLength(0)
                    }
                } else if (state === NetconfMessageState.END_CHUNKED_PATTERN) {
                    var deviceReply = deviceReplyBuilder.toString()
                    if (!NetconfMessageUtils.validateChunkedFraming(deviceReply)) {
                        log.debug("$deviceInfo: Received badly framed message $deviceReply")
                        socketClosed = true
                        sessionListener.notify(NetconfReceivedEvent(
                            NetconfReceivedEvent.Type.DEVICE_ERROR,
                            deviceInfo = deviceInfo))
                    } else {
                        deviceReply = deviceReply.replace(RpcMessageUtils.MSGLEN_REGEX_PATTERN.toRegex(), "")
                        deviceReply = deviceReply.replace(NetconfMessageUtils.CHUNKED_END_REGEX_PATTERN.toRegex(), "")
                        receivedMessage(deviceReply)
                        deviceReplyBuilder.setLength(0)
                    }
                }
            }
            //TODO: Bufferedreader should be closed

        } catch (e: IOException) {
            log.warn("$deviceInfo: Failed to read from Netconf SSH channel", e)
            sessionListener.notify(NetconfReceivedEvent(
                NetconfReceivedEvent.Type.DEVICE_ERROR,
                deviceInfo = deviceInfo))
        }
    }

    fun sendMessage(request: String, messageId: String): CompletableFuture<String> {
        log.info("$deviceInfo: Sending message: \n $request")
        val future = CompletableFuture<String>()
        replies[messageId] = future
        val outputStream = OutputStreamWriter(out, StandardCharsets.UTF_8)
        synchronized(sendMessageLockObject) {
            try {
                outputStream.write(request)
                outputStream.flush()
                //NOTE: Completion of this future happens in {@link NetconfSessionImpl#addDeviceReply},
                //which is triggered by {@link NetconfSessionListenerImpl}
                // on {@link org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.api.Type.DEVICE_REPLY} event.
            } catch (e: IOException) {
                log.error("$deviceInfo: Failed to send message : \n $request", e)
                future.completeExceptionally(e)
            }
        }
        return future
    }

    /**
     * Gets the value of the {@link CompletableFuture} from {@link NetconfDeviceCommunicator#sendMessage}
     * This function is used by NetconfSessionImpl. Needed to wrap exception testing in NetconfSessionImpl.
     * @param fut {@link CompletableFuture} object
     * @param timeout the maximum time to wait
     * @param timeUnit the time unit of the timeout argument
     * @return the result value
     * @throws CancellationException if this future was cancelled
     * @throws ExecutionException if this future completed exceptionally
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws TimeoutException if the wait timed out
     */
    internal fun getFutureFromSendMessage(
            fut: CompletableFuture<String>, timeout: Long, timeUnit: TimeUnit): String {
        return fut.get(timeout, timeUnit)
    }

    private fun receivedMessage(deviceReply: String) {
        if (deviceReply.contains(RpcMessageUtils.RPC_REPLY)
            || deviceReply.contains(RpcMessageUtils.RPC_ERROR)
            || deviceReply.contains(RpcMessageUtils.HELLO)) {
            log.info("$deviceInfo: Received message with messageId: {}  \n $deviceReply",
                NetconfMessageUtils.getMsgId(deviceReply))
        } else {
            log.error("$deviceInfo: Invalid message received: \n $deviceReply")
        }
        sessionListener.notify(NetconfReceivedEvent(
            NetconfReceivedEvent.Type.DEVICE_REPLY,
            deviceReply,
            NetconfMessageUtils.getMsgId(deviceReply),
            deviceInfo))
    }
}
