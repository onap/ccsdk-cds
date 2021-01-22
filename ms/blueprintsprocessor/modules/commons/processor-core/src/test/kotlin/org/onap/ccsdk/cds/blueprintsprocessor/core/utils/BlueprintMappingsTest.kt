package org.onap.ccsdk.cds.blueprintsprocessor.core.utils

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Flags
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.common.api.Flag
import org.onap.ccsdk.cds.controllerblueprints.core.utils.currentTimestamp
import org.springframework.test.context.junit4.SpringRunner
import java.text.SimpleDateFormat

@RunWith(SpringRunner::class)
class BlueprintMappingsTest {

    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val dateString = "2019-01-16T18:25:43.511Z"
    val dateForTest = formatter.parse(dateString)

    val flag = Flag.newBuilder().setIsForce(false).setTtl(1).build()

    fun createFlag(): Flags {
        val flag = Flags()
        flag.isForce = false
        flag.ttl = 1
        return flag
    }

    @Test
    fun flagToJavaTest() {
        val flag2 = flag.toJava()

        Assert.assertEquals(flag.isForce, flag2.isForce)
        Assert.assertEquals(flag.ttl, flag2.ttl)
    }

    @Test
    fun flagToProtoTest() {
        val flag = createFlag()
        val flag2 = flag.toProto()

        Assert.assertEquals(flag.isForce, flag2.isForce)
        Assert.assertEquals(flag.ttl, flag2.ttl)
    }

    fun createStatus(): org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Status {
        val status = org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Status()
        status.code = 400
        status.errorMessage = "Concurrent modification exception"
        status.eventType = EventType.EVENT_COMPONENT_PROCESSING.name
        status.message = "Error uploading data"
        status.timestamp = dateForTest
        return status
    }

    @Test
    fun statusToProtoTest() {
        val status = createStatus()
        val status2 = status.toProto()

        Assert.assertEquals(status.code, status2.code)
        Assert.assertEquals(status.errorMessage, status2.errorMessage)
        Assert.assertEquals(status.eventType, status2.eventType.name)
        Assert.assertEquals(status.message, status2.message)
        Assert.assertEquals(status.timestamp.toString(), status2.timestamp)
    }

    @Test
    fun commonHeaderToJavaTest() {
        val flag = Flag.newBuilder().setIsForce(true).setTtl(2).build()

        val commonHeader =
            CommonHeader.newBuilder().setOriginatorId("Origin").setRequestId("requestID").setSubRequestId("subRequestID").setTimestamp(dateString)
                .setFlag(flag).build()
        val commonHeader2 = commonHeader.toJava()

        Assert.assertEquals(commonHeader.originatorId, commonHeader2.originatorId)
        Assert.assertEquals(commonHeader.requestId, commonHeader2.requestId)
        Assert.assertEquals(commonHeader.subRequestId, commonHeader2.subRequestId)
        Assert.assertEquals(commonHeader.timestamp, formatter.format(commonHeader2.timestamp))
    }

    fun createCommonHeader(): org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader {
        val commonHeader = org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader()
        commonHeader.flags = createFlag()
        commonHeader.originatorId = "1234"
        commonHeader.requestId = "2345"
        commonHeader.subRequestId = "0123"
        commonHeader.timestamp = dateForTest
        return commonHeader
    }

    @Test
    fun commonHeaderToProtoTest() {
        val commonHeader = createCommonHeader()
        val commonHeader2 = commonHeader.toProto()
        Assert.assertEquals(commonHeader.originatorId, commonHeader2.originatorId)
        Assert.assertEquals(commonHeader.requestId, commonHeader2.requestId)
        Assert.assertEquals(commonHeader.subRequestId, commonHeader2.subRequestId)
        Assert.assertEquals(commonHeader.timestamp.currentTimestamp(), commonHeader2.timestamp)
    }

    @Test
    fun actionIdentifierToJavaTest() {
        val actionIdentifiers =
            ActionIdentifiers.newBuilder().setActionName("Process Action").setBlueprintName("BlueprintName").setBlueprintVersion("3.0")
                .setMode("Execution").build()
        val actionIdentifiers2 = actionIdentifiers.toJava()

        Assert.assertEquals(actionIdentifiers.actionName, actionIdentifiers2.actionName)
        Assert.assertEquals(actionIdentifiers.blueprintName, actionIdentifiers2.blueprintName)
        Assert.assertEquals(actionIdentifiers.blueprintVersion, actionIdentifiers2.blueprintVersion)
        Assert.assertEquals(actionIdentifiers.mode, actionIdentifiers2.mode)
    }

    fun createActionIdentifier(): org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers {
        val ac = org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers()
        ac.mode = "mode"
        ac.blueprintVersion = "version"
        ac.blueprintName = "name"
        ac.actionName = "action"
        return ac
    }

    @Test
    fun actionIdentifierToProtoTest() {
        val actionIdentifiers = createActionIdentifier()
        val actionIdentifiers2 = actionIdentifiers.toProto()

        Assert.assertEquals(actionIdentifiers.actionName, actionIdentifiers2.actionName)
        Assert.assertEquals(actionIdentifiers.blueprintName, actionIdentifiers2.blueprintName)
        Assert.assertEquals(actionIdentifiers.blueprintVersion, actionIdentifiers2.blueprintVersion)
        Assert.assertEquals(actionIdentifiers.mode, actionIdentifiers2.mode)
    }
}
