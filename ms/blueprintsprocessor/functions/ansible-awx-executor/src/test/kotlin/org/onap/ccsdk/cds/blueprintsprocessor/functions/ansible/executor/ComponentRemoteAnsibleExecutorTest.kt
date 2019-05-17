/*
 *  Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.functions.ansible.executor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StepData
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BluePrintRestLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.putJsonElement
import org.onap.ccsdk.cds.controllerblueprints.core.service.DefaultBluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = [BluePrintRestLibConfiguration::class,
    BlueprintPropertyConfiguration::class,
    BluePrintProperties::class,
    BluePrintProperties::class])
@TestPropertySource(properties =
[
    "server.port=8443",
    "server.ssl.enabled=true",
    "server.ssl.key-store=classpath:keystore.p12",
    "server.ssl.key-store-password=changeit",
    "server.ssl.keyStoreType=PKCS12",
    "server.ssl.keyAlias=tomcat",
    "blueprintsprocessor.restclient.awx.type=basic-auth",
    "blueprintsprocessor.restclient.awx.url=http://142.44.184.236",
    "blueprintsprocessor.restclient.awx.username=admin",
    "blueprintsprocessor.restclient.awx.password=password",
    "blueprintsprocessor.restclient.test.type=ssl-basic-auth",
    "blueprintsprocessor.restclient.test.url=https://localhost:8443",
    "blueprintsprocessor.restclient.test.username=admin",
    "blueprintsprocessor.restclient.test.password=jans",
    "blueprintsprocessor.restclient.test.keyStoreInstance=PKCS12",
    "blueprintsprocessor.restclient.test.sslTrust=src/test/resources/keystore.p12",
    "blueprintsprocessor.restclient.test.sslTrustPassword=changeit"
])
class ComponentRemoteAnsibleExecutorTest {

    @Autowired
    lateinit var bluePrintRestLibPropertyService: BluePrintRestLibPropertyService

    @Transient
    private val log = LoggerFactory.getLogger(ComponentRemoteAnsibleExecutorTest::class.java)

    @Test
    @Ignore
    fun testComponentRemoteAnsibleExecutor() {
        runBlocking {

            val awxRemoteExecutor = ComponentRemoteAnsibleExecutor(bluePrintRestLibPropertyService)

            val executionServiceInput = JacksonUtils.readValueFromClassPathFile(
                    "payload/requests/sample-remote-ansible-request.json",
                    ExecutionServiceInput::class.java)!!

            log.info("Request Inputs : " + executionServiceInput.payload)

            val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime("123456-1000",
                    "./../../../../components/model-catalog/blueprint-model/test-blueprint/remote_ansible")
            awxRemoteExecutor.bluePrintRuntimeService = bluePrintRuntimeService

            val stepMetaData: MutableMap<String, JsonNode> = hashMapOf()
            stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE, "execute-remote-ansible")
            stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_INTERFACE, "ComponentRemoteAnsibleExecutor")
            stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_OPERATION, "process")

            val stepInputData = StepData().apply {
                name = "execute-remote-ansible"
                properties = stepMetaData
            }
            executionServiceInput.stepData = stepInputData

            awxRemoteExecutor.applyNB(executionServiceInput)
        }
    }

    /**
     * Test cases for Ansible executor to work with the process NB of remote
     * executor.
     */
    @Test
    @Ignore
    fun testComponentRemoteAnsibleExecutorProcessNB() {
        runBlocking {
            //            val remoteScriptExecutionService = MockRemoteScriptExecutionService(bluePrintRestLibPropertyService)
            val componentRemoteAnsibleExecutor = ComponentRemoteAnsibleExecutor(bluePrintRestLibPropertyService)
            val bluePrintRuntime = mockk<DefaultBluePrintRuntimeService>("123456-1000")
            val input = getMockedOutput(bluePrintRuntime)
            componentRemoteAnsibleExecutor.bluePrintRuntimeService = bluePrintRuntime
            componentRemoteAnsibleExecutor.applyNB(input)
        }
    }

    /**
     * Mocked input information for remote Ansible executor.
     */
    fun getMockedOutput(svc: DefaultBluePrintRuntimeService):
            ExecutionServiceInput {
        val stepMetaData: MutableMap<String, JsonNode> = hashMapOf()

        stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE, "execute-remote-ansible")
        stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_INTERFACE, "ComponentRemoteAnsibleExecutor")
        stepMetaData.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_OPERATION, "process")

        val mapper = ObjectMapper()
        val rootNode = mapper.createObjectNode()
        rootNode.put("ip-address", "0.0.0.0")
        rootNode.put("type", "rest")

        val operationalInputs: MutableMap<String, JsonNode> = hashMapOf()
        operationalInputs.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE, "execute-remote-ansible")
        operationalInputs.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_INTERFACE, "ComponentRemoteAnsibleExecutor")
        operationalInputs.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_OPERATION, "process")
        operationalInputs.putJsonElement("endpoint-selector", "aai")
//        operationalInputs.putJsonElement("dynamic-properties", rootNode)
//        operationalInputs.putJsonElement("command", "./run.sh")
        operationalInputs.putJsonElement("job-template-name", "CDS_job_template2")

        every {
            svc.resolveNodeTemplateInterfaceOperationInputs(
                    "execute-remote-ansible",
                    "ComponentRemoteAnsibleExecutor", "process")
        } returns operationalInputs

        val stepInputData = StepData().apply {
            name = "execute-remote-ansible"
            properties = stepMetaData
        }

        val executionServiceInput = JacksonUtils
                .readValueFromClassPathFile(
                        "payload/requests/sample-remote-ansible-request.json",
                        ExecutionServiceInput::class.java)!!
        executionServiceInput.stepData = stepInputData

        val operationOutputs = hashMapOf<String, JsonNode>()
        every {
            svc.resolveNodeTemplateInterfaceOperationOutputs(
                    "execute-remote-ansible",
                    "ComponentRemoteAnsibleExecutor", "process")
        } returns operationOutputs
        val bluePrintRuntimeService = BluePrintMetadataUtils
                .getBluePrintRuntime("123456-1000",
                        "./../../../../components/model-" +
                                "catalog/blueprint-model/test-blueprint/" +
                                "remote_ansible")
//        every {
//            svc.resolveNodeTemplateArtifactDefinition("execute-remote-ansible", "component-script")
//        } returns bluePrintRuntimeService.resolveNodeTemplateArtifactDefinition("execute-remote-ansible",
//                                                                                "component-script")
        every {
            svc.setNodeTemplateAttributeValue(
                    "execute-remote-ansible",
                    "execute-command-status",
                    "successful".asJsonPrimitive())
        } returns Unit

        every {
            svc.setNodeTemplateAttributeValue(
                    "execute-remote-ansible",
                    "execute-command-logs", "N/A".asJsonPrimitive())
        } returns Unit

        every {
            svc.setNodeTemplateAttributeValue(
                    "execute-remote-ansible",
                    "execute-command-logs",
                    "processed successfully".asJsonPrimitive())
        } returns Unit

        every {
            svc.bluePrintContext()
        } returns bluePrintRuntimeService.bluePrintContext()
        return executionServiceInput
    }
}

//class MockRemoteScriptExecutionService : RemoteScriptExecutionService {
//    override suspend fun init(selector: String) {
//    }
//
//    override suspend fun prepareEnv(prepareEnvInput: PrepareRemoteEnvInput): RemoteScriptExecutionOutput {
//        assertEquals(prepareEnvInput.requestId, "123456-1000", "failed to match request id")
//        assertNotNull(prepareEnvInput.packages, "failed to get packages")
//
//        val remoteScriptExecutionOutput = mockk<RemoteScriptExecutionOutput>()
//        every { remoteScriptExecutionOutput.response } returns "prepared successfully"
//        every { remoteScriptExecutionOutput.status } returns StatusType.SUCCESS
//        return remoteScriptExecutionOutput
//    }
//
//    override suspend fun executeCommand(remoteExecutionInput: RemoteScriptExecutionInput): RemoteScriptExecutionOutput {
//        assertEquals(remoteExecutionInput.requestId, "123456-1000", "failed to match request id")
//
//        val remoteScriptExecutionOutput = mockk<RemoteScriptExecutionOutput>()
//        every { remoteScriptExecutionOutput.response } returns "processed successfully"
//        every { remoteScriptExecutionOutput.status } returns StatusType.SUCCESS
//        return remoteScriptExecutionOutput
//    }
//
//    override suspend fun close() {
//
//    }
//}