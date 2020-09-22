/*
 *  Copyright © 2019 Bell Canada.
 *  Modifications Copyright © 2018-2019 IBM.
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

import com.github.fge.jsonpatch.diff.JsonDiff
import org.apache.logging.log4j.util.Strings
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.db.ResourceConfigSnapshot
import org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.db.ResourceConfigSnapshot.Status.CANDIDATE
import org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.db.ResourceConfigSnapshot.Status.RUNNING
import org.onap.ccsdk.cds.blueprintsprocessor.functions.config.snapshots.db.ResourceConfigSnapshotService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.returnNullIfMissing
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.w3c.dom.Node
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.builder.Input
import org.xmlunit.diff.ComparisonType
import org.xmlunit.diff.Diff

/**
 * ComponentConfigSnapshotsExecutor
 *
 * Component that retrieves the saved configuration snapshot as identified by the input parameters,
 * named resource-id and resource-type.
 *
 * It reports the content of the requested snapshot via properties, config-snapshot-status
 * and config-snapshot-value.  In case of error, details can be found in the config-snapshot-status
 * and config-snapshot-message properties
 *
 * @author Serge Simard
 */
@Component("component-config-snapshots-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentConfigSnapshotsExecutor(private val cfgSnapshotService: ResourceConfigSnapshotService) :
    AbstractComponentFunction() {

    companion object {
        private val log = LoggerFactory.getLogger(ComponentConfigSnapshotsExecutor::class.java)

        // input fields names accepted by this executor
        const val INPUT_OPERATION = "operation"
        const val INPUT_RESOURCE_ID = "resource-id"
        const val INPUT_RESOURCE_TYPE = "resource-type"
        const val INPUT_RESOURCE_STATUS = "resource-status"
        const val INPUT_RESOURCE_SNAPSHOT = "resource-snapshot"
        const val INPUT_DIFF_CONTENT_TYPE = "diff-content-type"

        const val OPERATION_FETCH = "fetch"
        const val OPERATION_STORE = "store"
        const val OPERATION_DIFF = "diff"

        const val DIFF_JSON = "JSON"
        const val DIFF_XML = "XML"

        // output fields names (and values) populated by this executor.
        const val OUTPUT_STATUS = "config-snapshot-status"
        const val OUTPUT_MESSAGE = "config-snapshot-message"
        const val OUTPUT_SNAPSHOT = "config-snapshot-value"

        const val OUTPUT_STATUS_SUCCESS = "success"
        const val OUTPUT_STATUS_ERROR = "error"
    }

    /**
     * Main entry point for ComponentConfigSnapshotsExecutor
     * Supports 3 operations : fetch, store or diff
     */
    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        val operation = getOptionalOperationInput(INPUT_OPERATION)?.returnNullIfMissing()?.textValue() ?: ""
        val contentType = getOptionalOperationInput(INPUT_DIFF_CONTENT_TYPE)?.returnNullIfMissing()?.textValue() ?: ""
        val resourceId = getOptionalOperationInput(INPUT_RESOURCE_ID)?.returnNullIfMissing()?.textValue() ?: ""
        val resourceType = getOptionalOperationInput(INPUT_RESOURCE_TYPE)?.returnNullIfMissing()?.textValue() ?: ""
        val resourceStatus = getOptionalOperationInput(INPUT_RESOURCE_STATUS)?.returnNullIfMissing()?.textValue() ?: RUNNING.name
        val snapshot = getOptionalOperationInput(INPUT_RESOURCE_SNAPSHOT)?.returnNullIfMissing()?.textValue() ?: ""
        val status = ResourceConfigSnapshot.Status.valueOf(resourceStatus)

        when (operation) {
            OPERATION_FETCH -> fetchConfigurationSnapshot(resourceId, resourceType, status)
            OPERATION_STORE -> storeConfigurationSnapshot(snapshot, resourceId, resourceType, status)
            OPERATION_DIFF -> compareConfigurationSnapshot(resourceId, resourceType, contentType)

            else -> setNodeOutputErrors(
                OUTPUT_STATUS_ERROR,
                "Operation parameter must be fetch, store or diff"
            )
        }
    }

    /**
     * General error handling for the executor.
     */
    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        setNodeOutputErrors(OUTPUT_STATUS_ERROR, "Error : ${runtimeException.message}")
    }

    /**
     * Fetch a configuration snapshot, for resource identified by ID/type, of type status (RUNNING by default)
     */
    private suspend fun fetchConfigurationSnapshot(
        resourceId: String,
        resourceType: String,
        status: ResourceConfigSnapshot.Status = RUNNING
    ) {
        try {
            val cfgSnapshotValue = cfgSnapshotService.findByResourceIdAndResourceTypeAndStatus(resourceId, resourceType, status)
            setNodeOutputProperties(OUTPUT_STATUS_SUCCESS, cfgSnapshotValue)
        } catch (er: NoSuchElementException) {
            val message = "No Resource config snapshot identified by resourceId={$resourceId}, " +
                "resourceType={$resourceType} does not exists"
            setNodeOutputErrors(OUTPUT_STATUS_ERROR, message)
        }
    }

    /**
     * Store a configuration snapshot, for resource identified by ID/type, of type status (RUNNING by default)
     */
    private suspend fun storeConfigurationSnapshot(
        cfgSnapshotValue: String,
        resourceId: String,
        resourceType: String,
        status: ResourceConfigSnapshot.Status = RUNNING
    ) {
        if (cfgSnapshotValue.isNotEmpty()) {
            val cfgSnapshotSaved = cfgSnapshotService.write(cfgSnapshotValue, resourceId, resourceType, status)
            setNodeOutputProperties(OUTPUT_STATUS_SUCCESS, cfgSnapshotSaved.config_snapshot ?: "")
        } else {
            val message = "Could not store config snapshot identified by resourceId={$resourceId},resourceType={$resourceType} does not exists"
            setNodeOutputErrors(OUTPUT_STATUS_ERROR, message)
        }
    }

    /**
     * Compare two configs (RUNNING vs CANDIDATE) for resource identified by ID/type, using the specified contentType
     */
    private suspend fun compareConfigurationSnapshot(resourceId: String, resourceType: String, contentType: String) {

        val cfgRunning = cfgSnapshotService.findByResourceIdAndResourceTypeAndStatus(resourceId, resourceType, RUNNING)
        val cfgCandidate = cfgSnapshotService.findByResourceIdAndResourceTypeAndStatus(resourceId, resourceType, CANDIDATE)

        if (cfgRunning.isEmpty() || cfgCandidate.isEmpty()) {
            setNodeOutputProperties(OUTPUT_STATUS_SUCCESS, Strings.EMPTY)
            return
        }

        when (contentType.toUpperCase()) {
            DIFF_JSON -> {
                val patchNode = JsonDiff.asJson(cfgRunning.jsonAsJsonType(), cfgCandidate.jsonAsJsonType())
                setNodeOutputProperties(OUTPUT_STATUS_SUCCESS, patchNode.toString())
            }
            DIFF_XML -> {
                val myDiff = DiffBuilder
                    .compare(Input.fromString(cfgRunning))
                    .withTest(Input.fromString(cfgCandidate))
                    .checkForSimilar()
                    .ignoreComments()
                    .ignoreWhitespace()
                    .normalizeWhitespace()
                    .build()

                setNodeOutputProperties(OUTPUT_STATUS_SUCCESS, formatXmlDifferences(myDiff))
            }
            else -> {
                val message = "Could not compare config snapshots for type $contentType"
                setNodeOutputErrors(OUTPUT_STATUS_ERROR, message)
            }
        }
    }

    /**
     * Utility function to set the output properties of the executor node
     */
    private fun setNodeOutputProperties(status: String, snapshot: String) {
        setAttribute(OUTPUT_STATUS, status.asJsonPrimitive())
        setAttribute(OUTPUT_SNAPSHOT, snapshot.asJsonPrimitive())
        log.debug("Setting output $OUTPUT_STATUS=$status")
    }

    /**
     * Utility function to set the output properties and errors of the executor node, in case of errors
     */
    private fun setNodeOutputErrors(status: String, message: String) {
        setAttribute(OUTPUT_STATUS, status.asJsonPrimitive())
        setAttribute(OUTPUT_MESSAGE, message.asJsonPrimitive())
        setAttribute(OUTPUT_SNAPSHOT, "".asJsonPrimitive())

        log.info("Setting error and output $OUTPUT_STATUS=$status, $OUTPUT_MESSAGE=$message ")

        addError(status, OUTPUT_MESSAGE, message)
    }

    /**
     * Formats XmlUnit differences into xml-patch like response (RFC5261)
     */
    private fun formatXmlDifferences(differences: Diff): String {
        val output = StringBuilder()
        output.append(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<diff>"
        )
        val diffIterator = differences.getDifferences().iterator()
        while (diffIterator.hasNext()) {

            val aDiff = diffIterator.next().comparison
            when (aDiff.type) {
                ComparisonType.ATTR_VALUE -> {
                    output.append("<replace sel=\"").append(aDiff.testDetails.xPath).append("\">")
                        .append(aDiff.testDetails.value)
                        .append("</replace>")
                }
                ComparisonType.TEXT_VALUE -> {
                    output.append("<replace sel=\"").append(aDiff.testDetails.xPath).append("\">")
                        .append(aDiff.testDetails.value)
                        .append("</replace>")
                }
                ComparisonType.CHILD_LOOKUP -> {
                    output.append("<add sel=\"").append(aDiff.testDetails.parentXPath).append("\">")
                        .append(formatNode(aDiff.testDetails.target))
                        .append("</add>")
                }
                ComparisonType.CHILD_NODELIST_LENGTH -> {
                    // Ignored; will be processed in the CHILD_LOOKUP case
                }
                else -> {
                    log.warn("Unsupported XML difference found: $aDiff")
                }
            }
        }
        output.append("</diff>")

        return output.toString()
    }

    /**
     * Formats a node value obtained from an XmlUnit differences node
     */
    private fun formatNode(node: Node): String {
        val output = StringBuilder()

        val parentName = node.localName
        output.append("<$parentName>")
        if (node.hasChildNodes()) {
            val nodes = node.childNodes
            for (index in 1..nodes.length) {
                val child = nodes.item(index - 1)
                if (child.nodeType == Node.TEXT_NODE || child.nodeType == Node.COMMENT_NODE) {
                    output.append(child.nodeValue)
                } else {
                    output.append(formatNode(child))
                }
            }
        }
        output.append("</$parentName>")

        return output.toString()
    }
}
