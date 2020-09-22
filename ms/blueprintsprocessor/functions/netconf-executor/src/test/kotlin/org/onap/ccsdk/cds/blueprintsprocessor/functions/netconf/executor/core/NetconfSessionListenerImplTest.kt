/*
 * Copyright © 2019 Bell Canada
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

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfReceivedEvent

class NetconfSessionListenerImplTest {

    // Note: mockk's verifyAll is akin to verify with verifyNoMoreInteractions in Mockito
    private val netconSession = mockk<NetconfSessionImpl>()

    @Before
    fun init() {
        every { netconSession.disconnect() } returns Unit
        every { netconSession.addDeviceErrorReply(any()) } returns Unit
        every { netconSession.addDeviceReply(any(), any()) } returns Unit
    }

    @Test
    // NetconfReceivedEvent wth DEVICE_UNREGISTERED TYPE should call disconnect() on the NetconfSession
    fun deviceUnregisteredMessageShouldCallSessionDisconnect() {
        val netconfSessionListener = NetconfSessionListenerImpl(netconSession)
        val event: NetconfReceivedEvent = genEventByType(NetconfReceivedEvent.Type.DEVICE_UNREGISTERED)
        netconfSessionListener.accept(event)
        verifyAll { netconSession.disconnect() }
    }

    @Test
    // NetconfReceivedEvent wth SESSION_CLOSED TYPE should ALSO call disconnect() on the NetconfSession
    fun sessionClosedMessageShouldCallSesionDisconnect() {
        val netconfSessionListener = NetconfSessionListenerImpl(netconSession)
        val event: NetconfReceivedEvent = genEventByType(NetconfReceivedEvent.Type.SESSION_CLOSED)
        netconfSessionListener.accept(event)
        verifyAll { netconSession.disconnect() }
    }

    @Test
    // NetconfReceivedEvent wth DEVICE_ERROR TYPE should call addDeviceErrorReply() on the NetconfSession
    // with the event message payload
    fun deviceErrorMessageShouldCallAddDeviceErrorReply() {
        val netconfSessionListener = NetconfSessionListenerImpl(netconSession)
        val event: NetconfReceivedEvent = genEventByType(NetconfReceivedEvent.Type.DEVICE_ERROR)
        netconfSessionListener.accept(event)
        verifyAll { netconSession.addDeviceErrorReply(event.messagePayload) }
    }

    @Test
    // NetconfReceivedEvent wth DEVICE_REPLY TYPE should call addDeviceReply(messageId, payload) on the NetconfSession
    fun deviceReplyMessageShouldCallAddDeviceReply() {
        val netconfSessionListener = NetconfSessionListenerImpl(netconSession)
        val event: NetconfReceivedEvent = genEventByType(NetconfReceivedEvent.Type.DEVICE_REPLY)
        netconfSessionListener.accept(event)
        verifyAll { netconSession.addDeviceReply(event.messageId, event.messagePayload) }
    }

    /**
     * Helper to generate {@link NetconfReceivedEvent} object based on the {@link NetconfReceivedEvent.Type}
     * @param type {@link NetconfReceivedEvent.Type} of event
     */
    private fun genEventByType(type: NetconfReceivedEvent.Type): NetconfReceivedEvent {
        return NetconfReceivedEvent(
            type,
            "messagePayload",
            "messageId",
            DeviceInfo()
        )
    }
}
