/*
 *  Copyright © 2020 Huawei.
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
package org.onap.ccsdk.cds.blueprintsprocessor.functions.restful.executor

import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ArrayNode
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.restful.executor.nrmfunction.RestfulNRMServiceClient
import org.slf4j.LoggerFactory

abstract class RestfulCMComponentFunction : AbstractScriptComponentFunction() {

    private val log = LoggerFactory.getLogger(RestfulCMComponentFunction::class.java)
    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        throw BluePrintException("Not Implemented required")
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        throw BluePrintException("Not Implemented required")
    }

    open fun bluePrintRestLibPropertyService(): BluePrintRestLibPropertyService =
        functionDependencyInstanceAsType(RestLibConstants.SERVICE_BLUEPRINT_REST_LIB_PROPERTY)

    fun restClientService(clientInfo: JsonNode): BlueprintWebClientService {
        return bluePrintRestLibPropertyService().blueprintWebClientService(clientInfo)
    }

    fun processNRM(executionRequest: ExecutionServiceInput, input_params: ArrayNode): String {
        // process the managed object instances
        log.info("Processing NRM Object")
        operationInputs = executionServiceInput.stepData!!.properties
        val dynamic_properties = operationInputs.get("dynamic-properties")
        // instantiate one restClientService instance
        val hostname = dynamic_properties?.get("hostname").toString().replace("\"", "")
        val port = dynamic_properties?.get("port").toString().replace("\"", "")
        val username = dynamic_properties?.get("username").toString().replace("\"", "")
        val password = dynamic_properties?.get("password").toString().replace("\"", "")
        val url = "http://" + hostname + ":" + port
        val RestInfo: String = "{\n" +
            " \"type\" : \"basic-auth\",\n" +
            " \"url\" : \"" + url + "\",\n" +
            " \"username\" : \"" + username + "\",\n" +
            " \"password\" : \"" + password + "\"\n" +
            "}"
        val mapper = ObjectMapper()
        val jsonRestInfo: JsonNode = mapper.readTree(RestInfo)
        val web_client_service = restClientService(jsonRestInfo)
        val managed_object_instances = input_params
        var response = JacksonUtils.jsonNode("{}") as ObjectNode
        // Invoke the corresponding function according to the workflowname
        when (this.workflowName) {
            "config-deploy" -> {
                for (managed_object_instance in managed_object_instances) {
                    // invoke createMOI for each managed-object-instance
                    log.info("invoke createMOI for each managed-object-instance")
                    var NRM_Restful_client = RestfulNRMServiceClient()
                    val MOI_id = NRM_Restful_client.generateMOIid()
                    var httpresponse = NRM_Restful_client.createMOI(web_client_service, MOI_id, managed_object_instance)
                    var MOIname = managed_object_instance.get("className").toString().replace("\"", "")
                    response.put("/$MOIname/$MOI_id", httpresponse)
                }
            }
            "config-get" -> {
                for (managed_object_instance in managed_object_instances) {
                    // invoke getMOIAttributes for each managed-object-instance
                    log.info("invoke getMOIAttributes for each managed-object-instance")
                    var NRM_Restful_client = RestfulNRMServiceClient()
                    val MOI_id = managed_object_instance.get("id").toString().replace("\"", "")
                    var httpresponse = NRM_Restful_client.getMOIAttributes(web_client_service, MOI_id, managed_object_instance)
                    var MOIname = managed_object_instance.get("className").toString().replace("\"", "")
                    response.put("/$MOIname/$MOI_id", httpresponse)
                }
            }
            "config-modify" -> {
                for (managed_object_instance in managed_object_instances) {
                    // invoke modifyMOIAttributes for each managed-object-instance
                    log.info("invoke modifyMOIAttributes for each managed-object-instance")
                    var NRM_Restful_client = RestfulNRMServiceClient()
                    val MOI_id = managed_object_instance.get("id").toString().replace("\"", "")
                    var httpresponse = NRM_Restful_client.modifyMOIAttributes(web_client_service, MOI_id, managed_object_instance)
                    var MOIname = managed_object_instance.get("className").toString().replace("\"", "")
                    response.put("/$MOIname/$MOI_id", httpresponse)
                }
            }
            "config-delete" -> {
                for (managed_object_instance in managed_object_instances) {
                    // invoke deleteMOI for each managed-object-instance
                    log.info("invoke deleteMOI for each managed-object-instance")
                    var NRM_Restful_client = RestfulNRMServiceClient()
                    val MOI_id = managed_object_instance.get("id").toString().replace("\"", "")
                    var httpresponse = NRM_Restful_client.deleteMOI(web_client_service, MOI_id, managed_object_instance)
                    var MOIname = managed_object_instance.get("className").toString().replace("\"", "")
                    response.put("/$MOIname/$MOI_id", httpresponse)
                }
            }
            else -> {
                print("not Implemented")
                response.put(this.workflowName, "Not Implemented")
            }
        }
        val strresponse = "$response"
        return strresponse
    }
}
