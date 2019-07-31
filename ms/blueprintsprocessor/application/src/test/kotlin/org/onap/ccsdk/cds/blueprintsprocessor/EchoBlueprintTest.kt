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

import kotlin.test.Test

class EchoBlueprintTest : AbstractBlueprintTest() {

    @Test
    fun `should upload and process echo blueprint`() {

        uploadBlueprint("echo")

        val commonHeader = """
  "commonHeader": {
    "originatorId": "sdnc",
    "requestId": "1234",
    "subRequestId": "1234-12234"
  }"""

        val actionIdentifiers = """
  "actionIdentifiers": {
    "blueprintName": "echo_test",
    "blueprintVersion": "1.0.0",
    "actionName": "echo",
    "mode": "sync"
  }"""

        val theMessage = "Hello World!"

        val request = """{
  $commonHeader,
  $actionIdentifiers,
  "payload": {
    "echo-request": {
      "echo-properties": {
        "echoed-message": "$theMessage"
      }
    }
  }
}"""

        val expectedResponse = """{
  $commonHeader,
  $actionIdentifiers,
  "status": {
    "code": 200,
    "eventType": "EVENT_COMPONENT_EXECUTED",
    "errorMessage": null,
    "message": "success"
  },
  "payload": {
    "echo-response": {}
  },
  "stepData": {
    "name": "echo",
    "properties": {
      "resource-assignment-params": {
        "echo": "$theMessage"
      },
      "status": "success"
    }
  }
}"""

        processBlueprint(request, expectedResponse)
    }
}