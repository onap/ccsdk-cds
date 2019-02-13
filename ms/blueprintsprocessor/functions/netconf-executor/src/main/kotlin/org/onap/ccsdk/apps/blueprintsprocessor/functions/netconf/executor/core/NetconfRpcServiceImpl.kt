/*
 * Copyright © 2017-2019 AT&T, Bell Canada
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

import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.api.DeviceResponse
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.api.NetconfException
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.api.NetconfRpcService
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.api.NetconfSession
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils.NetconfDatastore
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils.NetconfMessageUtils
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils.RpcStatus
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class NetconfRpcServiceImpl(private val deviceInfo: DeviceInfo) : NetconfRpcService {

    private val log = LoggerFactory.getLogger(NetconfRpcService::class.java)

    private lateinit var netconfSession: NetconfSession

    fun setNetconfSession(netconfSession: NetconfSession) {
        this.netconfSession = netconfSession
    }

    override fun getConfig(messageId: String, filter: String, configTarget: String,
                           messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        log.info("$deviceInfo: getConfig: messageId($messageId)")
        try {
            val message = NetconfMessageUtils.getConfig(messageId, configTarget, filter)
            output = asyncRpc(message, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in get-config command $e.message"
        }
        return output
    }

    override fun deleteConfig(messageId: String, configTarget: String, messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        log.info("$deviceInfo: deleteConfig: messageId($messageId)")
        try {
            val deleteConfigMessage = NetconfMessageUtils.deleteConfig(messageId, configTarget)
            output.requestMessage = deleteConfigMessage
            output = asyncRpc(deleteConfigMessage, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in delete config command $e.message"
        }
        return output
    }

    override fun lock(messageId: String, configTarget: String, messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        log.info("$deviceInfo: lock: messageId($messageId)")
        try {
            val lockMessage = NetconfMessageUtils.lock(messageId, configTarget)
            output.requestMessage = lockMessage
            output = asyncRpc(lockMessage, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in lock command $e.message"
        }

        return output
    }

    override fun unLock(messageId: String, configTarget: String, messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        log.info("$deviceInfo: unLock: messageId($messageId)")
        try {
            val unlockMessage = NetconfMessageUtils.unlock(messageId, configTarget)
            output.requestMessage = unlockMessage
            output = asyncRpc(unlockMessage, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in lock command $e.message"
        }
        return output
    }

    override fun commit(messageId: String, discardChanges: Boolean, messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        log.info("$deviceInfo: commit: messageId($messageId)")
        try {
            val messageContent = NetconfMessageUtils.commit(messageId)
            output = asyncRpc(messageContent, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in commit command $e.message"

            // If commit failed apply discard changes
            if (discardChanges) {
                val discardChangesConfigMessageId = "$messageId-discard-changes"
                val discardOutput = discardConfig(discardChangesConfigMessageId, deviceInfo.replyTimeout)
                output.addSubDeviceResponse(discardChangesConfigMessageId, discardOutput)
            }
        }
        return output
    }

    override fun discardConfig(messageId: String, messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        log.info("$deviceInfo: discard: messageId($messageId)")
        try {
            val discardChangesMessage = NetconfMessageUtils.discardChanges(messageId)
            output.requestMessage = discardChangesMessage
            output = asyncRpc(discardChangesMessage, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in discard changes command " + e.message
        }
        return output
    }

    override fun closeSession(messageId: String, force: Boolean, messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        log.info("$deviceInfo: closeSession: messageId($messageId)")
        try {
            val messageContent = NetconfMessageUtils.closeSession(messageId, force)
            output = asyncRpc(messageContent, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in closeSession command " + e.message
        }
        return output
    }

    @Throws(NetconfException::class)
    override fun asyncRpc(request: String, messageId: String, messageTimeout: Int): DeviceResponse {
        val response = DeviceResponse()
        log.info("$deviceInfo: send asyncRpc with messageId($messageId)")
        response.requestMessage = request

        val rpcResponse = netconfSession.asyncRpc(request, messageId).get(messageTimeout.toLong(), TimeUnit.SECONDS)
        if (!NetconfMessageUtils.checkReply(rpcResponse)) {
            throw NetconfException(rpcResponse)
        }
        response.responseMessage = rpcResponse
        response.status = RpcStatus.SUCCESS
        response.errorMessage = null
        return response
    }

    override fun editConfig(messageId: String, messageContent: String, lock: Boolean, configTarget: String,
                            editDefaultOperation: String, deleteConfig: Boolean, validate: Boolean, commit: Boolean,
                            discardChanges: Boolean, unlock: Boolean, messageTimeout: Int): DeviceResponse {
        var editConfigDeviceResponse =
            DeviceResponse()

        try {
            val editMessage =
                NetconfMessageUtils.editConfig(messageId, configTarget, editDefaultOperation, messageContent)
            editConfigDeviceResponse.requestMessage = editMessage

            if (lock) {
                val lockMessageId = "$messageId-lock"
                val lockDeviceResponse = lock(lockMessageId, configTarget, deviceInfo.replyTimeout)
                editConfigDeviceResponse.addSubDeviceResponse(lockMessageId, lockDeviceResponse)
                if (!RpcStatus.SUCCESS.equals(lockDeviceResponse.status, ignoreCase = true)) {
                    throw NetconfException(
                        lockDeviceResponse.errorMessage!!)
                }
            }

            if (deleteConfig) {
                val deleteConfigMessageId = "$messageId-delete"
                val deleteConfigDeviceResponse = deleteConfig(deleteConfigMessageId,
                    NetconfDatastore.CANDIDATE, deviceInfo.replyTimeout)
                editConfigDeviceResponse.addSubDeviceResponse(deleteConfigMessageId, deleteConfigDeviceResponse)
                if (!RpcStatus.SUCCESS.equals(deleteConfigDeviceResponse.status,
                        ignoreCase = true)) {
                    throw NetconfException(
                        deleteConfigDeviceResponse.errorMessage!!)
                }
            }

            if (discardChanges) {
                val discardConfigMessageId = "$messageId-discard"
                val discardConfigDeviceResponse = discardConfig(discardConfigMessageId, deviceInfo.replyTimeout)
                editConfigDeviceResponse.addSubDeviceResponse(discardConfigMessageId, discardConfigDeviceResponse)
                if (!RpcStatus.SUCCESS.equals(discardConfigDeviceResponse.status,
                        ignoreCase = true)) {
                    throw NetconfException(
                        discardConfigDeviceResponse.errorMessage!!)
                }
            }

            editConfigDeviceResponse = asyncRpc(editMessage, messageId, messageTimeout)
            if (!RpcStatus.SUCCESS.equals(editConfigDeviceResponse.status, ignoreCase = true)) {
                throw NetconfException(
                    editConfigDeviceResponse.errorMessage!!)
            }

            if (validate) {
                val validateMessageId = "$messageId-validate"
                val validateDeviceResponse = validate(validateMessageId,
                    NetconfDatastore.CANDIDATE, deviceInfo.replyTimeout)
                editConfigDeviceResponse.addSubDeviceResponse(validateMessageId, validateDeviceResponse)
                if (!RpcStatus.SUCCESS.equals(validateDeviceResponse.status, ignoreCase = true)) {
                    throw NetconfException(
                        validateDeviceResponse.errorMessage!!)
                }
            }

            /**
             * If Commit is enable, the commit response is treated as Edit config response, If commit failed, we
             * need not to throw an exception, until we unlock the device.
             */
            if (commit) {
                val commitMessageId = "$messageId-commit"
                val commitDeviceResponse =
                    commit(commitMessageId, discardChanges, deviceInfo.replyTimeout)
                editConfigDeviceResponse.addSubDeviceResponse(commitMessageId, commitDeviceResponse)
                if (!RpcStatus.SUCCESS.equals(commitDeviceResponse.status, ignoreCase = true)) {
                    throw NetconfException(
                        commitDeviceResponse.errorMessage!!)
                }
            }

        } catch (e: Exception) {
            editConfigDeviceResponse.status = RpcStatus.FAILURE
            editConfigDeviceResponse.errorMessage = e.message
        } finally {
            if (unlock) {
                val unlockMessageId = "$messageId-unlock"
                val unlockDeviceResponse = unLock(unlockMessageId, configTarget, deviceInfo.replyTimeout)
                editConfigDeviceResponse.addSubDeviceResponse(unlockMessageId, unlockDeviceResponse)
                if (!RpcStatus.SUCCESS.equals(unlockDeviceResponse.status, ignoreCase = true)) {
                    editConfigDeviceResponse.status = RpcStatus.FAILURE
                    editConfigDeviceResponse.errorMessage = unlockDeviceResponse.errorMessage
                }
            }
        }
        return editConfigDeviceResponse
    }

    override fun validate(messageId: String, configTarget: String, messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        try {
            val validateMessage = NetconfMessageUtils.validate(messageId, configTarget)
            output.requestMessage = validateMessage
            output = asyncRpc(validateMessage, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = RpcStatus.FAILURE
            output.errorMessage = "$deviceInfo: failed in validate command " + e.message
        }
        return output
    }
}