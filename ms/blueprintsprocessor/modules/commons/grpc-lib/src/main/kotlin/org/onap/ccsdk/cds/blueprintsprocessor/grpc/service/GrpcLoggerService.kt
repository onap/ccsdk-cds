/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.grpc.service

import io.grpc.Grpc
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.getStringKey
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.putStringKeyValue
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants.ONAP_INVOCATION_ID
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants.ONAP_PARTNER_NAME
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants.ONAP_REQUEST_ID
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.defaultToEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.defaultToUUID
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.slf4j.MDC
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class GrpcLoggerService {

    private val log = logger(GrpcLoggerService::class)

    /** Used when server receives request */
    fun <ReqT : Any, RespT : Any> grpcRequesting(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ) {
        val requestID = headers.getStringKey(ONAP_REQUEST_ID).defaultToUUID()
        val invocationID = headers.getStringKey(ONAP_INVOCATION_ID).defaultToUUID()
        val partnerName = headers.getStringKey(ONAP_PARTNER_NAME) ?: "UNKNOWN"
        grpcRequesting(requestID, invocationID, partnerName, call)
    }

    fun <ReqT : Any, RespT : Any> grpcRequesting(
        call: ServerCall<ReqT, RespT>,
        headers: CommonHeader,
        next: ServerCallHandler<ReqT, RespT>
    ) {
        val requestID = headers.requestId.defaultToUUID()
        val invocationID = headers.subRequestId.defaultToUUID()
        val partnerName = headers.originatorId ?: "UNKNOWN"
        grpcRequesting(requestID, invocationID, partnerName, call)
    }

    fun <ReqT : Any, RespT : Any> grpcRequesting(
        requestID: String,
        invocationID: String,
        partnerName: String,
        call: ServerCall<ReqT, RespT>
    ) {
        val localhost = InetAddress.getLocalHost()

        val clientSocketAddress = call.attributes.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR) as? InetSocketAddress
            ?: throw BlueprintProcessorException("failed to get client address")
        val serviceName = call.methodDescriptor.fullMethodName

        MDC.put("InvokeTimestamp", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT))
        MDC.put("RequestID", requestID)
        MDC.put("InvocationID", invocationID)
        MDC.put("PartnerName", partnerName)
        MDC.put("ClientIPAddress", clientSocketAddress.address.defaultToEmpty())
        MDC.put("ServerFQDN", localhost.hostName.defaultToEmpty())
        MDC.put("ServiceName", serviceName)
        log.trace("MDC Properties : ${MDC.getCopyOfContextMap()}")
    }

    /** Used before invoking any GRPC outbound request, Inbound Invocation ID is used as request Id
     * for outbound Request, If invocation Id is missing then default Request Id will be generated.
     */
    fun grpcInvoking(requestHeader: Metadata) {
        requestHeader.putStringKeyValue(ONAP_REQUEST_ID, MDC.get("InvocationID").defaultToUUID())
        requestHeader.putStringKeyValue(ONAP_INVOCATION_ID, UUID.randomUUID().toString())
        requestHeader.putStringKeyValue(ONAP_PARTNER_NAME, BlueprintConstants.APP_NAME)
    }

    /** Used when server returns response */
    fun grpResponding(requestHeaders: Metadata, responseHeaders: Metadata) {
        try {
            responseHeaders.putStringKeyValue(ONAP_REQUEST_ID, MDC.get("RequestID").defaultToEmpty())
            responseHeaders.putStringKeyValue(ONAP_INVOCATION_ID, MDC.get("InvocationID").defaultToEmpty())
            responseHeaders.putStringKeyValue(ONAP_PARTNER_NAME, MDC.get("PartnerName").defaultToEmpty())
        } catch (e: Exception) {
            log.warn("couldn't set grpc response headers", e)
        }
    }
}
