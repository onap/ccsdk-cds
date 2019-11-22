/*
 * Copyright © 2017-2019 AT&T, Bell Canada
 * Modifications Copyright (c) 2019 IBM, Bell Canada
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.core

import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceResponse
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfException
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfRpcService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfSession
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.NetconfMessageUtils
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.RpcStatus
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class NetconfRpcServiceImpl(private var deviceInfo: DeviceInfo) : NetconfRpcService {

    private val log = LoggerFactory.getLogger(NetconfRpcService::class.java)

    private var responseTimeout: Int = deviceInfo.replyTimeout

    private lateinit var netconfSession: NetconfSession

    private val messageIdInteger = AtomicInteger(1)

    fun setNetconfSession(netconfSession: NetconfSession) {
        this.netconfSession = netconfSession
    }

    /**
     * accept a user-supplied RPC message WITH HEADER
     * <rpc message-id="abc123" xmlns=".....">
     *     .....
     *     .....
     * </rpc>
     *
     * and replace the user-supplied message-id with the one that is passed.
     * Used by NetconfRpcServiceImpl.invokeRpc to keep the message-id consistent
     * with auto-incremented numbering scheme.
     * @param rpc: Complete custom RPC call including the header
     * @param updatedMessageID new message-id to substitute
     * @return updated RPC message with message-id replaced.
     */
    private fun replaceUserSuppliedNetconfMessageID(rpc: String, updatedMessageID: String): String {
        return rpc.replaceFirst("message-id=\".+\"".toRegex(), "message-id=\"$updatedMessageID\"")
    }

    override fun invokeRpc(rpc: String): DeviceResponse {
        var output = DeviceResponse()
        // Attempt to extract the message-id field from the <rpc call
        val updatedMessageId = messageIdInteger.getAndIncrement().toString()
        val origMessageId = NetconfMessageUtils.getMsgId(rpc)
        log.info("$deviceInfo: invokeRpc: updating rpc original message-id:($origMessageId) to messageId($updatedMessageId)")
        try {
            output = asyncRpc(replaceUserSuppliedNetconfMessageID(rpc, updatedMessageId), updatedMessageId)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in 'invokeRpc' command. Message: ${e.message}."
            log.error("$deviceInfo: failed in 'invokeRpc' command. Exception: $e")
        }
        return output
    }

    override fun get(filter: String): DeviceResponse {
        var output = DeviceResponse()
        val messageId = messageIdInteger.getAndIncrement().toString()
        log.info("$deviceInfo: get operational config: messageId($messageId)")
        try {
            val message = NetconfMessageUtils.get(messageId, filter)
            output = asyncRpc(message, messageId)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in 'get' command. Message: ${e.message}..."
            log.error("$deviceInfo: failed in 'get' command. Exception: $e")
        }
        return output
    }

    override fun getConfig(filter: String, configTarget: String): DeviceResponse {
        var output = DeviceResponse()
        val messageId = messageIdInteger.getAndIncrement().toString()
        log.info("$deviceInfo: getConfig: messageId($messageId)")
        try {
            val message = NetconfMessageUtils.getConfig(messageId, configTarget, filter)
            output = asyncRpc(message, messageId)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in 'get-config' command. Message: ${e.message}"
            log.error("$deviceInfo: failed in 'get-config' command. Exception: $e")
        }
        return output
    }

    override fun deleteConfig(configTarget: String): DeviceResponse {
        var output = DeviceResponse()
        val messageId = messageIdInteger.getAndIncrement().toString()
        log.info("$deviceInfo: deleteConfig: messageId($messageId)")
        try {
            val deleteConfigMessage = NetconfMessageUtils.deleteConfig(messageId, configTarget)
            output.requestMessage = deleteConfigMessage
            output = asyncRpc(deleteConfigMessage, messageId)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in 'delete-config' command. Message: ${e.message}"
            log.error("$deviceInfo: failed in 'deleteConfig' command. Exception: $e")
        }
        return output
    }

    override fun lock(configTarget: String): DeviceResponse {
        var output = DeviceResponse()
        val messageId = messageIdInteger.getAndIncrement().toString()
        log.info("$deviceInfo: lock: messageId($messageId)")
        try {
            val lockMessage = NetconfMessageUtils.lock(messageId, configTarget)
            output.requestMessage = lockMessage
            output = asyncRpc(lockMessage, messageId)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in 'lock' command. Message ${e.message}"
            log.error("$deviceInfo: failed in 'lock' command. Exception: $e")
        }

        return output
    }

    override fun unLock(configTarget: String): DeviceResponse {
        var output = DeviceResponse()
        val messageId = messageIdInteger.getAndIncrement().toString()
        log.info("$deviceInfo: unLock: messageId($messageId)")
        try {
            val unlockMessage = NetconfMessageUtils.unlock(messageId, configTarget)
            output.requestMessage = unlockMessage
            output = asyncRpc(unlockMessage, messageId)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in 'unLock' command. Message: ${e.message}"
            log.error("$deviceInfo: failed in 'unLock' command. Exception: $e")
        }
        return output
    }

    override fun commit(confirmed: Boolean, confirmTimeout: Int, persist: String, persistId: String): DeviceResponse {
        var output = DeviceResponse()
        val messageId = messageIdInteger.getAndIncrement().toString()
        log.info("$deviceInfo: commit: messageId($messageId)")
        try {
            val messageContent = NetconfMessageUtils.commit(messageId, confirmed, confirmTimeout, persist, persistId)
            output = asyncRpc(messageContent, messageId)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in 'commit' command. Message: ${e.message}"
            log.error("$deviceInfo: failed in 'commit' command. Exception: $e")
        }
        return output
    }

    override fun cancelCommit(persistId: String): DeviceResponse {
        var output = DeviceResponse()
        val messageId = messageIdInteger.getAndIncrement().toString()
        log.info("$deviceInfo: cancelCommit: messageId($messageId)")
        try {
            val messageContent = NetconfMessageUtils.cancelCommit(messageId, persistId)
            output = asyncRpc(messageContent, messageId)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in 'cancelCommit' command. Message: ${e.message}"
            log.error("$deviceInfo: failed in 'cancelCommit' command. Exception: $e")
        }
        return output
    }

    override fun discardConfig(): DeviceResponse {
        var output = DeviceResponse()
        val messageId = messageIdInteger.getAndIncrement().toString()
        log.info("$deviceInfo: discard: messageId($messageId)")
        try {
            val discardChangesMessage = NetconfMessageUtils.discardChanges(messageId)
            output.requestMessage = discardChangesMessage
            output = asyncRpc(discardChangesMessage, messageId)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in 'discard-config' command. Message: ${e.message}"
            log.error("$deviceInfo: failed in 'discard-config' command. Exception: $e")
        }
        return output
    }

    override fun editConfig(
        messageContent: String,
        configTarget: String,
        editDefaultOperation: String
    ): DeviceResponse {
        var response = DeviceResponse()
        val messageId = messageIdInteger.getAndIncrement().toString()
        log.info("$deviceInfo: editConfig: messageId($messageId)")
        try {
            val editMessage =
                NetconfMessageUtils.editConfig(messageId, configTarget, editDefaultOperation, messageContent)
            response.requestMessage = editMessage
            response = asyncRpc(editMessage, messageId)
        } catch (e: Exception) {
            response.status = RpcStatus.FAILURE
            response.errorMessage = "$deviceInfo: failed in 'editConfig' command. Message: ${e.message}"
            log.error("$deviceInfo: failed in 'editConfig' command. Exception: $e")
        }
        return response
    }

    override fun validate(configTarget: String): DeviceResponse {
        var output = DeviceResponse()
        val messageId = messageIdInteger.getAndIncrement().toString()
        try {
            val validateMessage = NetconfMessageUtils.validate(messageId, configTarget)
            output.requestMessage = validateMessage
            output = asyncRpc(validateMessage, messageId)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in 'validate' command. Message: ${e.message}"
            log.error("$deviceInfo: failed in 'validate' command. Exception: $e")
        }
        return output
    }

    override fun closeSession(force: Boolean): DeviceResponse {
        var output = DeviceResponse()
        val messageId = messageIdInteger.getAndIncrement().toString()
        log.info("$deviceInfo: closeSession: messageId($messageId)")
        try {
            val messageContent = NetconfMessageUtils.closeSession(messageId, force)
            output = asyncRpc(messageContent, messageId)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in 'closeSession' command. Message: ${e.message}"
            log.error("$deviceInfo: failed in 'closeSession' command. Exception: $e")
        }
        return output
    }

    @Throws(NetconfException::class)
    override fun asyncRpc(request: String, messageId: String): DeviceResponse {
        val response = DeviceResponse()
        log.info("$deviceInfo: send asyncRpc with messageId($messageId)")
        response.requestMessage = request

        val rpcResponse = netconfSession.asyncRpc(request, messageId).get(responseTimeout.toLong(), TimeUnit.SECONDS)
        // TODO catch TimeoutException and ExecutionException
        if (!NetconfMessageUtils.checkReply(rpcResponse)) {
            log.error("RPC response didn't pass validation... $rpcResponse")
            throw NetconfException(rpcResponse)
        }
        response.responseMessage = rpcResponse
        response.status = RpcStatus.SUCCESS
        response.errorMessage = null
        return response
    }
}
