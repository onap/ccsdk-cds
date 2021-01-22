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

package org.onap.ccsdk.cds.blueprintsprocessor.message.service

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.cds.blueprintsprocessor.message.addHeader
import org.onap.ccsdk.cds.blueprintsprocessor.message.toMap
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.defaultToEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.defaultToUUID
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.slf4j.MDC
import java.net.InetAddress
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class MessageLoggerService {

    private val log = logger(MessageLoggerService::class)

    fun messageConsuming(headers: CommonHeader, consumerRecord: ConsumerRecord<*, *>) {
        messageConsuming(
            headers.requestId, headers.subRequestId,
            headers.originatorId, consumerRecord
        )
    }

    fun messageConsuming(consumerRecord: ConsumerRecord<*, *>) {
        val headers = consumerRecord.headers().toMap()
        val requestID = headers[BlueprintConstants.ONAP_REQUEST_ID].defaultToUUID()
        val invocationID = headers[BlueprintConstants.ONAP_INVOCATION_ID].defaultToUUID()
        val partnerName = headers[BlueprintConstants.ONAP_PARTNER_NAME] ?: "UNKNOWN"
        messageConsuming(requestID, invocationID, partnerName, consumerRecord)
    }

    fun messageConsuming(
        requestID: String,
        invocationID: String,
        partnerName: String,
        consumerRecord: ConsumerRecord<*, *>
    ) {
        val headers = consumerRecord.headers().toMap()
        val localhost = InetAddress.getLocalHost()
        MDC.put(
            "InvokeTimestamp",
            ZonedDateTime
                .ofInstant(Instant.ofEpochMilli(consumerRecord.timestamp()), ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT)
        )
        MDC.put("RequestID", requestID)
        MDC.put("InvocationID", invocationID)
        MDC.put("PartnerName", partnerName)
        MDC.put("ClientIPAddress", headers["ClientIPAddress"].defaultToEmpty())
        MDC.put("ServerFQDN", localhost.hostName.defaultToEmpty())
        MDC.put("ServiceName", consumerRecord.topic())
        // Custom MDC for Message Consumers
        MDC.put("Offset", consumerRecord.offset().toString())
        MDC.put("MessageKey", consumerRecord.key()?.toString().defaultToEmpty())
        log.info("Consuming MDC Properties : ${MDC.getCopyOfContextMap()}")
    }

    /** Used before producing message request, Inbound Invocation ID is used as request Id
     * for produced message Request, If invocation Id is missing then default Request Id will be generated.
     */
    fun messageProducing(requestHeader: Headers) {
        val localhost = InetAddress.getLocalHost()
        requestHeader.addHeader(BlueprintConstants.ONAP_REQUEST_ID, MDC.get("InvocationID").defaultToUUID())
        requestHeader.addHeader(BlueprintConstants.ONAP_INVOCATION_ID, UUID.randomUUID().toString())
        requestHeader.addHeader(BlueprintConstants.ONAP_PARTNER_NAME, BlueprintConstants.APP_NAME)
        requestHeader.addHeader("ClientIPAddress", localhost.hostAddress)
    }

    fun messageConsumingExisting() {
        MDC.clear()
    }
}
