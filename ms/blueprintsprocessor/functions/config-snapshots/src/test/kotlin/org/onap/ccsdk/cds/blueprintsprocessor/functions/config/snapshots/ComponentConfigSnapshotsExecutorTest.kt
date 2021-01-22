/*
 *  Copyright © 2019 Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.ComponentConfigSnapshotsExecutor.Companion.DIFF_JSON
import org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.ComponentConfigSnapshotsExecutor.Companion.DIFF_XML
import org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.ComponentConfigSnapshotsExecutor.Companion.OPERATION_DIFF
import org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.ComponentConfigSnapshotsExecutor.Companion.OPERATION_FETCH
import org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.ComponentConfigSnapshotsExecutor.Companion.OPERATION_STORE
import org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.db.ResourceConfigSnapshot
import org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.db.ResourceConfigSnapshotService
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [ResourceConfigSnapshotService::class, TestDatabaseConfiguration::class]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@ComponentScan(
    basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots"]
)
@Suppress("SameParameterValue")
class ComponentConfigSnapshotsExecutorTest {

    @Autowired
    lateinit var cfgSnapshotService: ResourceConfigSnapshotService
    lateinit var cfgSnapshotComponent: ComponentConfigSnapshotsExecutor
    private var bluePrintRuntimeService = BlueprintMetadataUtils.bluePrintRuntime(
        "123456-1000",
        "./../../../../components/model-catalog/blueprint-model/test-blueprint/remote_scripts"
    )

    private val resourceId = "1"
    private val resourceType = "ServiceInstance"
    private val props = mutableMapOf<String, JsonNode>()
    private val nodeTemplateName = "nodeTemplateName"

    private val executionRequest = ExecutionServiceInput()

    @Before
    fun setup() {
        cfgSnapshotComponent = ComponentConfigSnapshotsExecutor(cfgSnapshotService)
        props[ComponentConfigSnapshotsExecutor.INPUT_RESOURCE_ID] = resourceId.asJsonPrimitive()
        props[ComponentConfigSnapshotsExecutor.INPUT_RESOURCE_TYPE] = resourceType.asJsonPrimitive()

        cfgSnapshotComponent.operationInputs = props
        cfgSnapshotComponent.bluePrintRuntimeService = bluePrintRuntimeService
        cfgSnapshotComponent.nodeTemplateName = nodeTemplateName

        cfgSnapshotComponent.executionServiceInput = executionRequest
        cfgSnapshotComponent.processId = "12"
        cfgSnapshotComponent.workflowName = "workflow"
        cfgSnapshotComponent.stepName = "step"
        cfgSnapshotComponent.interfaceName = "interfaceName"
        cfgSnapshotComponent.operationName = "operationName"
    }

    @Test
    fun processNBFetchWithResourceIdAndResourceTypeSingleFind() {
        val snapshot = ResourceConfigSnapshot()
        val snapshotConfig = "TEST1"
        snapshot.config_snapshot = snapshotConfig

        runBlocking {
            try {
                val resId = "121111"
                val resType = "PNF"
                cfgSnapshotService.write(snapshotConfig, resId, resType)
                prepareRequestProperties(OPERATION_FETCH, resId, resType, ResourceConfigSnapshot.Status.RUNNING.name)

                cfgSnapshotComponent.processNB(executionRequest)
            } catch (e: BlueprintProcessorException) {
                kotlin.test.assertEquals(
                    "Can't proceed with the cfg snapshot lookup: provide resource-id and resource-type.",
                    e.message
                )
                return@runBlocking
            }
            // then; we should get success and the TEST1 payload in our output properties
            assertEquals(
                ComponentConfigSnapshotsExecutor.OUTPUT_STATUS_SUCCESS.asJsonPrimitive(),
                bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    nodeTemplateName,
                    ComponentConfigSnapshotsExecutor.OUTPUT_STATUS
                )
            )
            assertEquals(
                snapshotConfig.asJsonPrimitive(),
                bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    nodeTemplateName,
                    ComponentConfigSnapshotsExecutor.OUTPUT_SNAPSHOT
                )
            )
        }
    }

    @Test
    fun processNBFetchCandidateWithResourceIdAndResourceTypeSingleFind() {
        val snapshot = ResourceConfigSnapshot()
        val snapshotConfig = "TEST"
        snapshot.config_snapshot = snapshotConfig

        runBlocking {
            try {
                val resId = "121111"
                val resType = "PNF"
                cfgSnapshotService.write(snapshotConfig, resId, resType, ResourceConfigSnapshot.Status.CANDIDATE)
                prepareRequestProperties(OPERATION_FETCH, resId, resType, ResourceConfigSnapshot.Status.CANDIDATE.name)

                cfgSnapshotComponent.processNB(executionRequest)
            } catch (e: BlueprintProcessorException) {
                kotlin.test.assertEquals(
                    "Can't proceed with the cfg snapshot lookup: provide resource-id and resource-type.",
                    e.message
                )
                return@runBlocking
            }
            // then; we should get success and the TEST payload in our output properties
            assertEquals(
                ComponentConfigSnapshotsExecutor.OUTPUT_STATUS_SUCCESS.asJsonPrimitive(),
                bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    nodeTemplateName,
                    ComponentConfigSnapshotsExecutor.OUTPUT_STATUS
                )
            )
            assertEquals(
                snapshotConfig.asJsonPrimitive(),
                bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    nodeTemplateName,
                    ComponentConfigSnapshotsExecutor.OUTPUT_SNAPSHOT
                )
            )
        }
    }

    @Test
    fun processNBStoreWithResourceIdAndResourceType() {
        val snapshot = ResourceConfigSnapshot()
        val snapshotConfig = "PAYLOAD"
        snapshot.config_snapshot = snapshotConfig

        runBlocking {
            try {
                val resId = "121111"
                val resType = "PNF"
                prepareRequestProperties(OPERATION_STORE, resId, resType, snapshotConfig)

                cfgSnapshotComponent.processNB(executionRequest)
            } catch (e: BlueprintProcessorException) {
                kotlin.test.assertEquals(
                    "Can't proceed with the cfg snapshot lookup: provide resource-id and resource-type.",
                    e.message
                )
                return@runBlocking
            }

            // then; we should get success and the PAYLOAD payload in our output properties
            assertEquals(
                ComponentConfigSnapshotsExecutor.OUTPUT_STATUS_SUCCESS.asJsonPrimitive(),
                bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    nodeTemplateName,
                    ComponentConfigSnapshotsExecutor.OUTPUT_STATUS
                )
            )
            assertEquals(
                snapshotConfig.asJsonPrimitive(),
                bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    nodeTemplateName,
                    ComponentConfigSnapshotsExecutor.OUTPUT_SNAPSHOT
                )
            )
        }
    }

    @Test
    fun processNBFetchNoneFound() {

        runBlocking {
            // when; asking for unknown resource Id/ resource Type combo; should get an error response
            try {
                prepareRequestProperties(OPERATION_FETCH, "asdasd", "PNF", ResourceConfigSnapshot.Status.RUNNING.name)

                cfgSnapshotComponent.processNB(executionRequest)
            } catch (e: BlueprintProcessorException) {
                kotlin.test.assertEquals(
                    "Can't proceed with the cfg snapshot lookup: provide resource-id and resource-type.",
                    e.message
                )
                return@runBlocking
            }

            assertEquals(
                ComponentConfigSnapshotsExecutor.OUTPUT_STATUS_SUCCESS.asJsonPrimitive(),
                bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    nodeTemplateName,
                    ComponentConfigSnapshotsExecutor.OUTPUT_STATUS
                )
            )
        }
    }

    @Test
    fun processNBErrorOperationUnknown() {

        runBlocking {
            // when; asking for unknown operation update; should get an error response
            try {
                prepareRequestProperties("update", "asdasd", "PNF", ResourceConfigSnapshot.Status.RUNNING.name)

                cfgSnapshotComponent.processNB(executionRequest)
            } catch (e: BlueprintProcessorException) {
                kotlin.test.assertEquals(
                    "Can't proceed with the cfg snapshot lookup: provide resource-id and resource-type.",
                    e.message
                )
                return@runBlocking
            }

            // then; we should get error in our output properties
            assertTrue(bluePrintRuntimeService.getBlueprintError().errors.size == 1)
            assertEquals(
                ComponentConfigSnapshotsExecutor.OUTPUT_STATUS_ERROR.asJsonPrimitive(),
                bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    nodeTemplateName,
                    ComponentConfigSnapshotsExecutor.OUTPUT_STATUS
                )
            )
            val msg = "Operation parameter must be fetch, store or diff"
            assertEquals(
                msg.asJsonPrimitive(),
                bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    nodeTemplateName,
                    ComponentConfigSnapshotsExecutor.OUTPUT_MESSAGE
                )
            )
        }
    }

    @Test
    fun processNBErrorDiffContentTypeUnknown() {

        runBlocking {
            // when; asking for unknown content type diff operation; should get an error response
            try {
                val resId = "121111"
                val resType = "PNF"
                cfgSnapshotService.write("snapshotConfig", resId, resType, ResourceConfigSnapshot.Status.CANDIDATE)
                prepareRequestProperties(OPERATION_DIFF, resId, resType, "YANG")

                cfgSnapshotComponent.processNB(executionRequest)
            } catch (e: BlueprintProcessorException) {
                kotlin.test.assertEquals(
                    "Can't proceed with the cfg snapshot lookup: provide resource-id and resource-type.",
                    e.message
                )
                return@runBlocking
            }

            // then; we should get error in our output properties
            assertEquals(
                ComponentConfigSnapshotsExecutor.OUTPUT_STATUS_ERROR.asJsonPrimitive(),
                bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    nodeTemplateName,
                    ComponentConfigSnapshotsExecutor.OUTPUT_STATUS
                )
            )
            val message = "Could not compare config snapshots for type YANG"
            assertEquals(
                message.asJsonPrimitive(),
                bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    nodeTemplateName,
                    ComponentConfigSnapshotsExecutor.OUTPUT_MESSAGE
                )
            )
        }
    }

    @Test
    fun processNBCompareTwoJsonConfigSnapshots() {

        runBlocking {

            // when; comparing RUNNING vs CANDIDATE json configs; should get an success response; with differences
            try {
                val resId = "131313"
                val resType = "PNF"
                preparePayload("config-payload-running.json", resId, resType, ResourceConfigSnapshot.Status.RUNNING)
                preparePayload("config-payload-candidate.json", resId, resType, ResourceConfigSnapshot.Status.CANDIDATE)

                prepareRequestProperties(OPERATION_DIFF, resId, resType, DIFF_JSON)
                cfgSnapshotComponent.processNB(executionRequest)
            } catch (e: BlueprintProcessorException) {
                kotlin.test.assertEquals(
                    "Can't proceed with the cfg snapshot diff: provide resource-id and resource-type.",
                    e.message
                )
                return@runBlocking
            }

            // then; we should get success
            assertTrue(bluePrintRuntimeService.getBlueprintError().errors.size == 0)
            assertEquals(
                ComponentConfigSnapshotsExecutor.OUTPUT_STATUS_SUCCESS.asJsonPrimitive(),
                bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    nodeTemplateName,
                    ComponentConfigSnapshotsExecutor.OUTPUT_STATUS
                )
            )

            // then; we should get JSON-patches differences in our response property
            val diffJson =
                "[{\"op\":\"add\",\"path\":\"/system-uptime-information/last-configured-time/new-child-object\",\"value\":{\"property\":\"value\"}}," +
                    "{\"op\":\"replace\",\"path\":\"/system-uptime-information/system-booted-time/time-length\",\"value\":\"14:52:54\"}," +
                    "{\"op\":\"replace\",\"path\":\"/system-uptime-information/time-source\",\"value\":\" DNS CLOCK \"}," +
                    "{\"op\":\"add\",\"path\":\"/system-uptime-information/uptime-information/load-average-10\",\"value\":\"0.05\"}]"
            assertEquals(
                diffJson.asJsonPrimitive(),
                bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    nodeTemplateName,
                    ComponentConfigSnapshotsExecutor.OUTPUT_SNAPSHOT
                )
            )
        }
    }

    @Test
    fun processNBCompareTwoXmlConfigSnapshots() {

        runBlocking {

            // when; comparing RUNNING vs CANDIDATE xml configs; should get an success response; with differences
            try {
                val resId = "141414"
                val resType = "VNF"
                preparePayload("interface-running.xml", resId, resType, ResourceConfigSnapshot.Status.RUNNING)
                preparePayload("interface-candidate.xml", resId, resType, ResourceConfigSnapshot.Status.CANDIDATE)

                prepareRequestProperties(OPERATION_DIFF, resId, resType, DIFF_XML)

                cfgSnapshotComponent.processNB(executionRequest)
            } catch (e: BlueprintProcessorException) {
                kotlin.test.assertEquals(
                    "Can't proceed with the cfg snapshot diff: provide resource-id and resource-type.",
                    e.message
                )
                return@runBlocking
            }

            // then; we should get success
            assertTrue(bluePrintRuntimeService.getBlueprintError().errors.size == 0)
            assertEquals(
                ComponentConfigSnapshotsExecutor.OUTPUT_STATUS_SUCCESS.asJsonPrimitive(),
                bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    nodeTemplateName,
                    ComponentConfigSnapshotsExecutor.OUTPUT_STATUS
                )
            )

            // then; we should get XML-patches differences in our response property
            val diffXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<diff>" +
                "<replace sel=\"/output[1]/interface-information[1]/interface-flapped[1]/@seconds\">2343</replace>" +
                "<replace sel=\"/output[1]/interface-information[1]/interface-flapped[1]/text()[1]\">34</replace>" +
                "<replace sel=\"/output[1]/interface-information[1]/traffic-statistics[1]/input-packets[1]/text()[1]\">09098789</replace>" +
                "<replace sel=\"/output[1]/interface-information[1]/traffic-statistics[1]/output-packets[1]/text()[1]\">2828828</replace>" +
                "<add sel=\"/output[1]/interface-information[1]/physical-interface[1]\"><interface-name>TEGig400-int01</interface-name></add>" +
                "</diff>"
            assertEquals(
                diffXml.asJsonPrimitive(),
                bluePrintRuntimeService.getNodeTemplateAttributeValue(
                    nodeTemplateName,
                    ComponentConfigSnapshotsExecutor.OUTPUT_SNAPSHOT
                )
            )
        }
    }

    private fun preparePayload(
        filename: String,
        resId: String,
        resType: String,
        status: ResourceConfigSnapshot.Status
    ) {
        runBlocking {
            cfgSnapshotService.write(
                JacksonUtils.getClassPathFileContent("payload/requests/$filename"),
                resId,
                resType,
                status
            )
        }
    }

    private fun prepareRequestProperties(oper: String, resId: String, resType: String, optional: String = "") {
        cfgSnapshotComponent.operationInputs[ComponentConfigSnapshotsExecutor.INPUT_OPERATION] = oper.asJsonPrimitive()
        cfgSnapshotComponent.operationInputs[ComponentConfigSnapshotsExecutor.INPUT_RESOURCE_ID] =
            resId.asJsonPrimitive()
        cfgSnapshotComponent.operationInputs[ComponentConfigSnapshotsExecutor.INPUT_RESOURCE_TYPE] =
            resType.asJsonPrimitive()

        // Optional inputs
        cfgSnapshotComponent.operationInputs[ComponentConfigSnapshotsExecutor.INPUT_DIFF_CONTENT_TYPE] =
            "".asJsonPrimitive()
        cfgSnapshotComponent.operationInputs[ComponentConfigSnapshotsExecutor.INPUT_RESOURCE_SNAPSHOT] =
            "".asJsonPrimitive()
        cfgSnapshotComponent.operationInputs[ComponentConfigSnapshotsExecutor.INPUT_RESOURCE_STATUS] =
            ResourceConfigSnapshot.Status.RUNNING.name.asJsonPrimitive()

        when (oper) {
            OPERATION_DIFF ->
                cfgSnapshotComponent.operationInputs[ComponentConfigSnapshotsExecutor.INPUT_DIFF_CONTENT_TYPE] =
                    optional.asJsonPrimitive()
            OPERATION_STORE ->
                cfgSnapshotComponent.operationInputs[ComponentConfigSnapshotsExecutor.INPUT_RESOURCE_SNAPSHOT] =
                    optional.asJsonPrimitive()
            OPERATION_FETCH ->
                cfgSnapshotComponent.operationInputs[ComponentConfigSnapshotsExecutor.INPUT_RESOURCE_STATUS] =
                    optional.asJsonPrimitive()
        }
    }
}
