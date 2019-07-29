/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.cds.blueprintsprocessor

import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import org.mockito.ArgumentMatcher
import org.springframework.test.util.JsonExpectationsHelper
import org.springframework.test.util.XmlExpectationsHelper
import kotlin.test.Test

@Suppress("MemberVisibilityCanBePrivate")
class FiveGConfigBlueprintTest : AbstractBlueprintTest() {

    @Test
    fun `should upload BP and process config-assign and config-deploy`() {

        // Must match corresponding variables defined on module RestconfConfigDeploy.py
        val configletResourcePath = "yang-ext:mount/mynetconf:netconflist"
        val restconfServerIdentifier = "sdncodl"

        val nodeIdentifier = "network-topology:network-topology/topology/topology-netconf/node/$pnfId"
        val configUri = "restconf/config/$nodeIdentifier"
        val operationalUri = "restconf/operational/$nodeIdentifier"

        val restClient = createRestClientMock(restconfServerIdentifier,
                RestExpectation("PUT", configUri, 201),
                RestExpectation("GET", operationalUri, 200, odlConnectedResponse),
                RestExpectation("GET", "$configUri/$configletResourcePath", 200, "{}"),
                RestExpectation("PATCH", "$configUri/$configletResourcePath", 200),
                RestExpectation("DELETE", configUri, 200)
        )

        uploadBlueprint("5g_config")

        processBlueprint(assignRequest, expectedAssignResponse)

        processBlueprint(deployRequest, expectedDeployResponse)

        verify(restClient).exchangeResource(
                eq("PUT"),
                eq(configUri),
                argThat(ArgumentMatcher { requestBody ->
                    xmlExpectationsHelper.assertXmlEqual(expectedMountXmlRequest, requestBody)
                    true
                }),
                eq(mapOf("Content-Type" to "application/xml")))

        verify(restClient).exchangeResource(
                eq("PATCH"),
                eq("$configUri/$configletResourcePath"),
                argThat(ArgumentMatcher { requestBody ->
                    jsonExpectationsHelper.assertJsonEqual(configAssignPatch, requestBody)
                    true
                }),
                eq(mapOf("Content-Type" to "application/yang.patch+json")))
    }

    companion object {
        const val resKey = "61"
        const val pnfId = "pnf-id-2019-07-12"
        const val pnfAddress = "192.168.100.11"

        val xmlExpectationsHelper = XmlExpectationsHelper()
        val jsonExpectationsHelper = JsonExpectationsHelper()

        const val odlConnectedResponse = """{
    "node": [{
            "netconf-node-topology:connection-status":"connected"
        }]
} """

        // Should match the expanded contents of the ".../config-deploy-restconf-mount-template.vtl" file
        const val expectedMountXmlRequest = """
<node xmlns="urn:TBD:params:xml:ns:yang:network-topology">
    <node-id>$pnfId</node-id>
    <key-based xmlns="urn:opendaylight:netconf-node-topology">
        <key-id xmlns="urn:opendaylight:netconf-node-topology">ODL_private_key_0</key-id>
        <username xmlns="urn:opendaylight:netconf-node-topology">netconf</username>
     </key-based>
    <host xmlns="urn:opendaylight:netconf-node-topology">$pnfAddress</host>
    <port xmlns="urn:opendaylight:netconf-node-topology">6513</port>
    <tcp-only xmlns="urn:opendaylight:netconf-node-topology">false</tcp-only>
    <protocol xmlns="urn:opendaylight:netconf-node-topology">
        <name xmlns="urn:opendaylight:netconf-node-topology">TLS</name>
    </protocol>
    <max-connection-attempts xmlns="urn:opendaylight:netconf-node-topology">5</max-connection-attempts>
</node>"""
    }

    val commonHeader = """
  "commonHeader": {
    "originatorId": "sdnc",
    "requestId": "123456-1000",
    "subRequestId": "sub-123456-1000"
  }"""

    val assignActionIdentifiers = """
  "actionIdentifiers": {
    "actionName": "config-assign",
    "blueprintName": "configuration_over_restconf",
    "blueprintVersion": "1.0.0",
    "mode": "sync"
  }"""

    val assignRequest = """{
  $assignActionIdentifiers,
  $commonHeader,
  "payload": {
    "config-assign-request": {
      "resolution-key": "RES-KEY $resKey",
      "config-assign-properties": {
        "service-instance-id": "siid_1234",
        "pnf-id": "$pnfId",
        "pnf-ipv4-address": "$pnfAddress",
        "service-model-uuid": "service-model-uuid",
        "pnf-customization-uuid": "pnf-customization-uuid"
      }
    }
  }
}"""

    val configAssignPatch = """{
          "ietf-restconf:yang-patch": {
            "patch-id": "patch-1",
            "edit": [
              {
                "edit-id": "edit1",
                "operation": "merge",
                "target": "/",
                "value": { "netconflist": { "netconf": [ { "netconf-id": "10", "netconf-param": "1000" } ] } }
              },
              {
                "edit-id": "edit2",
                "operation": "merge",
                "target": "/",
                "value": { "netconflist": { "netconf": [ { "netconf-id": "20", "netconf-param": "2000" } ] } }
              },
              {
                "edit-id": "edit3",
                "operation": "merge",
                "target": "/",
                "value": { "netconflist": { "netconf": [ { "netconf-id": "30", "netconf-param": "3000" } ] } }
              }
            ]
          }
        }"""

    val expectedAssignResponse = """{
  $commonHeader,
  $assignActionIdentifiers,
  "status": {
    "code": 200,
    "eventType": "EVENT_COMPONENT_EXECUTED",
    "errorMessage": null,
    "message": "success"
  },
  "payload": {
    "config-assign-response": {}
  },
  "stepData": {
    "name": "config-assign",
    "properties": {
      "resource-assignment-params": {
        "config-assign": $configAssignPatch
      },
      "status": "success"
    }
  }
}"""

    val deployActionIdentifiers = """
  "actionIdentifiers": {
    "actionName": "config-deploy",
    "blueprintName": "configuration_over_restconf",
    "blueprintVersion": "1.0.0",
    "mode": "sync"
  }"""

    val deployRequest = """{
  $deployActionIdentifiers,
  $commonHeader,
  "payload": {
    "config-deploy-request": {
      "resolution-key": "RES-KEY $resKey",
      "config-deploy-properties": {
        "service-instance-id": "siid_1234",
        "pnf-id": "$pnfId",
        "pnf-ipv4-address": "$pnfAddress",
        "service-model-uuid": "service-model-uuid",
        "pnf-customization-uuid": "pnf-customization-uuid"
      }
    }
  }
}"""

    val expectedDeployResponse = """{
  $deployActionIdentifiers,
  $commonHeader,
  "payload": {
    "config-deploy-response": {}
  },
  "status": {
    "code": 200,
    "errorMessage": null,
    "eventType": "EVENT_COMPONENT_EXECUTED",
    "message": "success"
  },
  "stepData": {
    "name": "config-deploy",
    "properties": {
      "response-data": "",
      "status": "success"
    }
  }
}"""
}
