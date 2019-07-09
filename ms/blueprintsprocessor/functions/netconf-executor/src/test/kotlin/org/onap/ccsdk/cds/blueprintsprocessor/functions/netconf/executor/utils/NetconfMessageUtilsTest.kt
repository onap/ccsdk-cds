package org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.utils

import org.junit.Assert.*
import org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor.api.NetconfException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import kotlin.test.Test
import kotlin.test.assertFailsWith

class NetconfMessageUtilsTest {

    @Test
    fun `test getConfig with all parameters present`() {
        val outcome = NetconfMessageUtils.getConfig("customMessageId", "customConfigType", "customFilterContent")
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/getConfig-response-all-parameters.xml")
        assertEquals("getConfig return was not correct", expectation, outcome)
    }

    @Test
    fun `test getConfig with filterContent parameter null`() {
        val outcome = NetconfMessageUtils.getConfig("customMessageId", "customConfigType",null)
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/getConfig-response-filterContent-null.xml")
        assertEquals("getConfig return was not correct", expectation, outcome)
    }

    @Test
    fun `test doWrappedRpc`() {
        val outcome = NetconfMessageUtils.doWrappedRpc("customMessageId", "customRequest")
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/doWrappedRpc-response.xml")
        assertEquals("doWrappedRpc return was not correct", expectation, outcome)
    }

    @Test
    fun `test editConfig with all parameters present`() {
        val outcome = NetconfMessageUtils.editConfig("customMessageId", "customConfigType", "customDefaultOperation",
                "customNewConfiguration")
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/editConfig-response-all-parameters.xml")
        assertEquals("editConfig return was not correct", expectation, outcome)
    }

    @Test
    fun `test editConfig with defaultOperation parameter null`() {
        val outcome = NetconfMessageUtils.editConfig("customMessageId", "customConfigType", null,
                "customNewConfiguration")
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/editConfig-response-defaultOperation-null.xml")
        assertEquals("editConfig return was not correct", expectation, outcome)
    }

    @Test
    fun `test validate`() {
        val outcome = NetconfMessageUtils.validate("customMessageId", "customConfigType")
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/validate-response.xml")
        assertEquals("validate return was not correct", expectation, outcome)
    }

    @Test
    fun `test commit with both persistId and persist non-empty`() {
        assertFailsWith(exceptionClass = NetconfException::class, message = "commit should have thrown an exception") {
            NetconfMessageUtils.commit("customMessageId", false, 1, "customPersist", "customPersistId")
        }
    }

    @Test
    fun `test commit with confirmed true, persist empty and persistId non-empty`() {
        assertFailsWith(exceptionClass = NetconfException::class, message = "commit should have thrown an exception") {
            NetconfMessageUtils.commit("customMessageId", true, 1, "", "customPersistId")
        }
    }

    @Test
    fun `test commit with confirmed true, persistId empty and persist empty`() {
        val outcome = NetconfMessageUtils.commit("customMessageId", true, 1, "", "")
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/commit-response-confirmed-true-and-persistId-empty-and-persist-empty.xml")
        assertEquals("commit return was not correct", expectation, outcome)
    }

    @Test
    fun `test commit with confirmed false, persistId non-empty and persist empty`() {
        val outcome = NetconfMessageUtils.commit("customMessageId", false, 1, "", "customPersistId")
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/commit-response-confirmed-false-and-persistId-empty-and-persist-not-empty.xml")
        assertEquals("commit return was not correct", expectation, outcome)
    }

    @Test
    fun `test commit with confirmed false, persistId empty and persist non-empty`() {
        val outcome = NetconfMessageUtils.commit("customMessageId", false, 1, "customPersist", "")
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/commit-response-confirmed-false-and-persistId-not-empty-and-persist-empty.xml")
        assertEquals("commit return was not correct", expectation, outcome)
    }

    @Test
    fun `test cancelCommit with all parameters not empty`() {
        val outcome = NetconfMessageUtils.cancelCommit("customMessageId", "customPersistId")
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/cancelCommit-response-all-parameters-not-empty.xml")
        assertEquals("cancelCommit return was not correct", expectation, outcome)
    }

    @Test
    fun `test cancelCommit with persistId empty`() {
        val outcome = NetconfMessageUtils.cancelCommit("customMessageId", "")
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/cancelCommit-response-persistId-empty.xml")
        assertEquals("cancelCommit return was not correct", expectation, outcome)
    }

    @Test
    fun `test unlock with all parameters not empty`() {
        val outcome = NetconfMessageUtils.unlock("customMessageId", "customConfigType")
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/unlock-response-all-parameters-not-empty.xml")
        assertEquals("unlock return was not correct", expectation, outcome)
    }

    @Test
    fun `test deleteConfig with all parameters not empty`() {
        val outcome = NetconfMessageUtils.deleteConfig("customMessageId", "customConfigType")
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/deleteConfig-response-all-parameters-not-empty.xml")
        assertEquals("deleteConfig return was not correct", expectation, outcome)
    }

    @Test
    fun `test deleteConfig with configType equals to NetconfDatastore_RUNNING_datastore`() {
        assertFailsWith(exceptionClass = NetconfException::class, message = "deleteConfig should have thrown an exception") {
            NetconfMessageUtils.deleteConfig("customMessageId", NetconfDatastore.RUNNING.datastore)
        }
    }

    @Test
    fun `test discardChanges with all parameters not empty`() {
        val outcome = NetconfMessageUtils.discardChanges("customMessageId")
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/discardChanges-response-all-parameters-not-empty.xml")
        assertEquals("discardChanges return was not correct", expectation, outcome)
    }

    @Test
    fun `test lock with all parameters not empty`() {
        val outcome = NetconfMessageUtils.lock("customMessageId", "customConfigType")
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/lock-response-all-parameters-not-empty.xml")
        assertEquals("lock return was not correct", expectation, outcome)
    }

    @Test
    fun `test closeSession with force true`() {
        val outcome = NetconfMessageUtils.closeSession("customMessageId", true)
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/closeSession-response-force-true.xml")
        assertEquals("closeSession return was not correct", expectation, outcome)
    }

    @Test
    fun `test closeSession with force false`() {
        val outcome = NetconfMessageUtils.closeSession("customMessageId", false)
        val expectation = JacksonUtils.getClassPathFileContent("netconf-messages/closeSession-response-force-false.xml")
        assertEquals("closeSession return was not correct", expectation, outcome)
    }

    //TODO validateRPCXML

    @Test
    fun `test getMsgId with valid message`() {
        val messageId = "1234"
        val outcome = NetconfMessageUtils.getMsgId("message-id=\"${messageId}\"")
        val expectation = messageId
        assertEquals("getMsgId return was not correct", expectation, outcome)
    }

    @Test
    fun `test getMsgId with RpcMessageUtils_HELLO message`() {
        val outcome = NetconfMessageUtils.getMsgId(RpcMessageUtils.HELLO)
        val expectation = "-1"
        assertEquals("getMsgId return was not correct", expectation, outcome)
    }

    @Test
    fun `test getMsgId with invalid message`() {
        val outcome = NetconfMessageUtils.getMsgId("invalid message")
        val expectation = ""
        assertEquals("getMsgId return was not correct", expectation, outcome)
    }

//TODO validateChunkedFraming

}
