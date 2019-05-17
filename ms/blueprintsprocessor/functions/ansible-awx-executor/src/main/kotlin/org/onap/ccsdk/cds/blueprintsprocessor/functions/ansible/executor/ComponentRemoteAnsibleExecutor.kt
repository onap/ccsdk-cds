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
import com.fasterxml.jackson.databind.node.ObjectNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.*
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ExecutionServiceConstant
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Scope
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

@ConditionalOnBean(name = [ExecutionServiceConstant.SERVICE_GRPC_REMOTE_SCRIPT_EXECUTION])
@Component("component-remote-ansible-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentRemoteAnsibleExecutor(blueprintRestLibPropertyService: BluePrintRestLibPropertyService) : AbstractComponentFunction() {

    // TODO : this should be create per request based on endPointSelector
    private val restClientService = blueprintRestLibPropertyService.blueprintWebClientService("awx")

    private val log = LoggerFactory.getLogger(ComponentRemoteAnsibleExecutor::class.java)!!
    private val CHECKDELAY: Long = 5000

    companion object {
        const val INPUT_ENDPOINT_SELECTOR = "endpoint-selector"
        const val INPUT_JOB_TEMPLATE_NAME = "job-template-name"
        const val INPUT_LIMIT_TO_HOST = "limit"
        const val INPUT_INVENTORY = "inventory"
        const val INPUT_EXTRA_VARS = "extra-vars"
        const val INPUT_TAGS = "tags"
        const val INPUT_SKIP_TAGS = "skip-tags"

        const val ATTRIBUTE_EXEC_CMD_STATUS = "execute-command-status"
        const val ATTRIBUTE_EXEC_CMD_LOG = "execute-command-logs"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        log.info("Processing : $operationInputs")

        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()
        val blueprintName = bluePrintContext.name()
        val blueprintVersion = bluePrintContext.version()
        val operationAssignment: OperationAssignment = bluePrintContext
                .nodeTemplateInterfaceOperation(nodeTemplateName, interfaceName, operationName)

        var job_template_name = getOperationInput(INPUT_JOB_TEMPLATE_NAME).asText()

        // TODO create webclient based on Endpoint selector
        val endPointSelector = getOperationInput(INPUT_ENDPOINT_SELECTOR).asText()

        try {
            val jtId: String = lookupJobTemplateIDByName(restClientService, job_template_name, endPointSelector)

            if (jtId.isNotEmpty()) {
                runJobTemplateOnAWX(restClientService, job_template_name, jtId, endPointSelector)
            } else {
                addError("error", ATTRIBUTE_EXEC_CMD_STATUS, "Failed")
                addError("error", ATTRIBUTE_EXEC_CMD_LOG, "ERROR : job template ${job_template_name} does not exists")
            }

        } catch (e: Exception) {
            addError("error", ATTRIBUTE_EXEC_CMD_STATUS, "Failed")
            addError("error", ATTRIBUTE_EXEC_CMD_LOG, "ERROR : Failed to process on remote executor (${e.message})")

            log.error("Failed to process on remote executor", e)
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        bluePrintRuntimeService.getBluePrintError()
                .addError("Failed in ComponentRemoteAnsibleExecutor : ${runtimeException.message}")
    }

    /**
     * Finds the job template ID based on the job template name provided in the request
     */
    private fun lookupJobTemplateIDByName(awxClient : BlueprintWebClientService, job_template_name: String?, endPointSelector: String): String {
        val mapper = ObjectMapper()

        // Get Job Template details by name
        var response = awxClient.exchangeResource(HttpMethod.GET.name, "/api/v2/job_templates/${job_template_name}/", "", getRequestHeaders(endPointSelector))
        val jtDetails: JsonNode = mapper.readTree(response.body)
        val jtId: String = jtDetails.at("/id").asText()
        return jtId
    }

    /**
     * Performs the job template execution on AWX, ie. preprare arguments as per job template
     * requirements (ask fields) and provided overriding values. Then it launches the run, and monitors
     * its execution. Finally, it retrieves the job results via the stdout api.
     * The status and output attributes are populated in the process.
     */
    private fun runJobTemplateOnAWX(awxClient : BlueprintWebClientService, job_template_name: String?, jtId: String, endPointSelector: String) {
        val mapper = ObjectMapper()

        // Get Job Template requirements
        var response = awxClient.exchangeResource(HttpMethod.GET.name, "/api/v2/job_templates/${jtId}/launch/",
                "", getRequestHeaders(endPointSelector))
        val jtLaunchReqs: JsonNode = mapper.readTree(response.body)

        var payload = prepareLaunchPayload(jtLaunchReqs)
        log.info("Payload is $payload")

        // Launch the job for the targeted template
        response = awxClient.exchangeResource(HttpMethod.POST.name, "/api/v2/job_templates/${jtId}/launch/",
                payload.asText(), getRequestHeaders(endPointSelector))
        val jtLaunched: JsonNode = mapper.readTree(response.body)
        val jobId: String = jtLaunched.at("/id").asText()
        val fieldsIgnored: String = jtLaunched.at("/ignored_fields").asText()
        log.info("Ignored from request : $fieldsIgnored")

        // Poll current job status while job is not executed
        var jobStatus = "unknown"
        var jobEndTime = "null"
        while (jobEndTime == "null") {
            response = awxClient.exchangeResource(HttpMethod.GET.name, "/api/v2/jobs/${jobId}/", "",
                    getRequestHeaders(endPointSelector))
            val jobLaunched: JsonNode = mapper.readTree(response.body)
            jobStatus = jobLaunched.at("/status").asText()
            jobEndTime = jobLaunched.at("/finished").asText()
            Thread.sleep(CHECKDELAY)
        }

        log.info("Execution of job template $job_template_name in job #$jobId finished with status ($jobStatus) for requestId $processId")
        setAttribute(ATTRIBUTE_EXEC_CMD_STATUS, jobStatus.asJsonPrimitive())

        // Get job execution results (stdout)
        response = awxClient.exchangeResource(HttpMethod.GET.name, "/api/v2/jobs/${jobId}/stdout/?format=txt",
                "", getRequestHeaders(endPointSelector))
        setAttribute(ATTRIBUTE_EXEC_CMD_LOG, response.body.asJsonPrimitive())
        log.info("Output of job #$jobId : $response")
    }

    /**
     * Returns the cached request headers; including the auth token acdording to
     * the targeted endPointSelector
     */
    private fun getRequestHeaders(endPointSelector: String): MutableMap<String, String> {
        // TODO this should be allocated, per endpoint, upon first access, and cached in memory
        val bearerToken = "2cGzytOqe6bpDWnFIRbVqK1FLFj20l"

        // TODO add caching per endpoint
        val headers = mutableMapOf<String, String>()
        headers["Content-Type"] = "application/json"
        headers["Authorization"] = "Bearer " + bearerToken
        return headers
    }

    /**
     * Prepares the JSON payload expected by the job template api,
     * by applying the overrides that were provided
     * and allowed by the template definition jtLaunchReqs
     */
    private fun prepareLaunchPayload(jtLaunchReqs: JsonNode): ObjectNode {
        val payload = JacksonUtils.jsonNode("{}") as ObjectNode

        // Parameter defaults
        val limitProp = getOptionalOperationInput(INPUT_LIMIT_TO_HOST)?.asText()
        val tagsProp = getOptionalOperationInput(INPUT_TAGS)?.asText()
        val skipTagsProp = getOptionalOperationInput(INPUT_SKIP_TAGS)?.asText()
        val inventoryProp = getOptionalOperationInput(INPUT_INVENTORY)?.asInt()
        val extra_args = getOptionalOperationInput(INPUT_EXTRA_VARS)?.asText()

        var ask_limit_on_launch = jtLaunchReqs.at("/ask_limit_on_launch").asBoolean()
        if (ask_limit_on_launch && limitProp!!.isNotEmpty()) {
            payload.put("limit", limitProp)
        }
        var ask_tags_on_launch = jtLaunchReqs.at("/ask_tags_on_launch").asBoolean()
        if (ask_tags_on_launch && tagsProp!!.isNotEmpty()) {
            payload.put("tags", tagsProp)
        }
        if (ask_tags_on_launch && skipTagsProp!!.isNotEmpty()) {
            payload.put("skip-tags", skipTagsProp)
        }
        var ask_inventory_on_launch = jtLaunchReqs.at("/ask_inventory_on_launch").asBoolean()
        if (ask_inventory_on_launch && inventoryProp != null) {
            payload.put("inventory", inventoryProp)
        }
        if (extra_args != null && extra_args.isNotEmpty()) {
            payload.put("extra_args", extra_args)
        }

        return payload
    }
}