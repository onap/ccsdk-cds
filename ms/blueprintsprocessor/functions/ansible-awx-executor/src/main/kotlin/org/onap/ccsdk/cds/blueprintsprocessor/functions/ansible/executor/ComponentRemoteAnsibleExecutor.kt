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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.ansible.executor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import java.net.URLEncoder
import java.util.NoSuchElementException
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.*
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

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
open class ComponentRemoteAnsibleExecutor(private val blueprintRestLibPropertyService: BluePrintRestLibPropertyService)
    : AbstractComponentFunction() {

    private val log = LoggerFactory.getLogger(ComponentRemoteAnsibleExecutor::class.java)!!

    // HTTP related constants
    private val HTTP_SUCCESS = 200..202
    private val GET = HttpMethod.GET.name
    private val POST = HttpMethod.POST.name

    companion object {
        // input fields names accepted by this executor
        const val INPUT_ENDPOINT_SELECTOR = "endpoint-selector"
        const val INPUT_JOB_TEMPLATE_NAME = "job-template-name"
        const val INPUT_LIMIT_TO_HOST = "limit"
        const val INPUT_INVENTORY = "inventory"
        const val INPUT_EXTRA_VARS = "extra-vars"
        const val INPUT_TAGS = "tags"
        const val INPUT_SKIP_TAGS = "skip-tags"

        // output fields names (and values) populated by this executor; aligned with job details status field values.
        const val ATTRIBUTE_EXEC_CMD_STATUS = "ansible-command-status"
        const val ATTRIBUTE_EXEC_CMD_LOG = "ansible-command-logs"
        const val ATTRIBUTE_EXEC_CMD_STATUS_ERROR = "error"

        const val CHECKDELAY: Long = 10000
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        try {
            val restClientService = getAWXRestClient()

            val jobTemplateName = getOperationInput(INPUT_JOB_TEMPLATE_NAME).asText()
            val jtId = lookupJobTemplateIDByName(restClientService, jobTemplateName)
            if (jtId.isNotEmpty()) {
                runJobTemplateOnAWX(restClientService, jobTemplateName, jtId)
            } else {
                val message = "Job template ${jobTemplateName} does not exists"
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
        log.error(message,runtimeException)
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
        } catch (e : NoSuchElementException) {
            throw IllegalArgumentException("No value provided for input selector $endpointSelector", e)
        }
    }

    /**
     * Finds the job template ID based on the job template name provided in the request
     */
    private fun lookupJobTemplateIDByName(awxClient : BlueprintWebClientService, job_template_name: String?): String {
        val mapper = ObjectMapper()

        // Get Job Template details by name
        var response = awxClient.exchangeResource(GET, "/api/v2/job_templates/${job_template_name}/", "")
        val jtDetails: JsonNode = mapper.readTree(response.body)
        return jtDetails.at("/id").asText()
    }

    /**
     * Performs the job template execution on AWX, ie. prepare arguments as per job template
     * requirements (ask fields) and provided overriding values. Then it launches the run, and monitors
     * its execution. Finally, it retrieves the job results via the stdout api.
     * The status and output attributes are populated in the process.
     */
    private fun runJobTemplateOnAWX(awxClient : BlueprintWebClientService, job_template_name: String?, jtId: String) {
        val mapper = ObjectMapper()

        setNodeOutputProperties( "preparing".asJsonPrimitive(), "".asJsonPrimitive())

        // Get Job Template requirements
        var response = awxClient.exchangeResource(GET, "/api/v2/job_templates/${jtId}/launch/","")
        val jtLaunchReqs: JsonNode = mapper.readTree(response.body)
        var payload = prepareLaunchPayload(awxClient, jtLaunchReqs)
        log.info("Running job with $payload, for requestId $processId.")

        // Launch the job for the targeted template
        var jtLaunched : JsonNode = JacksonUtils.jsonNode("{}") as ObjectNode
        response = awxClient.exchangeResource(POST, "/api/v2/job_templates/${jtId}/launch/", payload)
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
                response = awxClient.exchangeResource(GET, "/api/v2/jobs/${jobId}/", "")
                val jobLaunched: JsonNode = mapper.readTree(response.body)
                jobStatus = jobLaunched.at("/status").asText()
                jobEndTime = jobLaunched.at("/finished").asText()
                Thread.sleep(CHECKDELAY)
            }

            log.info("Execution of job template $job_template_name in job #$jobId finished with status ($jobStatus) for requestId $processId")

            // Get job execution results (stdout)
            val plainTextHeaders = mutableMapOf<String, String>()
            plainTextHeaders["Content-Type"] = "text/plain ;utf-8"
            response = awxClient.exchangeResource(GET, "/api/v2/jobs/${jobId}/stdout/?format=txt","", plainTextHeaders)

            setNodeOutputProperties( jobStatus.asJsonPrimitive(), response.body.asJsonPrimitive())
        } else {
            // The job template requirements were not fulfilled with the values passed in. The message below will
            // provide more information via the response, like the ignored_fields, or variables_needed_to_start,
            // or resources_needed_to_start, in order to help user pinpoint the problems with the request.
            val message = "Execution of job template $job_template_name could not be started for requestId $processId." +
                    " (Response: ${response.body}) "
            log.error(message)
            setNodeOutputErrors( ATTRIBUTE_EXEC_CMD_STATUS_ERROR, message)
        }
    }

    /**
     * Prepares the JSON payload expected by the job template api,
     * by applying the overrides that were provided
     * and allowed by the template definition flags in jtLaunchReqs
     */
    private fun prepareLaunchPayload(awxClient : BlueprintWebClientService, jtLaunchReqs: JsonNode): String {
        val payload = JacksonUtils.jsonNode("{}") as ObjectNode

        // Parameter defaults
        val limitProp = getOptionalOperationInput(INPUT_LIMIT_TO_HOST)?.asText()
        val tagsProp = getOptionalOperationInput(INPUT_TAGS)?.asText()
        val skipTagsProp = getOptionalOperationInput(INPUT_SKIP_TAGS)?.asText()
        val inventoryProp : String? = getOptionalOperationInput(INPUT_INVENTORY)?.asText()
        val extraArgs : JsonNode = getOperationInput(INPUT_EXTRA_VARS)

        val askLimitOnLaunch = jtLaunchReqs.at( "/ask_limit_on_launch").asBoolean()
        if (askLimitOnLaunch && limitProp!!.isNotEmpty()) {
            payload.put(INPUT_LIMIT_TO_HOST, limitProp)
        }
        val askTagsOnLaunch = jtLaunchReqs.at("/ask_tags_on_launch").asBoolean()
        if (askTagsOnLaunch && tagsProp!!.isNotEmpty()) {
            payload.put(INPUT_TAGS, tagsProp)
        }
        if (askTagsOnLaunch && skipTagsProp!!.isNotEmpty()) {
            payload.put("skip_tags", skipTagsProp)
        }
        val askInventoryOnLaunch = jtLaunchReqs.at("/ask_inventory_on_launch").asBoolean()
        if (askInventoryOnLaunch && inventoryProp != null) {
            var inventoryKeyId = inventoryProp.toIntOrNull()
            if (inventoryKeyId == null) {
                inventoryKeyId = resolveInventoryIdByName(awxClient, inventoryProp)
            }
            payload.put(INPUT_INVENTORY, inventoryKeyId)
        }
        val askVariablesOnLaunch = jtLaunchReqs.at("/ask_variables_on_launch").asBoolean()
        if (askVariablesOnLaunch && extraArgs != null) {
            payload.put("extra_vars", extraArgs)
        }

        val strPayload = "$payload"

        return strPayload
    }

    private fun resolveInventoryIdByName(awxClient : BlueprintWebClientService, inventoryProp: String): Int? {
        var invId : Int? = null

        // Get Inventory by name
        val encoded = URLEncoder.encode(inventoryProp)
        val response = awxClient.exchangeResource(GET,"/api/v2/inventories/?name=$encoded","")
        if (response.status in HTTP_SUCCESS) {
            val mapper = ObjectMapper()

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
    private fun setNodeOutputProperties(status: JsonNode, message: JsonNode) {
        setAttribute(ATTRIBUTE_EXEC_CMD_STATUS, status)
        log.info("Executor status: $status")
        setAttribute(ATTRIBUTE_EXEC_CMD_LOG, message)
        log.info("Executor message: $message")
    }

    /**
     * Utility function to set the output properties and errors of the executor node, in cas of errors
     */
    private fun setNodeOutputErrors(status: String, message: String) {
        setAttribute(ATTRIBUTE_EXEC_CMD_STATUS, status.asJsonPrimitive())
        setAttribute(ATTRIBUTE_EXEC_CMD_LOG, message.asJsonPrimitive())

        addError(status, ATTRIBUTE_EXEC_CMD_LOG, message)
    }
}