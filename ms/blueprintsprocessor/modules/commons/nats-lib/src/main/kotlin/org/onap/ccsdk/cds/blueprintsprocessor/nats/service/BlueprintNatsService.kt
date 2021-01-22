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

@file:Suppress("BlockingMethodInNonBlockingContext")

package org.onap.ccsdk.cds.blueprintsprocessor.nats.service

import io.nats.client.Dispatcher
import io.nats.streaming.MessageHandler
import io.nats.streaming.StreamingConnection
import io.nats.streaming.Subscription
import io.nats.streaming.SubscriptionOptions
import java.time.Duration

interface BlueprintNatsService {

    /** Create and Return the NATS streaming connection */
    suspend fun connection(): StreamingConnection

    /** Send one request [message] to the [subject] and get only one reply
     * The request message subscriber may be multi instances consumer or load balance consumer.
     * If it is multi instances consumer, then we will get only first responses from subscribers.
     *
     */
    suspend fun requestAndGetOneReply(subject: String, message: ByteArray, timeout: Long): io.nats.client.Message {
        return connection().natsConnection.request(subject, message, Duration.ofMillis(timeout))
    }

    /** Send one request [message] to the [subject] and get multiple replies in [replySubject] with [messageHandler]
     * The request message subscriber may be multi instances consumer or load balance consumer.
     * If it is multi instances consumer, then we will get multiple responses from subscribers.
     * Include the unSubscribe logic's in [messageHandler] implementation.
     */
    suspend fun requestAndGetMultipleReplies(
        subject: String,
        replySubject: String,
        message: ByteArray,
        messageHandler: io.nats.client.MessageHandler
    ) {
        val natsConnection = connection().natsConnection
        val dispatcher = natsConnection.createDispatcher(messageHandler)
        /** Reply subject consumer */
        dispatcher.subscribe(replySubject)

        /** Publish the request message and expect the reply messages in reply subject consumer */
        natsConnection.publish(subject, replySubject, message)
    }

    /** Synchronous reply Subscribe the [subject] with the [messageHandler].
     * This is used only the message has to be consumed by all instances in the cluster and message handler must reply.
     */
    suspend fun replySubscribe(
        subject: String,
        messageHandler: io.nats.client.MessageHandler
    ): Dispatcher {
        val natsConnection = connection().natsConnection
        val dispatcher = natsConnection.createDispatcher(messageHandler)
        return dispatcher.subscribe(subject)
    }

    /**
     * Synchronous reply Subscriber will listen for [subject] with [loadBalanceGroup].
     * This is used only the message has to be consumed by only one instance in the cluster.
     * server will now load balance messages between the members of the queue group and message handler must reply.
     */
    suspend fun loadBalanceReplySubscribe(
        subject: String,
        loadBalanceGroup: String,
        messageHandler: io.nats.client.MessageHandler
    ): Dispatcher {
        val natsConnection = connection().natsConnection
        val dispatcher = natsConnection.createDispatcher(messageHandler)
        return dispatcher.subscribe(subject, loadBalanceGroup)
    }

    /** Publish the [message] to all subscribers on the [subject] */
    suspend fun publish(subject: String, message: ByteArray) {
        connection().publish(subject, message)
    }

    /** Subscribe the [subject] with the [messageHandler].
     * This is used only the message has to be consumed by all instances in the cluster.
     */
    suspend fun subscribe(
        subject: String,
        messageHandler: MessageHandler
    ): Subscription {
        return connection().subscribe(subject, messageHandler)
    }

    /** Subscribe the [subject] with the [messageHandler] and [subscriptionOptions].
     * This is used only the message has to be consumed by all instances in the cluster.
     */
    suspend fun subscribe(
        subject: String,
        messageHandler: MessageHandler,
        subscriptionOptions: SubscriptionOptions
    ): Subscription {
        return connection().subscribe(subject, messageHandler, subscriptionOptions)
    }

    /**
     * https://docs.nats.io/developing-with-nats/receiving/queues
     * subscribers will listen for [subject] with [loadBalanceGroup].
     * This is used only the message has to be consumed by only one instance in the cluster.
     * server will now load balance messages between the members of the queue group.
     */
    suspend fun loadBalanceSubscribe(
        subject: String,
        loadBalanceGroup: String,
        messageHandler: MessageHandler
    ): Subscription {
        return connection().subscribe(subject, loadBalanceGroup, messageHandler)
    }

    /**
     * https://docs.nats.io/developing-with-nats/receiving/queues
     * subscribers will listen for [subject] with [loadBalanceGroup] and [subscriptionOptions].
     * This is used only the message has to be consumed by only one instance in the cluster.
     * server will now load balance messages between the members of the queue group.
     */
    suspend fun loadBalanceSubscribe(
        subject: String,
        loadBalanceGroup: String,
        messageHandler: MessageHandler,
        subscriptionOptions: SubscriptionOptions
    ): Subscription {
        return connection().subscribe(subject, loadBalanceGroup, messageHandler, subscriptionOptions)
    }
}
