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

import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.DeviceInfo
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfReceivedEvent
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfSession
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfSessionListener
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils.RpcMessageUtils
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NetconfDeviceCommunicatorTest {
    private lateinit var netconfSession: NetconfSession
    private lateinit var netconfSessionListener: NetconfSessionListener
    private lateinit var mockInputStream: InputStream
    private lateinit var mockOutputStream: OutputStream
    private lateinit var stubInputStream: InputStream
    private lateinit var replies: MutableMap<String, CompletableFuture<String>>
    private val endPatternCharArray: List<Int> = stringToCharArray(RpcMessageUtils.END_PATTERN)

    companion object {
        private val chunkedEnding = "\n##\n"
        // using example from section 4.2 of RFC6242 (https://tools.ietf.org/html/rfc6242#section-4.2)
        private val validChunkedEncodedMsg = """
            |
            |#4
            |<rpc
            |#18
            | message-id="102"
            |
            |#79
            |     xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
            |  <close-session/>
            |</rpc>
            |##
            |""".trimMargin()
    }

    private fun stringToCharArray(str: String): List<Int> {
        return str.toCharArray().map(Char::toInt)
    }

    @Before
    fun setup() {
        netconfSession = mockk()
        netconfSessionListener = mockk()
        mockInputStream = mockk()
        mockOutputStream = mockk()
        replies = ConcurrentHashMap()
    }

    @Test
    fun `NetconfDeviceCommunicator should read from supplied reader`() {
        every { mockInputStream.read() } returns -1
        every { mockInputStream.read(any(), any(), any()) } returns -1
        val communicator: NetconfDeviceCommunicator =
            NetconfDeviceCommunicator(mockInputStream, mockOutputStream, genDeviceInfo(), netconfSessionListener, replies)
        communicator.join()
        // verify
        verify { mockInputStream.read(any(), any(), any()) }
    }

    @Test
    fun `NetconfDeviceCommunicator unregisters device on END_PATTERN`() {
        // The reader will generate RpcMessageUtils.END_PATTERN "]]>]]>" which tells Netconf
        // to unregister the device.
        // we want to capture the slot to return the value as inputStreamReader will pass a char array
        // create a slot where NetconfReceivedEvent will be placed to further verify Type.DEVICE_UNREGISTERED
        val eventSlot = CapturingSlot<NetconfReceivedEvent>()
        every { netconfSessionListener.accept(event = capture(eventSlot)) } just Runs
        stubInputStream = RpcMessageUtils.END_PATTERN.byteInputStream(StandardCharsets.UTF_8)
        val inputStreamSpy = spyk(stubInputStream)
        // RUN the test
        val communicator = NetconfDeviceCommunicator(
            inputStreamSpy, mockOutputStream,
            genDeviceInfo(), netconfSessionListener, replies
        )
        communicator.join()
        // Verify
        verify { inputStreamSpy.close() }
        assertTrue { eventSlot.isCaptured }
        assertEquals(NetconfReceivedEvent.Type.DEVICE_UNREGISTERED, eventSlot.captured.type)
        assertEquals(genDeviceInfo(), eventSlot.captured.deviceInfo)
    }

    @Test
    fun `NetconfDeviceCommunicator on IOException generated DEVICE_ERROR event`() {
        val eventSlot = CapturingSlot<NetconfReceivedEvent>()
        every { netconfSessionListener.accept(event = capture(eventSlot)) } just Runs
        stubInputStream = "".byteInputStream(StandardCharsets.UTF_8)
        val inputStreamSpy = spyk(stubInputStream)
        every { inputStreamSpy.read(any(), any(), any()) } returns 1 andThenThrows IOException("Fake IO Exception")
        // RUN THE TEST
        val communicator = NetconfDeviceCommunicator(
            inputStreamSpy, mockOutputStream,
            genDeviceInfo(), netconfSessionListener, replies
        )
        communicator.join()
        // Verify
        assertTrue { eventSlot.isCaptured }
        assertEquals(genDeviceInfo(), eventSlot.captured.deviceInfo)
        assertEquals(NetconfReceivedEvent.Type.DEVICE_ERROR, eventSlot.captured.type)
    }

    @Test
    fun `NetconfDeviceCommunicator in END_PATTERN state but fails RpcMessageUtils end pattern validation`() {
        val eventSlot = CapturingSlot<NetconfReceivedEvent>()
        val payload = "<rpc-reply>blah</rpc-reply>"
        stubInputStream = "$payload${RpcMessageUtils.END_PATTERN}".byteInputStream(StandardCharsets.UTF_8)
        every { netconfSessionListener.accept(event = capture(eventSlot)) } just Runs
        // RUN the test
        val communicator = NetconfDeviceCommunicator(
            stubInputStream, mockOutputStream,
            genDeviceInfo(), netconfSessionListener, replies
        )
        communicator.join()
        // Verify
        verify(exactly = 0) { mockInputStream.close() } // make sure the reader is not closed as this could cause problems
        assertTrue { eventSlot.isCaptured }
        // eventually, sessionListener is called with message type DEVICE_REPLY
        assertEquals(NetconfReceivedEvent.Type.DEVICE_REPLY, eventSlot.captured.type)
        assertEquals(payload, eventSlot.captured.messagePayload)
    }

    @Test
    fun `NetconfDeviceCommunicator in END_CHUNKED_PATTERN but validation failing produces DEVICE_ERROR`() {
        val eventSlot = CapturingSlot<NetconfReceivedEvent>()
        val payload = "<rpc-reply>blah</rpc-reply>"
        val payloadWithChunkedEnding = "$payload$chunkedEnding"
        every { netconfSessionListener.accept(event = capture(eventSlot)) } just Runs

        stubInputStream = payloadWithChunkedEnding.byteInputStream(StandardCharsets.UTF_8)
        // we have to ensure that the input stream is processed, so need to create a spy object.
        val inputStreamSpy = spyk(stubInputStream)
        // RUN the test
        val communicator = NetconfDeviceCommunicator(
            inputStreamSpy, mockOutputStream, genDeviceInfo(),
            netconfSessionListener, replies
        )
        communicator.join()
        // Verify
        verify(exactly = 0) { inputStreamSpy.close() } // make sure the reader is not closed as this could cause problems
        assertTrue { eventSlot.isCaptured }
        // eventually, sessionListener is called with message type DEVICE_REPLY
        assertEquals(NetconfReceivedEvent.Type.DEVICE_ERROR, eventSlot.captured.type)
        assertEquals("", eventSlot.captured.messagePayload)
    }

    @Test
    fun `NetconfDeviceCommunicator in END_CHUNKED_PATTERN passing validation generates DEVICE_REPLY`() {
        val eventSlot = CapturingSlot<NetconfReceivedEvent>()
        stubInputStream = validChunkedEncodedMsg.byteInputStream(StandardCharsets.UTF_8)
        val inputStreamSpy = spyk(stubInputStream)
        every { netconfSessionListener.accept(event = capture(eventSlot)) } just Runs
        // RUN the test
        NetconfDeviceCommunicator(inputStreamSpy, mockOutputStream, genDeviceInfo(), netconfSessionListener, replies).join()
        // Verify
        verify(exactly = 0) { inputStreamSpy.close() } // make sure the reader is not closed as this could cause problems
        assertTrue { eventSlot.isCaptured }
        // eventually, sessionListener is called with message type DEVICE_REPLY
        assertEquals(NetconfReceivedEvent.Type.DEVICE_REPLY, eventSlot.captured.type)
        assertEquals(
            """
        <rpc message-id="102"
             xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
          <close-session/>
        </rpc>
            """.trimIndent(),
            eventSlot.captured.messagePayload
        )
    }

    @Test
    // test to ensure that we have a valid test message to be then used in the case of chunked message
    // validation code path
    fun `chunked sample is validated by the chunked response regex`() {
        val test1 = "\n#10\nblah\n##\n"
        val chunkedFramingPattern = Pattern.compile("(\\n#([1-9][0-9]*)\\n(.+))+\\n##\\n", Pattern.DOTALL)
        val matcher = chunkedFramingPattern.matcher(test1)
        assertTrue { matcher.matches() }
    }

    @Test
    // Verify that our test sample passes the second pattern for chunked size
    fun `chunkSizeMatcher pattern finds matches in chunkedMessageSample`() {
        val sizePattern = Pattern.compile("\\n#([1-9][0-9]*)\\n")
        val matcher = sizePattern.matcher(validChunkedEncodedMsg)
        assertTrue { matcher.find() }
    }

    @Test
    fun `sendMessage writes the request to NetconfDeviceCommunicator Writer`() {
        val msgPayload = "some text"
        val msgId = "100"
        stubInputStream = "".byteInputStream(StandardCharsets.UTF_8) // no data available in the stream...
        every { mockOutputStream.write(any(), any(), any()) } just Runs
        every { mockOutputStream.write(msgPayload.toByteArray(Charsets.UTF_8)) } just Runs
        every { mockOutputStream.flush() } just Runs
        // Run the command
        val communicator = NetconfDeviceCommunicator(
            stubInputStream, mockOutputStream,
            genDeviceInfo(), netconfSessionListener, replies
        )
        val completableFuture = communicator.sendMessage(msgPayload, msgId)
        communicator.join()
        // verify
        verify { mockOutputStream.write(any(), any(), any()) }
        verify { mockOutputStream.flush() }
        assertFalse { completableFuture.isCompletedExceptionally }
    }

    @Test
    fun `sendMessage on IOError returns completed exceptionally future`() {
        val msgPayload = "some text"
        val msgId = "100"
        every { mockOutputStream.write(any(), any(), any()) } throws IOException("Some IO error occurred!")
        stubInputStream = "".byteInputStream(StandardCharsets.UTF_8) // no data available in the stream...
        // Run the command
        val communicator = NetconfDeviceCommunicator(
            stubInputStream, mockOutputStream,
            genDeviceInfo(), netconfSessionListener, replies
        )
        val completableFuture = communicator.sendMessage(msgPayload, msgId)
        // verify
        verify { mockOutputStream.write(any(), any(), any()) }
        verify(exactly = 0) { mockOutputStream.flush() }
        assertTrue { completableFuture.isCompletedExceptionally }
    }

    private fun genDeviceInfo(): DeviceInfo {
        return DeviceInfo().apply {
            username = "user"
            password = "pass"
            ipAddress = "localhost"
            port = 4567
        }
    }
}
