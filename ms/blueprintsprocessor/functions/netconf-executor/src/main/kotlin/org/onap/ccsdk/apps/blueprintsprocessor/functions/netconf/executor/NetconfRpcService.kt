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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor

import org.apache.commons.collections.CollectionUtils
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.core.NetconfSessionFactory
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.data.DeviceResponse
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.data.NetconfAdaptorConstant
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.DeviceInfo
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.NetconfRpcClientService
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.interfaces.NetconfSession
import org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils.RpcMessageUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit


@Service("netconf-rpc-service")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class NetconfRpcService : NetconfRpcClientService {

    val log = LoggerFactory.getLogger(NetconfRpcService::class.java)

    lateinit var deviceInfo: DeviceInfo
    lateinit var netconfSession: NetconfSession

    private val applyConfigIds = ArrayList<String>()
    private val recordedApplyConfigIds = ArrayList<String>()
    private val DEFAULT_MESSAGE_TIME_OUT = 30


    override fun connect(deviceInfo: DeviceInfo): NetconfSession {
        try {

            this.deviceInfo = deviceInfo

            log.info("Connecting Netconf Device .....")
            this.netconfSession = NetconfSessionFactory.instance("DEFAULT_NETCONF_SESSION", deviceInfo)
            publishMessage("Netconf Device Connection Established")
            return this.netconfSession
        } catch (e: NetconfException) {
            publishMessage(String.format("Netconf Device Connection Failed, %s", e.message))
            throw NetconfException("Netconf Device Connection Failed,$deviceInfo",e)
        }
    }

    override  fun disconnect() {
        netconfSession.close()
    }

    override fun reconnect() {
        disconnect()
        connect(deviceInfo)
    }

    override fun getConfig(messageId: String, messageContent: String, configTarget: String, messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        log.info("in the NetconfRpcService "+messageId)
        try {
            val message = RpcMessageUtils.getConfig(messageId, configTarget, messageContent)
            output = asyncRpc(message, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = NetconfAdaptorConstant.STATUS_FAILURE
            output.errorMessage = e.message
        }

        return output
    }

    override fun deleteConfig(messageId: String, configTarget: String, messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        try {
            val deleteConfigMessage = RpcMessageUtils.deleteConfig(messageId, configTarget)
            output.requestMessage = deleteConfigMessage
            output = asyncRpc(deleteConfigMessage, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = NetconfAdaptorConstant.STATUS_FAILURE
            output.errorMessage = "failed in delete config command " + e.message
        }

        return output
    }

    override fun lock(messageId: String, configTarget: String, messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        try {
            val lockMessage = RpcMessageUtils.lock(messageId, configTarget)
            output.requestMessage = lockMessage
            output = asyncRpc(lockMessage, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = NetconfAdaptorConstant.STATUS_FAILURE
            output.errorMessage = "failed in lock command " + e.message
        }

        return output
    }

    override fun unLock(messageId: String, configTarget: String, messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        try {
            val unlockMessage = RpcMessageUtils.unlock(messageId, configTarget)
            output.requestMessage = unlockMessage
            output = asyncRpc(unlockMessage, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = NetconfAdaptorConstant.STATUS_FAILURE
            output.errorMessage = "failed in lock command " + e.message
        }

        return output
    }

    override fun commit(messageId: String, message: String, discardChanges: Boolean, messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        try {
            val messageContent = RpcMessageUtils.commit(messageId, message)
            output = asyncRpc(messageContent, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = NetconfAdaptorConstant.STATUS_FAILURE
            output.errorMessage = "failed in commit command " + e.message
        } finally {
            // Update the Apply Config status
            if (CollectionUtils.isNotEmpty(applyConfigIds)) {
                val status = if (NetconfAdaptorConstant.STATUS_SUCCESS.equals(output.status,ignoreCase = true))
                    NetconfAdaptorConstant.CONFIG_STATUS_SUCCESS
                else
                    NetconfAdaptorConstant.CONFIG_STATUS_FAILED

                applyConfigIds.forEach{
                    recordedApplyConfigIds.add(it)
                    try {
                        //TODO persistance logic
                       // configPersistService.updateApplyConfig(applyConfigId, status)
                    } catch (e: Exception) {
                        log.error("failed to update apply config ($it) status ($status)")
                    }

                }
                applyConfigIds.clear()
            }
            // TODO
            // Update the Configuration in Running Config Table from 1810 release
            // String recordMessageId = "recoded-running-config-" + messageId;
            // recordRunningConfig(recordMessageId, null);
        }

        // If commit failed apply discard changes
        if (discardChanges && NetconfAdaptorConstant.STATUS_FAILURE.equals(output.status,ignoreCase = true)) {
            try {
                val discardChangesConfigMessageId = "$messageId-discard-changes"
                discardConfig(discardChangesConfigMessageId, NetconfAdaptorConstant.DEFAULT_MESSAGE_TIME_OUT)
            } catch (e: Exception) {
                log.error("failed to rollback ($e) ")
            }

        }

        return output
    }
    override fun discardConfig(messageId: String, messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        try {
            val discardChangesMessage = RpcMessageUtils.discardChanges(messageId)
            output.requestMessage = discardChangesMessage
            output = asyncRpc(discardChangesMessage, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = NetconfAdaptorConstant.STATUS_FAILURE
            output.errorMessage = "failed in discard changes command " + e.message
        }

        return output
    }

    override fun close(messageId: String, force: Boolean, messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        try {
            val messageContent = RpcMessageUtils.closeSession(messageId, force)
            output = asyncRpc(messageContent, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = NetconfAdaptorConstant.STATUS_FAILURE
            output.responseMessage = "failed in close command " + e.message
        }

        return output
    }


    override fun asyncRpc(request: String, msgId: String, timeOut: Int): DeviceResponse {
        val response = DeviceResponse()
                try {
            recordMessage("RPC request $request")
            response.requestMessage = request
            publishMessage("Netconf RPC InProgress")

            val rpcResponse = netconfSession.asyncRpc(request, msgId).get(timeOut.toLong(), TimeUnit.SECONDS)
            response.responseMessage = rpcResponse

            if (!RpcMessageUtils.checkReply(rpcResponse)) {
                throw NetconfException(rpcResponse)
            }
            response.status = NetconfAdaptorConstant.STATUS_SUCCESS
            response.errorMessage = null
        } catch (e: Exception) {
            response.status = NetconfAdaptorConstant.STATUS_FAILURE
            response.errorMessage = e.message
        } finally {
            recordMessage(String.format("RPC Response status (%s) reply (%s), error message (%s)", response.status,
                    response.responseMessage, response.errorMessage))

              when {
                   NetconfAdaptorConstant.STATUS_FAILURE.equals(response.status,ignoreCase = true) -> publishMessage(String.format("Netconf RPC Failed for messgaeID (%s) with (%s)", msgId,
                           response.errorMessage))
                   else -> publishMessage(String.format("Netconf RPC Success for messgaeID (%s)", msgId))
               }
        }

        return response
    }

    override fun editConfig(messageId: String, messageContent: String, reConnect: Boolean, wait: Int, lock: Boolean, configTarget: String, editDefaultOperation: String, clearCandidate: Boolean, validate: Boolean, commit: Boolean, discardChanges: Boolean, unlock: Boolean, preRestartWait: Int, postRestartWait: Int, messageTimeout: Int): DeviceResponse {
        var editConfigDeviceResponse = DeviceResponse()

        try {
            val editMessage = RpcMessageUtils.editConfig(messageId, NetconfAdaptorConstant.CONFIG_TARGET_CANDIDATE,
                    editDefaultOperation, messageContent)
            editConfigDeviceResponse.requestMessage = editMessage

           /* val applyConfigId = configPersistService.saveApplyConfig(netconfExecutionRequest.getRequestId(),
                    netconfDeviceInfo.getName(), netconfDeviceInfo.getDeviceId(), ConfigModelConstant.PROTOCOL_NETCONF,
                    configTarget, editMessage)

            applyConfigIds.add(applyConfigId)  */

            // Reconnect Client Session
            if (reConnect) {
                reconnect()
            }
            // Provide invocation Delay
            if (wait > 0) {
                log.info("Waiting for {} sec for the transaction to start", wait)
                Thread.sleep(wait * 1000L)
            }

            if (lock) {
                val lockMessageId = "$messageId-lock"
                val lockDeviceResponse = lock(lockMessageId, configTarget, DEFAULT_MESSAGE_TIME_OUT)
                editConfigDeviceResponse.addSubDeviceResponse(lockMessageId, lockDeviceResponse)
                if (!NetconfAdaptorConstant.STATUS_SUCCESS.equals(lockDeviceResponse.status,ignoreCase = true)) {
                    throw NetconfException(lockDeviceResponse.errorMessage!!)
                }
            }

            if (clearCandidate) {
                val deleteConfigMessageId = "$messageId-delete"
                val deleteConfigDeviceResponse = deleteConfig(deleteConfigMessageId,
                        NetconfAdaptorConstant.CONFIG_TARGET_CANDIDATE, DEFAULT_MESSAGE_TIME_OUT)
                editConfigDeviceResponse.addSubDeviceResponse(deleteConfigMessageId, deleteConfigDeviceResponse)
                if (!NetconfAdaptorConstant.STATUS_SUCCESS.equals(deleteConfigDeviceResponse.status,ignoreCase = true)) {
                    throw NetconfException(deleteConfigDeviceResponse.errorMessage!!)
                }
            }

            if (discardChanges) {
                val discardConfigMessageId = "$messageId-discard"
                val discardConfigDeviceResponse = discardConfig(discardConfigMessageId, DEFAULT_MESSAGE_TIME_OUT)
                editConfigDeviceResponse.addSubDeviceResponse(discardConfigMessageId, discardConfigDeviceResponse)
                if (!NetconfAdaptorConstant.STATUS_SUCCESS.equals(discardConfigDeviceResponse.status,ignoreCase = true)) {
                    throw NetconfException(discardConfigDeviceResponse.errorMessage!!)
                }
            }

            editConfigDeviceResponse = asyncRpc(editMessage, messageId, messageTimeout)
            if (!NetconfAdaptorConstant.STATUS_SUCCESS.equals(editConfigDeviceResponse.status,ignoreCase = true)) {
                throw NetconfException(editConfigDeviceResponse.errorMessage!!)
            }

            if (validate) {
                val validateMessageId = "$messageId-validate"
                val validateDeviceResponse = validate(validateMessageId,
                        NetconfAdaptorConstant.CONFIG_TARGET_CANDIDATE, DEFAULT_MESSAGE_TIME_OUT)
                editConfigDeviceResponse.addSubDeviceResponse(validateMessageId, validateDeviceResponse)
                if (!NetconfAdaptorConstant.STATUS_SUCCESS.equals(validateDeviceResponse.status,ignoreCase = true)) {
                    throw NetconfException(validateDeviceResponse.errorMessage!!)
                }
            }

            /**
             * If Commit is enable, the commit response is treated as Edit config response, If commit failed, we
             * need not to throw an exception, until we unlock the device.
             */
            if (commit) {
                val commitMessageId = "$messageId-commit"
                val commitDeviceResponse = commit(commitMessageId, commitMessageId, discardChanges, DEFAULT_MESSAGE_TIME_OUT)
                editConfigDeviceResponse.addSubDeviceResponse(commitMessageId, commitDeviceResponse)
                if (!NetconfAdaptorConstant.STATUS_SUCCESS.equals(commitDeviceResponse.status,ignoreCase = true)) {
                    throw NetconfException(commitDeviceResponse.errorMessage!!)
                }
            }

            // Provide pre restart Delay
            if (preRestartWait > 0) {
                log.info("Waiting for {} sec for restart", wait)
                Thread.sleep(preRestartWait * 1000L)
            }
            // TODO Restart Device
            // Provide post restart Delay
            if (postRestartWait > 0) {
                log.info("Waiting for {} sec for the post restart", wait)
                Thread.sleep(postRestartWait * 1000L)
            }

        } catch (e: Exception) {
            editConfigDeviceResponse.status = NetconfAdaptorConstant.STATUS_FAILURE
            editConfigDeviceResponse.errorMessage = e.message
        } finally {
            if (unlock) {
                val unlockMessageId = "$messageId-unlock"
                val unlockDeviceResponse = unLock(unlockMessageId, configTarget, DEFAULT_MESSAGE_TIME_OUT)
                editConfigDeviceResponse.addSubDeviceResponse(unlockMessageId, unlockDeviceResponse)
                if (!NetconfAdaptorConstant.STATUS_SUCCESS.equals(unlockDeviceResponse.status,ignoreCase = true)) {
                    editConfigDeviceResponse.status = NetconfAdaptorConstant.STATUS_FAILURE
                    editConfigDeviceResponse.errorMessage = unlockDeviceResponse.errorMessage
                }
            }
        }
        return editConfigDeviceResponse
    }

    override fun validate(messageId: String, configTarget: String, messageTimeout: Int): DeviceResponse {
        var output = DeviceResponse()
        try {
            val validateMessage = RpcMessageUtils.validate(messageId, configTarget)
            output.requestMessage = validateMessage
            output = asyncRpc(validateMessage, messageId, messageTimeout)
        } catch (e: Exception) {
            output.status = NetconfAdaptorConstant.STATUS_FAILURE
            output.errorMessage = "failed in validate command " + e.message
        }

        return output
    }


    fun recordMessage(message: String) {
        recordMessage(NetconfAdaptorConstant.LOG_MESSAGE_TYPE_LOG, message)
    }

    fun recordMessage(messageType: String, message: String) {
        //TODO
        //eventPublishService.recordMessage(netconfExecutionRequest.getRequestId(), messageType, message)
    }

    fun publishMessage(message: String) {
        //TODO
        //eventPublishService.publishMessage(netconfExecutionRequest.getRequestId(), message)
    }


}