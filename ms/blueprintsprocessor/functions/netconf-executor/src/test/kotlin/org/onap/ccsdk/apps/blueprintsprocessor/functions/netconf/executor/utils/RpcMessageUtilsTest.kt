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
package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.utils

import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import org.springframework.beans.factory.annotation.Autowired

class RpcMessageUtilsTest {

    @Test
    fun getConfig() {
        val checkString = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<get-config><source><candidate/></source><filter type=\"subtree\">Test-Filter-Content</filter>"
                + "</get-config></rpc>")

        val messageId = "Test-Message-ID"
        val configType = "candidate"
        val filterContent = "Test-Filter-Content"

        val result = RpcMessageUtils.getConfig(messageId, configType, filterContent).replace("[\n\r\t]".toRegex(), "")

        assertTrue(RpcMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)
    }



    @Test
    fun editConfig() {
        val checkString = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<get-config><source><candidate/></source><filter type=\"subtree\">Test-Filter-Content</filter>"
                + "</get-config></rpc>")

        val messageId = "Test-Message-ID"
        val configType = "candidate"
        val filterContent = "Test-Filter-Content"

        val result = RpcMessageUtils.getConfig(messageId, configType, filterContent).replace("[\n\r\t]".toRegex(), "")

        assertTrue(RpcMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)
    }

    @Test
    fun validate() {
        val checkString = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<validate><source><candidate/></source></validate></rpc>")

        val messageId = "Test-Message-ID"
        val configType = "candidate"

        val result = RpcMessageUtils.validate(messageId, configType).replace("[\n\r\t]".toRegex(), "")

        assertTrue(RpcMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)
    }

    @Test
    fun commit() {
        val checkString = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<commit></commit></rpc>")

        val messageId = "Test-Message-ID"
        val message = "Test-Message"

        val result = RpcMessageUtils.commit(messageId, message).replace("[\n\r\t]".toRegex(), "")

        assertTrue(RpcMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)

    }

    @Test
    fun unlock() {
        val checkString = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<unlock><target><candidate/></target></unlock></rpc>")

        val messageId = "Test-Message-ID"
        val configType = "candidate"

        val result = RpcMessageUtils.unlock(messageId, configType).replace("[\n\r\t]".toRegex(), "")

        assertTrue(RpcMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)
    }

    @Test
    fun deleteConfig() {
        val checkString = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<delete-config><target><candidate/></target></delete-config></rpc>")

        val messageId = "Test-Message-ID"
        val netconfTargetConfig = "candidate"

        val result = RpcMessageUtils.deleteConfig(messageId, netconfTargetConfig).replace("[\n\r\t]".toRegex(), "")

        assertTrue(RpcMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)
    }

    @Test
    fun discardChanges() {
        val checkString = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<discard-changes/></rpc>")

        val messageId = "Test-Message-ID"

        val result = RpcMessageUtils.discardChanges(messageId).replace("[\n\r\t]".toRegex(), "")

        assertTrue(RpcMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)
    }

    @Test
    fun lock() {
        val checkString = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<rpc message-id=\"Test-Message-ID\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<lock><target><candidate/></target></lock></rpc>")

        val messageId = "Test-Message-ID"
        val configType = "candidate"
        val result = RpcMessageUtils.lock(messageId, configType).replace("[\n\r\t]".toRegex(), "")

        assertTrue(RpcMessageUtils.validateRPCXML(result))
        Assert.assertEquals(checkString, result)
    }


}