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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StepData
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService.WebClientResponse
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.putJsonElement
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@Suppress("SameParameterValue")
class ComponentRemoteAnsibleExecutorTest {

    private val webClientService = mockk<BlueprintWebClientService>()

    companion object {

        private const val jtId = 9
        private const val jobId = 223

        private val mapper = ObjectMapper()

        // IMPORTANT: must match the corresponding properties blueprintsprocessor.restclient.awx.* on
        // "application-test.properties"
        private const val endpointSelector = """{
            "type": "token-auth",
            "url": "http://142.44.184.236",
            "token": "Bearer J9gEtMDqf7P4YsJ7444fioY9VAhLDIs1"
            }"""
    }

    @Test
    fun testComponentRemoteAnsibleExecutor() {

        every {
            webClientService.exchangeResource("GET", "/api/v2/job_templates/hello_world_job_template/", "")
        } returns WebClientResponse(200, getJobTemplates(jtId))
        every {
            webClientService.exchangeResource("GET", "/api/v2/job_templates/$jtId/launch/", "")
        } returns WebClientResponse(200, getJobTemplateLaunch(jtId))
        every {
            webClientService.exchangeResource("GET", "/api/v2/inventories/?name=Demo+Inventory", "")
        } returns WebClientResponse(200, getInventory())
        every {
            webClientService.exchangeResource(
                "POST", "/api/v2/job_templates/$jtId/launch/",
                """{"inventory":1,"extra_vars":{"site_id":"3 - Belmont","tor_group":"vEPC"}}"""
            )
        } returns WebClientResponse(201, newJobTemplateLaunch(jtId, jobId))
        every {
            webClientService.exchangeResource("GET", "/api/v2/jobs/$jobId/", "")
        } returnsMany listOf(
            WebClientResponse(200, getJobStatus1(jtId, jobId)),
            WebClientResponse(200, getJobStatus2(jtId, jobId)),
            WebClientResponse(200, getJobStatus3(jtId, jobId)),
            WebClientResponse(200, getJobStatus4(jtId, jobId))
        )
        every {
            webClientService.exchangeResource(
                "GET", "/api/v2/jobs/$jobId/stdout/?format=txt", "",
                mapOf("Accept" to "text/plain")
            )
        } returns WebClientResponse(200, getReport())
        val selector = mapper.readTree(endpointSelector)
        val bluePrintRestLibPropertyService = mockk<BlueprintRestLibPropertyService>()
        every { bluePrintRestLibPropertyService.blueprintWebClientService(selector) } returns webClientService
        val awxRemoteExecutor = ComponentRemoteAnsibleExecutor(bluePrintRestLibPropertyService, mapper)
        awxRemoteExecutor.checkDelay = 1

        val executionServiceInput = JacksonUtils.readValueFromClassPathFile(
            "payload/requests/sample-remote-ansible-request.json",
            ExecutionServiceInput::class.java
        )!!

        val bluePrintRuntimeService = createBlueprintRuntimeService(awxRemoteExecutor, executionServiceInput)

        // when
        runBlocking {
            awxRemoteExecutor.applyNB(executionServiceInput)
        }

        // then
        assertTrue(bluePrintRuntimeService.getBlueprintError().errors.isEmpty())
    }

    @Test
    fun `handle unknown inventory`() {

        every {
            webClientService.exchangeResource("GET", "/api/v2/job_templates/hello_world_job_template/", "")
        } returns WebClientResponse(200, getJobTemplates(jtId))
        every {
            webClientService.exchangeResource("GET", "/api/v2/job_templates/$jtId/launch/", "")
        } returns WebClientResponse(200, getJobTemplateLaunch(jtId))
        every {
            webClientService.exchangeResource("GET", "/api/v2/inventories/?name=Demo+Inventory", "")
        } returns WebClientResponse(404, "")
        val selector = mapper.readTree(endpointSelector)
        val bluePrintRestLibPropertyService = mockk<BlueprintRestLibPropertyService>()
        every { bluePrintRestLibPropertyService.blueprintWebClientService(selector) } returns webClientService
        val awxRemoteExecutor = ComponentRemoteAnsibleExecutor(bluePrintRestLibPropertyService, mapper)
        awxRemoteExecutor.checkDelay = 1

        val executionServiceInput = JacksonUtils.readValueFromClassPathFile(
            "payload/requests/remote-ansible-request-full.json",
            ExecutionServiceInput::class.java
        )!!

        val bluePrintRuntimeService = createBlueprintRuntimeService(awxRemoteExecutor, executionServiceInput)

        // when
        runBlocking {
            awxRemoteExecutor.applyNB(executionServiceInput)
        }

        // then
        val errors = bluePrintRuntimeService.getBlueprintError().errors
        assertEquals(1, errors.size)
    }

    @Test
    fun `handle failure on job submission`() {

        every {
            webClientService.exchangeResource("GET", "/api/v2/job_templates/hello_world_job_template/", "")
        } returns WebClientResponse(200, getJobTemplates(jtId))
        every {
            webClientService.exchangeResource("GET", "/api/v2/job_templates/$jtId/launch/", "")
        } returns WebClientResponse(200, getJobTemplateLaunch(jtId))
        every {
            webClientService.exchangeResource("GET", "/api/v2/inventories/?name=Demo+Inventory", "")
        } returns WebClientResponse(200, getInventory())
        every {
            webClientService.exchangeResource(
                "POST", "/api/v2/job_templates/$jtId/launch/",
                """{"limit":"123","tags":"some-tag","skip_tags":"some-skip-tag","inventory":1,"extra_vars":{"site_id":"3 - Belmont","tor_group":"vEPC"}}"""
            )
        } returns WebClientResponse(500, "")
        val selector = mapper.readTree(endpointSelector)
        val bluePrintRestLibPropertyService = mockk<BlueprintRestLibPropertyService>()
        every { bluePrintRestLibPropertyService.blueprintWebClientService(selector) } returns webClientService
        val awxRemoteExecutor = ComponentRemoteAnsibleExecutor(bluePrintRestLibPropertyService, mapper)
        awxRemoteExecutor.checkDelay = 1

        val executionServiceInput = JacksonUtils.readValueFromClassPathFile(
            "payload/requests/remote-ansible-request-full.json",
            ExecutionServiceInput::class.java
        )!!

        val bluePrintRuntimeService = createBlueprintRuntimeService(awxRemoteExecutor, executionServiceInput)

        // when
        runBlocking {
            awxRemoteExecutor.applyNB(executionServiceInput)
        }

        // then
        val errors = bluePrintRuntimeService.getBlueprintError().errors
        assertEquals(1, errors.size)
    }

    private fun createBlueprintRuntimeService(
        awxRemoteExecutor: ComponentRemoteAnsibleExecutor,
        executionServiceInput: ExecutionServiceInput
    ): BlueprintRuntimeService<MutableMap<String, JsonNode>> {
        val bluePrintRuntimeService = BlueprintMetadataUtils.bluePrintRuntime(
            "123456-1000",
            "./../../../../components/model-catalog/blueprint-model/test-blueprint/remote_ansible"
        )
        awxRemoteExecutor.bluePrintRuntimeService = bluePrintRuntimeService

        val workflowName = executionServiceInput.actionIdentifiers.actionName

        // Assign Workflow inputs
        val input = executionServiceInput.payload.get("$workflowName-request")
        bluePrintRuntimeService.assignWorkflowInputs(workflowName, input)

        val stepMetaData: MutableMap<String, JsonNode> = hashMapOf()
        stepMetaData.putJsonElement(BlueprintConstants.PROPERTY_CURRENT_NODE_TEMPLATE, "execute-remote-ansible")
        stepMetaData.putJsonElement(BlueprintConstants.PROPERTY_CURRENT_INTERFACE, "ComponentRemoteAnsibleExecutor")
        stepMetaData.putJsonElement(BlueprintConstants.PROPERTY_CURRENT_OPERATION, "process")

        val stepInputData = StepData().apply {
            name = "execute-remote-ansible"
            properties = stepMetaData
        }
        executionServiceInput.stepData = stepInputData
        return bluePrintRuntimeService
    }

    private fun getJobTemplates(jtId: Int) = """{
      "id": $jtId,
      "type": "job_template",
      "url": "/api/v2/job_templates/$jtId/",
      "related": {
        "named_url": "/api/v2/job_templates/hello_world_job_template/",
        "created_by": "/api/v2/users/1/",
        "modified_by": "/api/v2/users/1/",
        "labels": "/api/v2/job_templates/$jtId/labels/",
        "inventory": "/api/v2/inventories/1/",
        "project": "/api/v2/projects/8/",
        "extra_credentials": "/api/v2/job_templates/$jtId/extra_credentials/",
        "credentials": "/api/v2/job_templates/$jtId/credentials/",
        "last_job": "/api/v2/jobs/222/",
        "jobs": "/api/v2/job_templates/$jtId/jobs/",
        "schedules": "/api/v2/job_templates/$jtId/schedules/",
        "activity_stream": "/api/v2/job_templates/$jtId/activity_stream/",
        "launch": "/api/v2/job_templates/$jtId/launch/",
        "notification_templates_any": "/api/v2/job_templates/$jtId/notification_templates_any/",
        "notification_templates_success": "/api/v2/job_templates/$jtId/notification_templates_success/",
        "notification_templates_error": "/api/v2/job_templates/$jtId/notification_templates_error/",
        "access_list": "/api/v2/job_templates/$jtId/access_list/",
        "survey_spec": "/api/v2/job_templates/$jtId/survey_spec/",
        "object_roles": "/api/v2/job_templates/$jtId/object_roles/",
        "instance_groups": "/api/v2/job_templates/$jtId/instance_groups/",
        "slice_workflow_jobs": "/api/v2/job_templates/$jtId/slice_workflow_jobs/",
        "copy": "/api/v2/job_templates/$jtId/copy/"
      },
      "summary_fields": {
        "inventory": {
          "id": 1,
          "name": "Demo Inventory",
          "description": "",
          "has_active_failures": false,
          "total_hosts": 1,
          "hosts_with_active_failures": 0,
          "total_groups": 0,
          "groups_with_active_failures": 0,
          "has_inventory_sources": false,
          "total_inventory_sources": 0,
          "inventory_sources_with_failures": 0,
          "organization_id": 1,
          "kind": ""
        },
        "project": {
          "id": 8,
          "name": "cds_playbooks",
          "description": "CDS - cds_playbooks Project",
          "status": "ok",
          "scm_type": ""
        },
        "last_job": {
          "id": 222,
          "name": "hello_world_job_template",
          "description": "hello_world Runner Job Template",
          "finished": "2019-06-12T11:20:27.892787Z",
          "status": "successful",
          "failed": false
        },
        "last_update": {
          "id": 222,
          "name": "hello_world_job_template",
          "description": "hello_world Runner Job Template",
          "status": "successful",
          "failed": false
        },
        "created_by": {
          "id": 1,
          "username": "admin",
          "first_name": "",
          "last_name": ""
        },
        "modified_by": {
          "id": 1,
          "username": "admin",
          "first_name": "",
          "last_name": ""
        },
        "object_roles": {
          "admin_role": {
            "description": "Can manage all aspects of the job template",
            "name": "Admin",
            "id": 51
          },
          "execute_role": {
            "description": "May run the job template",
            "name": "Execute",
            "id": 52
          },
          "read_role": {
            "description": "May view settings for the job template",
            "name": "Read",
            "id": 53
          }
        },
        "user_capabilities": {
          "edit": true,
          "delete": true,
          "start": true,
          "schedule": true,
          "copy": true
        },
        "labels": {
          "count": 0,
          "results": []
        },
        "survey": {
          "title": "",
          "description": ""
        },
        "recent_jobs": [
          {
            "id": 222,
            "status": "successful",
            "finished": "2019-06-12T11:20:27.892787Z",
            "type": "job"
          },
          {
            "id": 65,
            "status": "successful",
            "finished": "2019-06-03T18:27:19.114796Z",
            "type": "job"
          },
          {
            "id": 64,
            "status": "successful",
            "finished": "2019-06-03T18:26:53.606618Z",
            "type": "job"
          },
          {
            "id": 63,
            "status": "successful",
            "finished": "2019-06-03T18:24:36.072943Z",
            "type": "job"
          },
          {
            "id": 62,
            "status": "successful",
            "finished": "2019-06-03T18:17:50.616528Z",
            "type": "job"
          },
          {
            "id": 61,
            "status": "successful",
            "finished": "2019-06-03T18:04:42.995611Z",
            "type": "job"
          },
          {
            "id": 60,
            "status": "successful",
            "finished": "2019-06-03T17:47:13.983951Z",
            "type": "job"
          },
          {
            "id": 50,
            "status": "successful",
            "finished": "2019-05-30T15:47:55.700161Z",
            "type": "job"
          },
          {
            "id": 49,
            "status": "successful",
            "finished": "2019-05-29T14:46:51.615926Z",
            "type": "job"
          },
          {
            "id": 47,
            "status": "successful",
            "finished": "2019-05-27T20:23:58.656709Z",
            "type": "job"
          }
        ],
        "extra_credentials": [],
        "credentials": []
      },
      "created": "2019-05-21T19:28:05.953730Z",
      "modified": "2019-05-21T20:06:55.728697Z",
      "name": "hello_world_job_template",
      "description": "hello_world Runner Job Template",
      "job_type": "run",
      "inventory": 1,
      "project": 8,
      "playbook": "hello_world.yml",
      "forks": 0,
      "limit": "",
      "verbosity": 0,
      "extra_vars": "",
      "job_tags": "",
      "force_handlers": false,
      "skip_tags": "",
      "start_at_task": "",
      "timeout": 0,
      "use_fact_cache": false,
      "last_job_run": "2019-06-12T11:20:27.892787Z",
      "last_job_failed": false,
      "next_job_run": null,
      "status": "successful",
      "host_config_key": "",
      "ask_diff_mode_on_launch": false,
      "ask_variables_on_launch": true,
      "ask_limit_on_launch": true,
      "ask_tags_on_launch": true,
      "ask_skip_tags_on_launch": true,
      "ask_job_type_on_launch": false,
      "ask_verbosity_on_launch": false,
      "ask_inventory_on_launch": true,
      "ask_credential_on_launch": true,
      "survey_enabled": true,
      "become_enabled": false,
      "diff_mode": false,
      "allow_simultaneous": false,
      "custom_virtualenv": null,
      "job_slice_count": 1,
      "credential": null,
      "vault_credential": null
    }"""

    private fun getJobTemplateLaunch(jtId: Int) = """{
      "can_start_without_user_input": false,
      "passwords_needed_to_start": [],
      "ask_variables_on_launch": true,
      "ask_tags_on_launch": true,
      "ask_diff_mode_on_launch": false,
      "ask_skip_tags_on_launch": true,
      "ask_job_type_on_launch": false,
      "ask_limit_on_launch": true,
      "ask_verbosity_on_launch": false,
      "ask_inventory_on_launch": true,
      "ask_credential_on_launch": true,
      "survey_enabled": true,
      "variables_needed_to_start": [
        "tor_group",
        "site_id"
      ],
      "credential_needed_to_start": false,
      "inventory_needed_to_start": false,
      "job_template_data": {
        "name": "hello_world_job_template",
        "id": $jtId,
        "description": "hello_world Runner Job Template"
      },
      "defaults": {
        "extra_vars": "",
        "diff_mode": false,
        "limit": "",
        "job_tags": "",
        "skip_tags": "",
        "job_type": "run",
        "verbosity": 0,
        "inventory": {
          "name": "Demo Inventory",
          "id": 1
        }
      }
    }"""

    private fun getInventory() = """{
          "count": 1,
          "next": null,
          "previous": null,
          "results": [
            {
              "id": 1,
              "type": "inventory",
              "url": "/api/v2/inventories/1/",
              "related": {
                "created_by": "/api/v2/users/1/",
                "modified_by": "/api/v2/users/1/",
                "hosts": "/api/v2/inventories/1/hosts/",
                "groups": "/api/v2/inventories/1/groups/",
                "root_groups": "/api/v2/inventories/1/root_groups/",
                "variable_data": "/api/v2/inventories/1/variable_data/",
                "script": "/api/v2/inventories/1/script/",
                "tree": "/api/v2/inventories/1/tree/",
                "inventory_sources": "/api/v2/inventories/1/inventory_sources/",
                "update_inventory_sources": "/api/v2/inventories/1/update_inventory_sources/",
                "activity_stream": "/api/v2/inventories/1/activity_stream/",
                "job_templates": "/api/v2/inventories/1/job_templates/",
                "ad_hoc_commands": "/api/v2/inventories/1/ad_hoc_commands/",
                "access_list": "/api/v2/inventories/1/access_list/",
                "object_roles": "/api/v2/inventories/1/object_roles/",
                "instance_groups": "/api/v2/inventories/1/instance_groups/",
                "copy": "/api/v2/inventories/1/copy/",
                "organization": "/api/v2/organizations/1/"
              },
              "summary_fields": {
                "organization": {
                  "id": 1,
                  "name": "Default",
                  "description": ""
                },
                "created_by": {
                  "id": 1,
                  "username": "admin",
                  "first_name": "",
                  "last_name": ""
                },
                "modified_by": {
                  "id": 1,
                  "username": "admin",
                  "first_name": "",
                  "last_name": ""
                },
                "object_roles": {
                  "admin_role": {
                    "description": "Can manage all aspects of the inventory",
                    "name": "Admin",
                    "id": 21
                  },
                  "update_role": {
                    "description": "May update project or inventory or group using the configured source update system",
                    "name": "Update",
                    "id": 22
                  },
                  "adhoc_role": {
                    "description": "May run ad hoc commands on an inventory",
                    "name": "Ad Hoc",
                    "id": 23
                  },
                  "use_role": {
                    "description": "Can use the inventory in a job template",
                    "name": "Use",
                    "id": 24
                  },
                  "read_role": {
                    "description": "May view settings for the inventory",
                    "name": "Read",
                    "id": 25
                  }
                },
                "user_capabilities": {
                  "edit": true,
                  "delete": true,
                  "copy": true,
                  "adhoc": true
                }
              },
              "created": "2019-05-21T15:45:31.954359Z",
              "modified": "2019-05-21T15:45:31.954378Z",
              "name": "Demo Inventory",
              "description": "",
              "organization": 1,
              "kind": "",
              "host_filter": null,
              "variables": "",
              "has_active_failures": false,
              "total_hosts": 1,
              "hosts_with_active_failures": 0,
              "total_groups": 0,
              "groups_with_active_failures": 0,
              "has_inventory_sources": false,
              "total_inventory_sources": 0,
              "inventory_sources_with_failures": 0,
              "insights_credential": null,
              "pending_deletion": false
            }
          ]
        }"""

    private fun newJobTemplateLaunch(jtId: Int, jobId: Int) = """{
      "job": $jobId,
      "ignored_fields": {},
      "id": $jobId,
      "type": "job",
      "url": "/api/v2/jobs/$jobId/",
      "related": {
        "created_by": "/api/v2/users/1/",
        "modified_by": "/api/v2/users/1/",
        "labels": "/api/v2/jobs/$jobId/labels/",
        "inventory": "/api/v2/inventories/1/",
        "project": "/api/v2/projects/8/",
        "extra_credentials": "/api/v2/jobs/$jobId/extra_credentials/",
        "credentials": "/api/v2/jobs/$jobId/credentials/",
        "unified_job_template": "/api/v2/job_templates/$jtId/",
        "stdout": "/api/v2/jobs/$jobId/stdout/",
        "job_events": "/api/v2/jobs/$jobId/job_events/",
        "job_host_summaries": "/api/v2/jobs/$jobId/job_host_summaries/",
        "activity_stream": "/api/v2/jobs/$jobId/activity_stream/",
        "notifications": "/api/v2/jobs/$jobId/notifications/",
        "job_template": "/api/v2/job_templates/$jtId/",
        "cancel": "/api/v2/jobs/$jobId/cancel/",
        "create_schedule": "/api/v2/jobs/$jobId/create_schedule/",
        "relaunch": "/api/v2/jobs/$jobId/relaunch/"
      },
      "summary_fields": {
        "inventory": {
          "id": 1,
          "name": "Demo Inventory",
          "description": "",
          "has_active_failures": false,
          "total_hosts": 1,
          "hosts_with_active_failures": 0,
          "total_groups": 0,
          "groups_with_active_failures": 0,
          "has_inventory_sources": false,
          "total_inventory_sources": 0,
          "inventory_sources_with_failures": 0,
          "organization_id": 1,
          "kind": ""
        },
        "project": {
          "id": 8,
          "name": "cds_playbooks",
          "description": "CDS - cds_playbooks Project",
          "status": "ok",
          "scm_type": ""
        },
        "job_template": {
          "id": $jtId,
          "name": "hello_world_job_template",
          "description": "hello_world Runner Job Template"
        },
        "unified_job_template": {
          "id": $jtId,
          "name": "hello_world_job_template",
          "description": "hello_world Runner Job Template",
          "unified_job_type": "job"
        },
        "created_by": {
          "id": 1,
          "username": "admin",
          "first_name": "",
          "last_name": ""
        },
        "modified_by": {
          "id": 1,
          "username": "admin",
          "first_name": "",
          "last_name": ""
        },
        "user_capabilities": {
          "delete": true,
          "start": true
        },
        "labels": {
          "count": 0,
          "results": []
        },
        "extra_credentials": [],
        "credentials": []
      },
      "created": "2019-06-12T11:21:26.891986Z",
      "modified": "2019-06-12T11:21:27.016410Z",
      "name": "hello_world_job_template",
      "description": "hello_world Runner Job Template",
      "job_type": "run",
      "inventory": 1,
      "project": 8,
      "playbook": "hello_world.yml",
      "forks": 0,
      "limit": "",
      "verbosity": 0,
      "extra_vars": "{\"tor_group\": \"vEPC\", \"site_id\": \"3 - Belmont\"}",
      "job_tags": "",
      "force_handlers": false,
      "skip_tags": "",
      "start_at_task": "",
      "timeout": 0,
      "use_fact_cache": false,
      "unified_job_template": $jtId,
      "launch_type": "manual",
      "status": "pending",
      "failed": false,
      "started": null,
      "finished": null,
      "elapsed": 0,
      "job_args": "",
      "job_cwd": "",
      "job_env": {},
      "job_explanation": "",
      "execution_node": "",
      "controller_node": "",
      "result_traceback": "",
      "event_processing_finished": false,
      "job_template": $jtId,
      "passwords_needed_to_start": [],
      "ask_diff_mode_on_launch": false,
      "ask_variables_on_launch": true,
      "ask_limit_on_launch": true,
      "ask_tags_on_launch": true,
      "ask_skip_tags_on_launch": true,
      "ask_job_type_on_launch": false,
      "ask_verbosity_on_launch": false,
      "ask_inventory_on_launch": true,
      "ask_credential_on_launch": true,
      "allow_simultaneous": false,
      "artifacts": {},
      "scm_revision": "",
      "instance_group": null,
      "diff_mode": false,
      "job_slice_number": 0,
      "job_slice_count": 1,
      "credential": null,
      "vault_credential": null
    }"""

    private fun getJobStatus1(jtId: Int, jobId: Int) = """{
      "id": $jobId,
      "type": "job",
      "url": "/api/v2/jobs/$jobId/",
      "related": {
        "created_by": "/api/v2/users/1/",
        "labels": "/api/v2/jobs/$jobId/labels/",
        "inventory": "/api/v2/inventories/1/",
        "project": "/api/v2/projects/8/",
        "extra_credentials": "/api/v2/jobs/$jobId/extra_credentials/",
        "credentials": "/api/v2/jobs/$jobId/credentials/",
        "unified_job_template": "/api/v2/job_templates/$jtId/",
        "stdout": "/api/v2/jobs/$jobId/stdout/",
        "job_events": "/api/v2/jobs/$jobId/job_events/",
        "job_host_summaries": "/api/v2/jobs/$jobId/job_host_summaries/",
        "activity_stream": "/api/v2/jobs/$jobId/activity_stream/",
        "notifications": "/api/v2/jobs/$jobId/notifications/",
        "job_template": "/api/v2/job_templates/$jtId/",
        "cancel": "/api/v2/jobs/$jobId/cancel/",
        "create_schedule": "/api/v2/jobs/$jobId/create_schedule/",
        "relaunch": "/api/v2/jobs/$jobId/relaunch/"
      },
      "summary_fields": {
        "inventory": {
          "id": 1,
          "name": "Demo Inventory",
          "description": "",
          "has_active_failures": false,
          "total_hosts": 1,
          "hosts_with_active_failures": 0,
          "total_groups": 0,
          "groups_with_active_failures": 0,
          "has_inventory_sources": false,
          "total_inventory_sources": 0,
          "inventory_sources_with_failures": 0,
          "organization_id": 1,
          "kind": ""
        },
        "project": {
          "id": 8,
          "name": "cds_playbooks",
          "description": "CDS - cds_playbooks Project",
          "status": "ok",
          "scm_type": ""
        },
        "job_template": {
          "id": $jtId,
          "name": "hello_world_job_template",
          "description": "hello_world Runner Job Template"
        },
        "unified_job_template": {
          "id": $jtId,
          "name": "hello_world_job_template",
          "description": "hello_world Runner Job Template",
          "unified_job_type": "job"
        },
        "instance_group": {
          "name": "tower",
          "id": 1
        },
        "created_by": {
          "id": 1,
          "username": "admin",
          "first_name": "",
          "last_name": ""
        },
        "user_capabilities": {
          "delete": true,
          "start": true
        },
        "labels": {
          "count": 0,
          "results": []
        },
        "extra_credentials": [],
        "credentials": []
      },
      "created": "2019-06-12T11:21:26.891986Z",
      "modified": "2019-06-12T11:21:27.355185Z",
      "name": "hello_world_job_template",
      "description": "hello_world Runner Job Template",
      "job_type": "run",
      "inventory": 1,
      "project": 8,
      "playbook": "hello_world.yml",
      "forks": 0,
      "limit": "",
      "verbosity": 0,
      "extra_vars": "{\"tor_group\": \"vEPC\", \"site_id\": \"3 - Belmont\"}",
      "job_tags": "",
      "force_handlers": false,
      "skip_tags": "",
      "start_at_task": "",
      "timeout": 0,
      "use_fact_cache": false,
      "unified_job_template": $jtId,
      "launch_type": "manual",
      "status": "waiting",
      "failed": false,
      "started": null,
      "finished": null,
      "elapsed": 0,
      "job_args": "",
      "job_cwd": "",
      "job_env": {},
      "job_explanation": "",
      "execution_node": "awx",
      "controller_node": "",
      "result_traceback": "",
      "event_processing_finished": false,
      "job_template": $jtId,
      "passwords_needed_to_start": [],
      "ask_diff_mode_on_launch": false,
      "ask_variables_on_launch": true,
      "ask_limit_on_launch": true,
      "ask_tags_on_launch": true,
      "ask_skip_tags_on_launch": true,
      "ask_job_type_on_launch": false,
      "ask_verbosity_on_launch": false,
      "ask_inventory_on_launch": true,
      "ask_credential_on_launch": true,
      "allow_simultaneous": false,
      "artifacts": {},
      "scm_revision": "",
      "instance_group": 1,
      "diff_mode": false,
      "job_slice_number": 0,
      "job_slice_count": 1,
      "host_status_counts": {},
      "playbook_counts": {
        "play_count": 0,
        "task_count": 0
      },
      "custom_virtualenv": null,
      "credential": null,
      "vault_credential": null
    }"""

    private fun getJobStatus2(jtId: Int, jobId: Int) = """{
      "id": $jobId,
      "type": "job",
      "url": "/api/v2/jobs/$jobId/",
      "related": {
        "created_by": "/api/v2/users/1/",
        "labels": "/api/v2/jobs/$jobId/labels/",
        "inventory": "/api/v2/inventories/1/",
        "project": "/api/v2/projects/8/",
        "extra_credentials": "/api/v2/jobs/$jobId/extra_credentials/",
        "credentials": "/api/v2/jobs/$jobId/credentials/",
        "unified_job_template": "/api/v2/job_templates/$jtId/",
        "stdout": "/api/v2/jobs/$jobId/stdout/",
        "job_events": "/api/v2/jobs/$jobId/job_events/",
        "job_host_summaries": "/api/v2/jobs/$jobId/job_host_summaries/",
        "activity_stream": "/api/v2/jobs/$jobId/activity_stream/",
        "notifications": "/api/v2/jobs/$jobId/notifications/",
        "job_template": "/api/v2/job_templates/$jtId/",
        "cancel": "/api/v2/jobs/$jobId/cancel/",
        "create_schedule": "/api/v2/jobs/$jobId/create_schedule/",
        "relaunch": "/api/v2/jobs/$jobId/relaunch/"
      },
      "summary_fields": {
        "inventory": {
          "id": 1,
          "name": "Demo Inventory",
          "description": "",
          "has_active_failures": false,
          "total_hosts": 1,
          "hosts_with_active_failures": 0,
          "total_groups": 0,
          "groups_with_active_failures": 0,
          "has_inventory_sources": false,
          "total_inventory_sources": 0,
          "inventory_sources_with_failures": 0,
          "organization_id": 1,
          "kind": ""
        },
        "project": {
          "id": 8,
          "name": "cds_playbooks",
          "description": "CDS - cds_playbooks Project",
          "status": "ok",
          "scm_type": ""
        },
        "job_template": {
          "id": $jtId,
          "name": "hello_world_job_template",
          "description": "hello_world Runner Job Template"
        },
        "unified_job_template": {
          "id": $jtId,
          "name": "hello_world_job_template",
          "description": "hello_world Runner Job Template",
          "unified_job_type": "job"
        },
        "instance_group": {
          "name": "tower",
          "id": 1
        },
        "created_by": {
          "id": 1,
          "username": "admin",
          "first_name": "",
          "last_name": ""
        },
        "user_capabilities": {
          "delete": true,
          "start": true
        },
        "labels": {
          "count": 0,
          "results": []
        },
        "extra_credentials": [],
        "credentials": []
      },
      "created": "2019-06-12T11:21:26.891986Z",
      "modified": "2019-06-12T11:21:27.355185Z",
      "name": "hello_world_job_template",
      "description": "hello_world Runner Job Template",
      "job_type": "run",
      "inventory": 1,
      "project": 8,
      "playbook": "hello_world.yml",
      "forks": 0,
      "limit": "",
      "verbosity": 0,
      "extra_vars": "{\"tor_group\": \"vEPC\", \"site_id\": \"3 - Belmont\"}",
      "job_tags": "",
      "force_handlers": false,
      "skip_tags": "",
      "start_at_task": "",
      "timeout": 0,
      "use_fact_cache": false,
      "unified_job_template": $jtId,
      "launch_type": "manual",
      "status": "running",
      "failed": false,
      "started": "2019-06-12T11:21:27.510766Z",
      "finished": null,
      "elapsed": 10.862184,
      "job_args": "[\"ansible-playbook\", \"-u\", \"root\", \"-i\", \"/tmp/awx_223_ft8hu4p4/tmptmtwllu4\", \"-e\", \"@/tmp/awx_223_ft8hu4p4/env/extravars\", \"hello_world.yml\"]",
      "job_cwd": "/var/lib/awx/projects/cds_playbooks_folder",
      "job_env": {
        "HOSTNAME": "awx",
        "LC_ALL": "en_US.UTF-8",
        "VIRTUAL_ENV": "/var/lib/awx/venv/ansible",
        "PATH": "/var/lib/awx/venv/ansible/bin:/var/lib/awx/venv/awx/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
        "SUPERVISOR_GROUP_NAME": "tower-processes",
        "PWD": "/var/lib/awx",
        "LANG": "en_US.UTF-8",
        "PS1": "(awx) ",
        "SUPERVISOR_ENABLED": "1",
        "HOME": "/var/lib/awx",
        "SHLVL": "2",
        "LANGUAGE": "en_US.UTF-8",
        "LC_CTYPE": "en_US.UTF-8",
        "SUPERVISOR_PROCESS_NAME": "dispatcher",
        "SUPERVISOR_SERVER_URL": "unix:///tmp/supervisor.sock",
        "DJANGO_SETTINGS_MODULE": "awx.settings.production",
        "DJANGO_LIVE_TEST_SERVER_ADDRESS": "localhost:9013-9199",
        "TZ": "UTC",
        "ANSIBLE_FORCE_COLOR": "True",
        "ANSIBLE_HOST_KEY_CHECKING": "False",
        "ANSIBLE_INVENTORY_UNPARSED_FAILED": "True",
        "ANSIBLE_PARAMIKO_RECORD_HOST_KEYS": "False",
        "ANSIBLE_VENV_PATH": "/var/lib/awx/venv/ansible",
        "AWX_PRIVATE_DATA_DIR": "/tmp/awx_223_ft8hu4p4",
        "PYTHONPATH": "/var/lib/awx/venv/ansible/lib/python2.7/site-packages:/var/lib/awx/venv/awx/lib64/python3.6/site-packages/awx/lib:",
        "JOB_ID": "$jobId",
        "INVENTORY_ID": "1",
        "PROJECT_REVISION": "",
        "ANSIBLE_RETRY_FILES_ENABLED": "False",
        "MAX_EVENT_RES": "700000",
        "ANSIBLE_CALLBACK_PLUGINS": "/var/lib/awx/venv/awx/lib64/python3.6/site-packages/awx/plugins/callback",
        "AWX_HOST": "https://towerhost",
        "ANSIBLE_SSH_CONTROL_PATH_DIR": "/tmp/awx_223_ft8hu4p4/cp",
        "ANSIBLE_STDOUT_CALLBACK": "awx_display",
        "AWX_ISOLATED_DATA_DIR": "/tmp/awx_223_ft8hu4p4/artifacts/$jobId"
      },
      "job_explanation": "",
      "execution_node": "awx",
      "controller_node": "",
      "result_traceback": "",
      "event_processing_finished": false,
      "job_template": $jtId,
      "passwords_needed_to_start": [],
      "ask_diff_mode_on_launch": false,
      "ask_variables_on_launch": true,
      "ask_limit_on_launch": true,
      "ask_tags_on_launch": true,
      "ask_skip_tags_on_launch": true,
      "ask_job_type_on_launch": false,
      "ask_verbosity_on_launch": false,
      "ask_inventory_on_launch": true,
      "ask_credential_on_launch": true,
      "allow_simultaneous": false,
      "artifacts": {},
      "scm_revision": "",
      "instance_group": 1,
      "diff_mode": false,
      "job_slice_number": 0,
      "job_slice_count": 1,
      "host_status_counts": {},
      "playbook_counts": {
        "play_count": 1,
        "task_count": 1
      },
      "custom_virtualenv": "/var/lib/awx/venv/ansible",
      "credential": null,
      "vault_credential": null
    }"""

    private fun getJobStatus3(jtId: Int, jobId: Int) = """{
      "id": $jobId,
      "type": "job",
      "url": "/api/v2/jobs/$jobId/",
      "related": {
        "created_by": "/api/v2/users/1/",
        "labels": "/api/v2/jobs/$jobId/labels/",
        "inventory": "/api/v2/inventories/1/",
        "project": "/api/v2/projects/8/",
        "extra_credentials": "/api/v2/jobs/$jobId/extra_credentials/",
        "credentials": "/api/v2/jobs/$jobId/credentials/",
        "unified_job_template": "/api/v2/job_templates/$jtId/",
        "stdout": "/api/v2/jobs/$jobId/stdout/",
        "job_events": "/api/v2/jobs/$jobId/job_events/",
        "job_host_summaries": "/api/v2/jobs/$jobId/job_host_summaries/",
        "activity_stream": "/api/v2/jobs/$jobId/activity_stream/",
        "notifications": "/api/v2/jobs/$jobId/notifications/",
        "job_template": "/api/v2/job_templates/$jtId/",
        "cancel": "/api/v2/jobs/$jobId/cancel/",
        "create_schedule": "/api/v2/jobs/$jobId/create_schedule/",
        "relaunch": "/api/v2/jobs/$jobId/relaunch/"
      },
      "summary_fields": {
        "inventory": {
          "id": 1,
          "name": "Demo Inventory",
          "description": "",
          "has_active_failures": false,
          "total_hosts": 1,
          "hosts_with_active_failures": 0,
          "total_groups": 0,
          "groups_with_active_failures": 0,
          "has_inventory_sources": false,
          "total_inventory_sources": 0,
          "inventory_sources_with_failures": 0,
          "organization_id": 1,
          "kind": ""
        },
        "project": {
          "id": 8,
          "name": "cds_playbooks",
          "description": "CDS - cds_playbooks Project",
          "status": "ok",
          "scm_type": ""
        },
        "job_template": {
          "id": $jtId,
          "name": "hello_world_job_template",
          "description": "hello_world Runner Job Template"
        },
        "unified_job_template": {
          "id": $jtId,
          "name": "hello_world_job_template",
          "description": "hello_world Runner Job Template",
          "unified_job_type": "job"
        },
        "instance_group": {
          "name": "tower",
          "id": 1
        },
        "created_by": {
          "id": 1,
          "username": "admin",
          "first_name": "",
          "last_name": ""
        },
        "user_capabilities": {
          "delete": true,
          "start": true
        },
        "labels": {
          "count": 0,
          "results": []
        },
        "extra_credentials": [],
        "credentials": []
      },
      "created": "2019-06-12T11:21:26.891986Z",
      "modified": "2019-06-12T11:21:27.355185Z",
      "name": "hello_world_job_template",
      "description": "hello_world Runner Job Template",
      "job_type": "run",
      "inventory": 1,
      "project": 8,
      "playbook": "hello_world.yml",
      "forks": 0,
      "limit": "",
      "verbosity": 0,
      "extra_vars": "{\"tor_group\": \"vEPC\", \"site_id\": \"3 - Belmont\"}",
      "job_tags": "",
      "force_handlers": false,
      "skip_tags": "",
      "start_at_task": "",
      "timeout": 0,
      "use_fact_cache": false,
      "unified_job_template": $jtId,
      "launch_type": "manual",
      "status": "running",
      "failed": false,
      "started": "2019-06-12T11:21:27.510766Z",
      "finished": null,
      "elapsed": 21.297881,
      "job_args": "[\"ansible-playbook\", \"-u\", \"root\", \"-i\", \"/tmp/awx_223_ft8hu4p4/tmptmtwllu4\", \"-e\", \"@/tmp/awx_223_ft8hu4p4/env/extravars\", \"hello_world.yml\"]",
      "job_cwd": "/var/lib/awx/projects/cds_playbooks_folder",
      "job_env": {
        "HOSTNAME": "awx",
        "LC_ALL": "en_US.UTF-8",
        "VIRTUAL_ENV": "/var/lib/awx/venv/ansible",
        "PATH": "/var/lib/awx/venv/ansible/bin:/var/lib/awx/venv/awx/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
        "SUPERVISOR_GROUP_NAME": "tower-processes",
        "PWD": "/var/lib/awx",
        "LANG": "en_US.UTF-8",
        "PS1": "(awx) ",
        "SUPERVISOR_ENABLED": "1",
        "HOME": "/var/lib/awx",
        "SHLVL": "2",
        "LANGUAGE": "en_US.UTF-8",
        "LC_CTYPE": "en_US.UTF-8",
        "SUPERVISOR_PROCESS_NAME": "dispatcher",
        "SUPERVISOR_SERVER_URL": "unix:///tmp/supervisor.sock",
        "DJANGO_SETTINGS_MODULE": "awx.settings.production",
        "DJANGO_LIVE_TEST_SERVER_ADDRESS": "localhost:9013-9199",
        "TZ": "UTC",
        "ANSIBLE_FORCE_COLOR": "True",
        "ANSIBLE_HOST_KEY_CHECKING": "False",
        "ANSIBLE_INVENTORY_UNPARSED_FAILED": "True",
        "ANSIBLE_PARAMIKO_RECORD_HOST_KEYS": "False",
        "ANSIBLE_VENV_PATH": "/var/lib/awx/venv/ansible",
        "AWX_PRIVATE_DATA_DIR": "/tmp/awx_223_ft8hu4p4",
        "PYTHONPATH": "/var/lib/awx/venv/ansible/lib/python2.7/site-packages:/var/lib/awx/venv/awx/lib64/python3.6/site-packages/awx/lib:",
        "JOB_ID": "$jobId",
        "INVENTORY_ID": "1",
        "PROJECT_REVISION": "",
        "ANSIBLE_RETRY_FILES_ENABLED": "False",
        "MAX_EVENT_RES": "700000",
        "ANSIBLE_CALLBACK_PLUGINS": "/var/lib/awx/venv/awx/lib64/python3.6/site-packages/awx/plugins/callback",
        "AWX_HOST": "https://towerhost",
        "ANSIBLE_SSH_CONTROL_PATH_DIR": "/tmp/awx_223_ft8hu4p4/cp",
        "ANSIBLE_STDOUT_CALLBACK": "awx_display",
        "AWX_ISOLATED_DATA_DIR": "/tmp/awx_223_ft8hu4p4/artifacts/$jobId"
      },
      "job_explanation": "",
      "execution_node": "awx",
      "controller_node": "",
      "result_traceback": "",
      "event_processing_finished": false,
      "job_template": $jtId,
      "passwords_needed_to_start": [],
      "ask_diff_mode_on_launch": false,
      "ask_variables_on_launch": true,
      "ask_limit_on_launch": true,
      "ask_tags_on_launch": true,
      "ask_skip_tags_on_launch": true,
      "ask_job_type_on_launch": false,
      "ask_verbosity_on_launch": false,
      "ask_inventory_on_launch": true,
      "ask_credential_on_launch": true,
      "allow_simultaneous": false,
      "artifacts": {},
      "scm_revision": "",
      "instance_group": 1,
      "diff_mode": false,
      "job_slice_number": 0,
      "job_slice_count": 1,
      "host_status_counts": {},
      "playbook_counts": {
        "play_count": 1,
        "task_count": 2
      },
      "custom_virtualenv": "/var/lib/awx/venv/ansible",
      "credential": null,
      "vault_credential": null
    } """

    private fun getJobStatus4(jtId: Int, jobId: Int) = """{
      "id": $jobId,
      "type": "job",
      "url": "/api/v2/jobs/$jobId/",
      "related": {
        "created_by": "/api/v2/users/1/",
        "labels": "/api/v2/jobs/$jobId/labels/",
        "inventory": "/api/v2/inventories/1/",
        "project": "/api/v2/projects/8/",
        "extra_credentials": "/api/v2/jobs/$jobId/extra_credentials/",
        "credentials": "/api/v2/jobs/$jobId/credentials/",
        "unified_job_template": "/api/v2/job_templates/$jtId/",
        "stdout": "/api/v2/jobs/$jobId/stdout/",
        "job_events": "/api/v2/jobs/$jobId/job_events/",
        "job_host_summaries": "/api/v2/jobs/$jobId/job_host_summaries/",
        "activity_stream": "/api/v2/jobs/$jobId/activity_stream/",
        "notifications": "/api/v2/jobs/$jobId/notifications/",
        "job_template": "/api/v2/job_templates/$jtId/",
        "cancel": "/api/v2/jobs/$jobId/cancel/",
        "create_schedule": "/api/v2/jobs/$jobId/create_schedule/",
        "relaunch": "/api/v2/jobs/$jobId/relaunch/"
      },
      "summary_fields": {
        "inventory": {
          "id": 1,
          "name": "Demo Inventory",
          "description": "",
          "has_active_failures": false,
          "total_hosts": 1,
          "hosts_with_active_failures": 0,
          "total_groups": 0,
          "groups_with_active_failures": 0,
          "has_inventory_sources": false,
          "total_inventory_sources": 0,
          "inventory_sources_with_failures": 0,
          "organization_id": 1,
          "kind": ""
        },
        "project": {
          "id": 8,
          "name": "cds_playbooks",
          "description": "CDS - cds_playbooks Project",
          "status": "ok",
          "scm_type": ""
        },
        "job_template": {
          "id": $jtId,
          "name": "hello_world_job_template",
          "description": "hello_world Runner Job Template"
        },
        "unified_job_template": {
          "id": $jtId,
          "name": "hello_world_job_template",
          "description": "hello_world Runner Job Template",
          "unified_job_type": "job"
        },
        "instance_group": {
          "name": "tower",
          "id": 1
        },
        "created_by": {
          "id": 1,
          "username": "admin",
          "first_name": "",
          "last_name": ""
        },
        "user_capabilities": {
          "delete": true,
          "start": true
        },
        "labels": {
          "count": 0,
          "results": []
        },
        "extra_credentials": [],
        "credentials": []
      },
      "created": "2019-06-12T11:21:26.891986Z",
      "modified": "2019-06-12T11:21:27.355185Z",
      "name": "hello_world_job_template",
      "description": "hello_world Runner Job Template",
      "job_type": "run",
      "inventory": 1,
      "project": 8,
      "playbook": "hello_world.yml",
      "forks": 0,
      "limit": "",
      "verbosity": 0,
      "extra_vars": "{\"tor_group\": \"vEPC\", \"site_id\": \"3 - Belmont\"}",
      "job_tags": "",
      "force_handlers": false,
      "skip_tags": "",
      "start_at_task": "",
      "timeout": 0,
      "use_fact_cache": false,
      "unified_job_template": $jtId,
      "launch_type": "manual",
      "status": "successful",
      "failed": false,
      "started": "2019-06-12T11:21:27.510766Z",
      "finished": "2019-06-12T11:21:48.993385Z",
      "elapsed": 21.483,
      "job_args": "[\"ansible-playbook\", \"-u\", \"root\", \"-i\", \"/tmp/awx_223_ft8hu4p4/tmptmtwllu4\", \"-e\", \"@/tmp/awx_223_ft8hu4p4/env/extravars\", \"hello_world.yml\"]",
      "job_cwd": "/var/lib/awx/projects/cds_playbooks_folder",
      "job_env": {
        "HOSTNAME": "awx",
        "LC_ALL": "en_US.UTF-8",
        "VIRTUAL_ENV": "/var/lib/awx/venv/ansible",
        "PATH": "/var/lib/awx/venv/ansible/bin:/var/lib/awx/venv/awx/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
        "SUPERVISOR_GROUP_NAME": "tower-processes",
        "PWD": "/var/lib/awx",
        "LANG": "en_US.UTF-8",
        "PS1": "(awx) ",
        "SUPERVISOR_ENABLED": "1",
        "HOME": "/var/lib/awx",
        "SHLVL": "2",
        "LANGUAGE": "en_US.UTF-8",
        "LC_CTYPE": "en_US.UTF-8",
        "SUPERVISOR_PROCESS_NAME": "dispatcher",
        "SUPERVISOR_SERVER_URL": "unix:///tmp/supervisor.sock",
        "DJANGO_SETTINGS_MODULE": "awx.settings.production",
        "DJANGO_LIVE_TEST_SERVER_ADDRESS": "localhost:9013-9199",
        "TZ": "UTC",
        "ANSIBLE_FORCE_COLOR": "True",
        "ANSIBLE_HOST_KEY_CHECKING": "False",
        "ANSIBLE_INVENTORY_UNPARSED_FAILED": "True",
        "ANSIBLE_PARAMIKO_RECORD_HOST_KEYS": "False",
        "ANSIBLE_VENV_PATH": "/var/lib/awx/venv/ansible",
        "AWX_PRIVATE_DATA_DIR": "/tmp/awx_223_ft8hu4p4",
        "PYTHONPATH": "/var/lib/awx/venv/ansible/lib/python2.7/site-packages:/var/lib/awx/venv/awx/lib64/python3.6/site-packages/awx/lib:",
        "JOB_ID": "$jobId",
        "INVENTORY_ID": "1",
        "PROJECT_REVISION": "",
        "ANSIBLE_RETRY_FILES_ENABLED": "False",
        "MAX_EVENT_RES": "700000",
        "ANSIBLE_CALLBACK_PLUGINS": "/var/lib/awx/venv/awx/lib64/python3.6/site-packages/awx/plugins/callback",
        "AWX_HOST": "https://towerhost",
        "ANSIBLE_SSH_CONTROL_PATH_DIR": "/tmp/awx_223_ft8hu4p4/cp",
        "ANSIBLE_STDOUT_CALLBACK": "awx_display",
        "AWX_ISOLATED_DATA_DIR": "/tmp/awx_223_ft8hu4p4/artifacts/$jobId"
      },
      "job_explanation": "",
      "execution_node": "awx",
      "controller_node": "",
      "result_traceback": "",
      "event_processing_finished": true,
      "job_template": $jtId,
      "passwords_needed_to_start": [],
      "ask_diff_mode_on_launch": false,
      "ask_variables_on_launch": true,
      "ask_limit_on_launch": true,
      "ask_tags_on_launch": true,
      "ask_skip_tags_on_launch": true,
      "ask_job_type_on_launch": false,
      "ask_verbosity_on_launch": false,
      "ask_inventory_on_launch": true,
      "ask_credential_on_launch": true,
      "allow_simultaneous": false,
      "artifacts": {},
      "scm_revision": "",
      "instance_group": 1,
      "diff_mode": false,
      "job_slice_number": 0,
      "job_slice_count": 1,
      "host_status_counts": {
        "ok": 1
      },
      "playbook_counts": {
        "play_count": 1,
        "task_count": 2
      },
      "custom_virtualenv": "/var/lib/awx/venv/ansible",
      "credential": null,
      "vault_credential": null
    }"""

    private fun getReport() = """

PLAY [Hello World Sample] ******************************************************

TASK [Gathering Facts] *********************************************************
ok: [localhost]

TASK [Hello Message] ***********************************************************
ok: [localhost] => {
    "msg": "Hello World!"
}

PLAY RECAP *********************************************************************
localhost                  : ok=2    changed=0    unreachable=0    failed=0

"""
}
