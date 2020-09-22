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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.ansible.executor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import kotlinx.coroutines.delay
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.isNullOrMissing
import org.onap.ccsdk.cds.controllerblueprints.core.returnNullIfMissing
import org.onap.ccsdk.cds.controllerblueprints.core.rootFieldsToMap
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import java.net.URI
import java.net.URLEncoder
import java.util.NoSuchElementException

/**
 * ComponentRemoteAnsibleExecutor
 *
 * Component that launches a run of a job template (INPUT_JOB_TEMPLATE_NAME) representing an Ansible playbook,
 * and its parameters, via the AWX server identified by the INPUT_ENDPOINT_SELECTOR parameter.
 *
 * It supports extra_vars, limit, tags, skip-tags, inventory (by name or Id) Ansible parameters.
 * It reports the results of the execution via properties, named execute-command-status and execute-command-logs
 *
 * @author Serge Simard
 */
@Component("component-remote-ansible-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentRemoteAnsibleExecutor(
    private val blueprintRestLibPropertyService: BluePrintRestLibPropertyService,
    private val mapper: ObjectMapper
) :
    AbstractComponentFunction() {

    // HTTP related constants
    private val HTTP_SUCCESS = 200..202
    private val GET = HttpMethod.GET.name
    private val POST = HttpMethod.POST.name
    private val plainTextHeaders = mapOf("Accept" to "text/plain")

    var checkDelay: Long = 15_000

    companion object {

        private val log = LoggerFactory.getLogger(ComponentRemoteAnsibleExecutor::class.java)

        // input fields names accepted by this executor
        const val INPUT_ENDPOINT_SELECTOR = "endpoint-selector"
        const val INPUT_JOB_TEMPLATE_NAME = "job-template-name"
        const val INPUT_WORKFLOW_JOB_TEMPLATE_NAME = "workflow-job-template-id"
        const val INPUT_LIMIT_TO_HOST = "limit"
        const val INPUT_INVENTORY = "inventory"
        const val INPUT_EXTRA_VARS = "extra-vars"
        const val INPUT_TAGS = "tags"
        const val INPUT_SKIP_TAGS = "skip-tags"

        // output fields names (and values) populated by this executor; aligned with job details status field values.
        const val ATTRIBUTE_EXEC_CMD_ARTIFACTS = "ansible-artifacts"
        const val ATTRIBUTE_EXEC_CMD_STATUS = "ansible-command-status"
        const val ATTRIBUTE_EXEC_CMD_LOG = "ansible-command-logs"
        const val ATTRIBUTE_EXEC_CMD_STATUS_ERROR = "error"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        try {
            val restClientService = getAWXRestClient()

            // Get either a job template name or a workflow template name property
            var workflowURIPrefix = ""
            var jobTemplateName = getOperationInput(INPUT_JOB_TEMPLATE_NAME).returnNullIfMissing()?.textValue() ?: ""
            val isWorkflowJT = jobTemplateName.isBlank()
            if (isWorkflowJT) {
                jobTemplateName = getOperationInput(INPUT_WORKFLOW_JOB_TEMPLATE_NAME).asText()
                workflowURIPrefix = "workflow_"
            }

            val jtId = lookupJobTemplateIDByName(restClientService, jobTemplateName, workflowURIPrefix)
            if (jtId.isNotEmpty()) {
                runJobTemplateOnAWX(restClientService, jobTemplateName, jtId, workflowURIPrefix)
            } else {
                val message = "Workflow/Job template $jobTemplateName does not exists"
                log.error(message)
                setNodeOutputErrors(ATTRIBUTE_EXEC_CMD_STATUS_ERROR, message)
            }
        } catch (e: Exception) {
            log.error("Failed to process on remote executor (${e.message})", e)
            setNodeOutputErrors(ATTRIBUTE_EXEC_CMD_STATUS_ERROR, "Failed to process on remote executor (${e.message})")
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        val message = "Error in ComponentRemoteAnsibleExecutor : ${runtimeException.message}"
        log.error(message, runtimeException)
        setNodeOutputErrors(ATTRIBUTE_EXEC_CMD_STATUS_ERROR, message)
    }

    /** Creates a TokenAuthRestClientService, since this executor expect type property to be "token-auth" and the
     * token to be an OAuth token (access_token response field) generated via the AWX /api/o/token rest endpoint
     * The token field is of the form "Bearer access_token_from_response", for example :
     *  "blueprintsprocessor.restclient.awx.type=token-auth"
     *  "blueprintsprocessor.restclient.awx.url=http://awx-endpoint"
     *  "blueprintsprocessor.restclient.awx.token=Bearer J9gEtMDzxcqw25574fioY9VAhLDIs1"
     *
     * Also supports json endpoint definition via DSL entry, e.g.:
     *     "ansible-remote-endpoint": {
     *        "type": "token-auth",
     *        "url": "http://awx-endpoint",
     *        "token": "Bearer J9gEtMDzxcqw25574fioY9VAhLDIs1"
     *     }
     */
    private fun getAWXRestClient(): BlueprintWebClientService {

        val endpointSelector = getOperationInput(INPUT_ENDPOINT_SELECTOR)

        try {
            return blueprintRestLibPropertyService.blueprintWebClientService(endpointSelector)
        } catch (e: NoSuchElementException) {
            throw IllegalArgumentException("No value provided for input selector $endpointSelector", e)
        }
    }

    /**
     * Finds the job template ID based on the job template name provided in the request
     */
    private fun lookupJobTemplateIDByName(
        awxClient: BlueprintWebClientService,
        job_template_name: String?,
        workflowPrefix: String
    ): String {
        val encodedJTName = URI(
            null, null,
            "/api/v2/${workflowPrefix}job_templates/$job_template_name/",
            null, null
        ).rawPath

        // Get Job Template details by name
        var response = awxClient.exchangeResource(GET, encodedJTName, "")
        val jtDetails: JsonNode = mapper.readTree(response.body)
        return jtDetails.at("/id").asText()
    }

    /**
     * Performs the job template execution on AWX, ie. prepare arguments as per job template
     * requirements (ask fields) and provided overriding values. Then it launches the run, and monitors
     * its execution. Finally, it retrieves the job results via the stdout api.
     * The status and output attributes are populated in the process.
     */
    private suspend fun runJobTemplateOnAWX(
        awxClient: BlueprintWebClientService,
        job_template_name: String?,
        jtId: String,
        workflowPrefix: String
    ) {
        setNodeOutputProperties("preparing".asJsonPrimitive(), "".asJsonPrimitive(), "".asJsonPrimitive())

        // Get Job Template requirements
        var response = awxClient.exchangeResource(GET, "/api/v2/${workflowPrefix}job_templates/$jtId/launch/", "")
        // FIXME: handle non-successful SC
        val jtLaunchReqs: JsonNode = mapper.readTree(response.body)
        val payload = prepareLaunchPayload(awxClient, jtLaunchReqs, workflowPrefix.isNotBlank())

        log.info("Running job with $payload, for requestId $processId.")

        // Launch the job for the targeted template
        var jtLaunched: JsonNode = JacksonUtils.objectMapper.createObjectNode()
        response = awxClient.exchangeResource(POST, "/api/v2/${workflowPrefix}job_templates/$jtId/launch/", payload)
        if (response.status in HTTP_SUCCESS) {
            jtLaunched = mapper.readTree(response.body)
            val fieldsIgnored: JsonNode = jtLaunched.at("/ignored_fields")
            if (fieldsIgnored.rootFieldsToMap().isNotEmpty()) {
                log.warn("Ignored fields : $fieldsIgnored, for requestId $processId.")
            }
        }

        if (response.status in HTTP_SUCCESS) {
            val jobId: String = jtLaunched.at("/id").asText()

            // Poll current job status while job is not executed
            var jobStatus = "unknown"
            var jobEndTime = "null"
            while (jobEndTime == "null") {
                response = awxClient.exchangeResource(GET, "/api/v2/${workflowPrefix}jobs/$jobId/", "")
                val jobLaunched: JsonNode = mapper.readTree(response.body)
                jobStatus = jobLaunched.at("/status").asText()
                jobEndTime = jobLaunched.at("/finished").asText()
                delay(checkDelay)
            }

            log.info("Execution of job template $job_template_name in job #$jobId finished with status ($jobStatus) for requestId $processId")

            populateJobRunResponse(awxClient, jobId, workflowPrefix, jobStatus)
        } else {
            // The job template requirements were not fulfilled with the values passed in. The message below will
            // provide more information via the response, like the ignored_fields, or variables_needed_to_start,
            // or resources_needed_to_start, in order to help user pinpoint the problems with the request.
            val message = "Execution of job template $job_template_name could not be started for requestId $processId." +
                " (Response: ${response.body}) "
            log.error(message)
            setNodeOutputErrors(ATTRIBUTE_EXEC_CMD_STATUS_ERROR, message)
        }
    }

    /**
     * Extracts the output from either a job stdout call OR collects the workflow run output, as well as the artifacts
     * and populate the component corresponding output properties
     */
    private fun populateJobRunResponse(
        awxClient: BlueprintWebClientService,
        jobId: String,
        workflowPrefix: String,
        jobStatus: String
    ) {

        val collectedResponses = StringBuilder(4096)
        val artifacts: MutableMap<String, JsonNode> = mutableMapOf()

        collectJobIdsRelatedToJobRun(awxClient, jobId, workflowPrefix).forEach { aJobId ->

            // Collect the response text from the corresponding jobIds
            var response = awxClient.exchangeResource(GET, "/api/v2/jobs/$aJobId/stdout/?format=txt", "", plainTextHeaders)
            if (response.status in HTTP_SUCCESS) {
                val jobOutput = response.body
                collectedResponses
                    .append("Output for Job $aJobId :" + System.lineSeparator())
                    .append(jobOutput)
                    .append(System.lineSeparator())
                log.info("Response for job $aJobId: \n $jobOutput \n")
            } else {
                log.warn("Could not gather response for job $aJobId. Status=${response.status}")
            }

            // Collect artifacts variables from each job and gather them up in one json node
            response = awxClient.exchangeResource(GET, "/api/v2/jobs/$aJobId/", "")
            if (response.status in HTTP_SUCCESS) {
                val jobArtifacts = mapper.readTree(response.body).at("/artifacts")
                if (jobArtifacts != null) {
                    artifacts.putAll(jobArtifacts.rootFieldsToMap())
                }
            }
        }

        log.info("Artifacts for job $jobId: \n $artifacts \n")

        setNodeOutputProperties(jobStatus.asJsonPrimitive(), collectedResponses.toString().asJsonPrimitive(), artifacts.asJsonNode())
    }

    /**
     * List all the job Ids for a give workflow, i.e. sub jobs, or the jobId if not a workflow instance
     */
    private fun collectJobIdsRelatedToJobRun(awxClient: BlueprintWebClientService, jobId: String, workflowPrefix: String): Array<String> {

        var jobIds: Array<String>

        if (workflowPrefix.isNotEmpty()) {
            var response = awxClient.exchangeResource(GET, "/api/v2/${workflowPrefix}jobs/$jobId/workflow_nodes/", "")
            val jobDetails = mapper.readTree(response.body).at("/results")

            // gather up job Id of all actual job nodes that ran during the workflow
            jobIds = emptyArray()
            for (jobDetail in jobDetails.elements()) {
                if (jobDetail.at("/do_not_run").asText() == "false") {
                    jobIds = jobIds.plus(jobDetail.at("/summary_fields/job/id").asText())
                }
            }
        } else {
            jobIds = arrayOf(jobId)
        }
        return jobIds
    }

    /**
     * Prepares the JSON payload expected by the job template api,
     * by applying the overrides that were provided
     * and allowed by the template definition flags in jtLaunchReqs
     */
    private fun prepareLaunchPayload(
        awxClient: BlueprintWebClientService,
        jtLaunchReqs: JsonNode,
        isWorkflow: Boolean
    ): String {
        val payload = JacksonUtils.objectMapper.createObjectNode()

        // Parameter defaults
        val inventoryProp = getOptionalOperationInput(INPUT_INVENTORY)
        val extraArgs = getOperationInput(INPUT_EXTRA_VARS)

        if (!isWorkflow) {
            val limitProp = getOptionalOperationInput(INPUT_LIMIT_TO_HOST)
            val tagsProp = getOptionalOperationInput(INPUT_TAGS)
            val skipTagsProp = getOptionalOperationInput(INPUT_SKIP_TAGS)

            val askLimitOnLaunch = jtLaunchReqs.at("/ask_limit_on_launch").asBoolean()
            if (askLimitOnLaunch && !limitProp.isNullOrMissing()) {
                payload.set<JsonNode>(INPUT_LIMIT_TO_HOST, limitProp)
            }
            val askTagsOnLaunch = jtLaunchReqs.at("/ask_tags_on_launch").asBoolean()
            if (askTagsOnLaunch && !tagsProp.isNullOrMissing()) {
                payload.set<JsonNode>(INPUT_TAGS, tagsProp)
            }
            if (askTagsOnLaunch && !skipTagsProp.isNullOrMissing()) {
                payload.set<JsonNode>("skip_tags", skipTagsProp)
            }
        }

        val askInventoryOnLaunch = jtLaunchReqs.at("/ask_inventory_on_launch").asBoolean()
        if (askInventoryOnLaunch && !inventoryProp.isNullOrMissing()) {
            var inventoryKeyId = if (inventoryProp is TextNode) {
                resolveInventoryIdByName(awxClient, inventoryProp.textValue())?.asJsonPrimitive()
            } else {
                inventoryProp
            }
            payload.set<JsonNode>(INPUT_INVENTORY, inventoryKeyId)
        }

        payload.set<JsonNode>("extra_vars", extraArgs)

        return payload.asJsonString(false)
    }

    private fun resolveInventoryIdByName(awxClient: BlueprintWebClientService, inventoryProp: String): Int? {
        var invId: Int? = null

        // Get Inventory by name
        val encoded = URLEncoder.encode(inventoryProp)
        val response = awxClient.exchangeResource(GET, "/api/v2/inventories/?name=$encoded", "")
        if (response.status in HTTP_SUCCESS) {
            // Extract the inventory ID from response
            val invDetails = mapper.readTree(response.body)
            val nbInvFound = invDetails.at("/count").asInt()
            if (nbInvFound == 1) {
                invId = invDetails["results"][0]["id"].asInt()
                log.info("Resolved inventory $inventoryProp to ID #: $invId")
            }
        }

        if (invId == null) {
            val message = "Could not resolve inventory $inventoryProp by name..."
            log.error(message)
            throw IllegalArgumentException(message)
        }

        return invId
    }

    /**
     * Utility function to set the output properties of the executor node
     */
    private fun setNodeOutputProperties(status: JsonNode, message: JsonNode, artifacts: JsonNode) {
        setAttribute(ATTRIBUTE_EXEC_CMD_STATUS, status)
        log.info("Executor status   : $status")
        setAttribute(ATTRIBUTE_EXEC_CMD_ARTIFACTS, artifacts)
        log.info("Executor artifacts: $artifacts")
        setAttribute(ATTRIBUTE_EXEC_CMD_LOG, message)
        log.info("Executor message  : $message")
    }

    /**
     * Utility function to set the output properties and errors of the executor node, in cas of errors
     */
    private fun setNodeOutputErrors(status: String, message: String, artifacts: JsonNode = "".asJsonPrimitive()) {
        setAttribute(ATTRIBUTE_EXEC_CMD_STATUS, status.asJsonPrimitive())
        setAttribute(ATTRIBUTE_EXEC_CMD_LOG, message.asJsonPrimitive())
        setAttribute(ATTRIBUTE_EXEC_CMD_ARTIFACTS, artifacts)

        addError(status, ATTRIBUTE_EXEC_CMD_LOG, message)
    }
}
