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

package org.onap.ccsdk.cds.blueprintsprocessor.nats.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.nats.streaming.MessageHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.nats.NatsConnectionProperties
import org.onap.ccsdk.cds.blueprintsprocessor.nats.NatsLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.nats.TokenAuthNatsConnectionProperties
import org.onap.ccsdk.cds.blueprintsprocessor.nats.strData
import org.onap.ccsdk.cds.blueprintsprocessor.nats.utils.SubscriptionOptionsUtils
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType
import kotlin.test.assertNotNull

class BlueprintNatsServiceTest {

    @Test
    fun testTokenAuthNatService() {
        val configuration = """{
                "type" : "${NatsLibConstants.TYPE_TOKEN_AUTH}",
                "host" : "nats://localhost:4222",
                "token" : "tokenAuth"
            }            
        """.trimIndent()

        val bluePrintNatsLibPropertyService = BlueprintNatsLibPropertyService(mockk())

        val spkBlueprintNatsLibPropertyService = spyk(bluePrintNatsLibPropertyService)
        every {
            spkBlueprintNatsLibPropertyService
                .bluePrintNatsService(any<NatsConnectionProperties>())
        } returns TokenAuthNatsService(
            mockk()
        )

        val bluePrintNatsService =
            spkBlueprintNatsLibPropertyService.bluePrintNatsService(configuration.jsonAsJsonType())
        assertNotNull(bluePrintNatsService, "failed to get NATS Service")
    }

    @Test
    fun testTLSAuthNatService() {
        val configuration = """{
                "type" : "${NatsLibConstants.TYPE_TLS_AUTH}",
                "host" : "nats://localhost:4222"
            }            
        """.trimIndent()

        val bluePrintNatsLibPropertyService = BlueprintNatsLibPropertyService(mockk())

        val spkBlueprintNatsLibPropertyService = spyk(bluePrintNatsLibPropertyService)
        every {
            spkBlueprintNatsLibPropertyService
                .bluePrintNatsService(any<NatsConnectionProperties>())
        } returns TLSAuthNatsService(
            mockk()
        )

        val bluePrintNatsService =
            spkBlueprintNatsLibPropertyService.bluePrintNatsService(configuration.jsonAsJsonType())
        assertNotNull(bluePrintNatsService, "failed to get NATS Service")
    }

    /** Enable to test only on local desktop. Don't enable in Build server
     * Start the Server with : nats-streaming-server -cid cds-cluster --auth tokenAuth -m 8222 -V
     */
    // @Test
    fun localIntegrationTest() {
        runBlocking {

            val connectionProperties = TokenAuthNatsConnectionProperties().apply {
                host = "nats://localhost:4222,nats://localhost:4223"
                clientId = "client-1"
                token = "tokenAuth"
            }
            val natsService = TokenAuthNatsService(connectionProperties)
            val streamingConnection = natsService.connection()
            assertNotNull(streamingConnection, "failed to create nats connection")

            val connectionProperties2 = TokenAuthNatsConnectionProperties().apply {
                host = "nats://localhost:4222,nats://localhost:4223"
                clientId = "client-2"
                token = "tokenAuth"
            }
            val tlsAuthNatsService2 = TokenAuthNatsService(connectionProperties2)
            val streamingConnection2 = tlsAuthNatsService2.connection()
            assertNotNull(streamingConnection2, "failed to create nats connection 2")

            testMultiPublish(natsService)
            testLoadBalance(natsService)
            testLimitSubscription(natsService)
            testRequestReply(natsService)
            testMultiRequestReply(natsService)
            delay(1000)
        }
    }

    private fun testMultiPublish(natsService: BlueprintNatsService) {
        runBlocking {
            /** Multiple Publish Message Test **/
            val messageHandler1 =
                MessageHandler { message -> println("Multi Publish Message Handler 1: ${message.strData()}") }
            val messageHandler2 =
                MessageHandler { message -> println("Multi Publish Message Handler 2: ${message.strData()}") }

            natsService.subscribe("multi-publish", messageHandler1)
            natsService.subscribe("multi-publish", messageHandler2)

            repeat(5) {
                natsService.publish("multi-publish", "multi publish message-$it".toByteArray())
            }
        }
    }

    private fun testLoadBalance(natsService: BlueprintNatsService) {
        runBlocking {
            /** Load balance Publish Message Test **/
            val lbMessageHandler1 =
                MessageHandler { message -> println("LB Publish Message Handler 1: ${message.strData()}") }
            val lbMessageHandler2 =
                MessageHandler { message -> println("LB Publish Message Handler 2: ${message.strData()}") }

            val sub1 = natsService.loadBalanceSubscribe("lb-publish", "lb-group", lbMessageHandler1)
            val sub2 = natsService.loadBalanceSubscribe("lb-publish", "lb-group", lbMessageHandler2)

            repeat(5) {
                natsService.publish("lb-publish", "lb publish message-$it".toByteArray())
            }
            sub1.unsubscribe()
            sub2.unsubscribe()
        }
    }

    private fun testLimitSubscription(natsService: BlueprintNatsService) {
        runBlocking {
            /** Load balance Publish Message Test **/
            val lbMessageHandler1 =
                MessageHandler { message ->
                    runBlocking {
                        println("LB Publish Message Handler 1: ${message.strData()}")
                        message.ack()
                    }
                }
            val lbMessageHandler2 =
                MessageHandler { message ->
                    runBlocking {
                        println("LB Publish Message Handler 2: ${message.strData()}")
                        message.ack()
                    }
                }

            val sub1 = natsService.loadBalanceSubscribe(
                "lb-publish", "lb-group", lbMessageHandler1,
                SubscriptionOptionsUtils.manualAckWithRateLimit(1)
            )
            val sub2 = natsService.loadBalanceSubscribe(
                "lb-publish", "lb-group", lbMessageHandler2,
                SubscriptionOptionsUtils.manualAckWithRateLimit(1)
            )

            repeat(10) {
                natsService.publish("lb-publish", "lb limit message-$it".toByteArray())
            }
            sub1.unsubscribe()
            sub2.unsubscribe()
        }
    }

    private fun testRequestReply(natsService: BlueprintNatsService) {
        runBlocking {
            val lbMessageHandler1 = io.nats.client.MessageHandler { message ->
                println("LB RR Request Handler 1: ${String(message.data)} will reply to(${message.replyTo})")
                message.connection.publish(
                    message.replyTo,
                    "Notification ${String(message.data)} reply from 1".toByteArray()
                )
            }

            val lbMessageHandler2 = io.nats.client.MessageHandler { message ->
                println("LB RR Request Handler 2: ${String(message.data)} will reply to(${message.replyTo})")
                message.connection.publish(
                    message.replyTo,
                    "Notification ${String(message.data)} reply from 2".toByteArray()
                )
            }

            natsService.loadBalanceReplySubscribe("rr-request", "rr-group", lbMessageHandler1)
            natsService.loadBalanceReplySubscribe("rr-request", "rr-group", lbMessageHandler2)

            repeat(5) {
                val message = natsService.requestAndGetOneReply(
                    "rr-request",
                    "rr message-$it".toByteArray(),
                    1000
                )
                println("Received : ${message.strData()}")
            }
        }
    }

    private fun testMultiRequestReply(natsService: BlueprintNatsService) {
        runBlocking {
            /** Request Reply **/
            val lbMessageHandler1 = io.nats.client.MessageHandler { message ->
                println("LB RR Request Handler 1: ${String(message.data)} will reply to(${message.replyTo})")
                message.connection.publish(
                    message.replyTo,
                    "Notification ${message.strData()} reply from 1".toByteArray()
                )
                message.connection.publish(
                    message.replyTo,
                    "Completion ${message.strData()} reply from 1".toByteArray()
                )
            }
            val lbMessageHandler2 = io.nats.client.MessageHandler { message ->
                println("LB RR Request Handler 2: ${message.strData()} will reply to(${message.replyTo})")
                message.connection.publish(
                    message.replyTo,
                    "Notification ${message.strData()} reply from 2".toByteArray()
                )
                message.connection.publish(
                    message.replyTo,
                    "Completion ${message.strData()} reply from 2".toByteArray()
                )
            }

            natsService.loadBalanceReplySubscribe("rr-request", "rr-group", lbMessageHandler1)
            natsService.loadBalanceReplySubscribe("rr-request", "rr-group", lbMessageHandler2)

            /** Should unsubscribe on completion message */
            val rrReplyMessageHandler = io.nats.client.MessageHandler { message ->
                val messageContent = message.strData()
                println("RR Reply Handler : $messageContent")
                if (messageContent.startsWith("Completion")) {
                    message.subscription.unsubscribe()
                }
            }
            repeat(5) {
                natsService.requestAndGetMultipleReplies(
                    "rr-request",
                    "rr-reply-$it",
                    "rr message-$it".toByteArray(),
                    rrReplyMessageHandler
                )
            }
        }
    }
}
