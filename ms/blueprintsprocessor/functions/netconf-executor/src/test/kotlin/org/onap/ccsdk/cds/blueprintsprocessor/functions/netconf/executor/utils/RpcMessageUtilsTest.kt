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
package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils

import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfException
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.fail

class RpcMessageUtilsTest {

    @Test
    fun getConfig() {
        val checkString = (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<get-config><source><candidate/></source><filter type=\"subtree\">Test-Filter-Content</filter>" +
                "</get-config></rpc>"
            )

        val messageId = "Test-Message-ID"
        val configType = NetconfDatastore.CANDIDATE.datastore
        val filterContent = "Test-Filter-Content"

        val result =
            NetconfMessageUtils.getConfig(messageId, configType, filterContent).replace("[\n\r\t]".toRegex(), "")

        assertTrue(NetconfMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)
    }

    @Test
    fun editConfig() {
        val checkString = (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<edit-config><target><candidate/></target><default-operation>Test-Default-Operation</default-operation>" +
                "<config xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">Test-Filter-Content</config></edit-config></rpc>"
            )

        val messageId = "Test-Message-ID"
        val configType = NetconfDatastore.CANDIDATE.datastore
        val filterContent = "Test-Filter-Content"
        val defaultOperation = "Test-Default-Operation"

        val result =
            NetconfMessageUtils.editConfig(messageId, configType, defaultOperation, filterContent).replace("[\n\r\t]".toRegex(), "")

        assertTrue(NetconfMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)
    }

    @Test
    fun validate() {
        val checkString = (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<validate><source><candidate/></source></validate></rpc>"
            )

        val messageId = "Test-Message-ID"
        val configType = NetconfDatastore.CANDIDATE.datastore

        val result = NetconfMessageUtils.validate(messageId, configType).replace("[\n\r\t]".toRegex(), "")

        assertTrue(NetconfMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)
    }

    @Test
    fun cancelCommit() {
        val checkString =
            (
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                    "<cancel-commit>" +
                    "<persist-id>1234</persist-id>" +
                    "</cancel-commit></rpc>"
                )

        val messageId = "Test-Message-ID"

        val cancelCommitPersistId =
            NetconfMessageUtils.cancelCommit(messageId, "1234").replace("[\n\r\t]".toRegex(), "")

        assertTrue(NetconfMessageUtils.validateRPCXML(cancelCommitPersistId))
        Assert.assertEquals(checkString, cancelCommitPersistId)
    }

    @Test
    fun cancelCommitNoPersistId() {
        val checkString =
            (
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                    "<cancel-commit>" +
                    "</cancel-commit></rpc>"
                )

        val messageId = "Test-Message-ID"

        val cancelCommitNoPersistId = NetconfMessageUtils.cancelCommit(messageId, "").replace("[\n\r\t]".toRegex(), "")

        assertTrue(NetconfMessageUtils.validateRPCXML(cancelCommitNoPersistId))
        Assert.assertEquals(checkString, cancelCommitNoPersistId)
    }

    @Test
    fun commit() {
        val checkString = (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<commit></commit></rpc>"
            )

        val messageId = "Test-Message-ID"

        val commit = NetconfMessageUtils.commit(messageId, false, 0, "", "").replace("[\n\r\t]".toRegex(), "")

        val commitWithPersistButNotConfirmed =
            NetconfMessageUtils.commit(messageId, false, 0, "1234", "").replace("[\n\r\t]".toRegex(), "")

        assertTrue(NetconfMessageUtils.validateRPCXML(commit))
        Assert.assertEquals(checkString, commit)
        Assert.assertEquals(checkString, commitWithPersistButNotConfirmed)
    }

    @Test
    fun commitPersistId() {
        val checkString =
            (
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                    "<commit>" +
                    "<persist-id>1234</persist-id>" +
                    "</commit></rpc>"
                )

        val messageId = "Test-Message-ID"

        val result = NetconfMessageUtils.commit(messageId, false, 30, "", "1234").replace("[\n\r\t]".toRegex(), "")
        assertTrue(NetconfMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)

        try {
            NetconfMessageUtils.commit(messageId, true, 30, "", "1234").replace("[\n\r\t]".toRegex(), "")
        } catch (e: NetconfException) {
            Assert.assertEquals(
                "Can't proceed <commit> with both confirmed flag and persistId(1234) specified. Only one should be specified.",
                e.message
            )
            return
        }

        fail()
    }

    @Test
    fun commitPersist() {
        val checkString =
            (
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                    "<commit>" +
                    "<confirmed/>" +
                    "<confirm-timeout>30</confirm-timeout>" +
                    "<persist>1234</persist>" +
                    "</commit></rpc>"
                )

        val messageId = "Test-Message-ID"

        val result = NetconfMessageUtils.commit(messageId, true, 30, "1234", "").replace("[\n\r\t]".toRegex(), "")

        assertTrue(NetconfMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)

        try {
            NetconfMessageUtils.commit(messageId, false, 30, "1234", "1234").replace("[\n\r\t]".toRegex(), "")
        } catch (e: NetconfException) {
            Assert.assertEquals(
                "Can't proceed <commit> with both persist(1234) and persistId(1234) specified. Only one should be specified.",
                e.message
            )
            return
        }
        fail()
    }

    @Test
    fun unlock() {
        val checkString = (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<unlock><target><candidate/></target></unlock></rpc>"
            )

        val messageId = "Test-Message-ID"
        val configType = NetconfDatastore.CANDIDATE.datastore

        val result = NetconfMessageUtils.unlock(messageId, configType).replace("[\n\r\t]".toRegex(), "")

        assertTrue(NetconfMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)
    }

    @Test
    fun deleteConfig() {
        val checkString = (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<delete-config><target><candidate/></target></delete-config></rpc>"
            )

        val messageId = "Test-Message-ID"
        val netconfTargetConfig = NetconfDatastore.CANDIDATE.datastore

        val result = NetconfMessageUtils.deleteConfig(messageId, netconfTargetConfig).replace("[\n\r\t]".toRegex(), "")

        assertTrue(NetconfMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)
    }

    @Test
    fun deleteConfigThrowsNetconfExceptionOnRunningDataStore() {
        assertFailsWith(exceptionClass = NetconfException::class) {
            val netconfTargetConfig = NetconfDatastore.RUNNING.datastore
            val msgId = "35"
            NetconfMessageUtils.deleteConfig(msgId, netconfTargetConfig)
        }
    }

    @Test
    fun discardChanges() {
        val checkString = (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<discard-changes/></rpc>"
            )

        val messageId = "Test-Message-ID"

        val result = NetconfMessageUtils.discardChanges(messageId).replace("[\n\r\t]".toRegex(), "")

        assertTrue(NetconfMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)
    }

    @Test
    fun lock() {
        val checkString = (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<lock><target><candidate/></target></lock></rpc>"
            )

        val messageId = "Test-Message-ID"
        val configType = NetconfDatastore.CANDIDATE.datastore
        val result = NetconfMessageUtils.lock(messageId, configType).replace("[\n\r\t]".toRegex(), "")

        assertTrue(NetconfMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)
    }

    @Test
    fun getMsgId() {
        val checkString = ("testmessage")

        var messageId = "message-id=\"testmessage\""
        var result = NetconfMessageUtils.getMsgId(messageId).replace("[\n\r\t]".toRegex(), "")
        Assert.assertEquals(checkString, result)

        messageId = "message-id=\"hello\""
        result = NetconfMessageUtils.getMsgId(messageId).replace("[\n\r\t]".toRegex(), "")
        Assert.assertEquals("hello", result)

        messageId = "message-id"
        result = NetconfMessageUtils.getMsgId(messageId).replace("[\n\r\t]".toRegex(), "")
        Assert.assertEquals("", result)
    }

    @Test
    fun createHelloString() {
        val checkString = (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">  " +
                "<capabilities>    <capability>hi</capability>    <capability>hello</capability>  </capabilities></hello>]]>]]>"
            )

        val capability = listOf<String>("hi", "hello")

        val result = NetconfMessageUtils.createHelloString(capability).replace("[\n\r\t]".toRegex(), "")
        Assert.assertEquals(checkString, result)
    }

    @Test
    fun validateChunkedFraming() {
        val reply = ("hello")
        val result = NetconfMessageUtils.validateChunkedFraming(reply)
        Assert.assertFalse(result)
    }

    @Test
    fun `checkReply should return true on ok msg`() {
        assertTrue(NetconfMessageUtils.checkReply("ok"))
    }

    @Test
    fun `checkReply on rpc-error should return false`() {
        assertFalse { NetconfMessageUtils.checkReply("something something rpc-error>") }
    }

    @Test
    fun `checkReply on null input should return false`() {
        assertFalse { NetconfMessageUtils.checkReply(null) }
    }

    @Test
    fun formatRPCRequest() {
        val checkString = (
            "#199" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">  <capabilities>    <capability>hi</capability>    <capability>hello</capability>  </capabilities></hello>" +
                "##"
            )

        val request = (
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">  " +
                "<capabilities>    <capability>hi</capability>    <capability>hello</capability>  </capabilities></hello>]]>]]>"
            )

        val messageId = "Test-Message-ID"

        val capabilities = setOf<String>("hi", "hello", "urn:ietf:params:netconf:base:1.1")

        val result = NetconfMessageUtils.formatRPCRequest(request, messageId, capabilities).replace("[\n\r\t]".toRegex(), "")
        Assert.assertEquals(checkString, result)
    }

    @Test
    fun `validateRPCXML on empty input returns false`() {
        assertFalse { NetconfMessageUtils.validateRPCXML("") }
    }

    @Test
    fun `validateRPCXML on bad input returns false`() {
        println("Don't fear \"[Fatal Error] :1:1: Content is not allowed in prolog.\" TODO: adjust logging for NetconfMessageUtils")
        assertFalse { NetconfMessageUtils.validateRPCXML("really bad XML ~~~input") }
    }
}
