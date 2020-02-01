/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.api

import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.MessagePrioritizationStateService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.UpdateStateRequest
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.mdcWebCoroutineScope
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/v1/message-prioritization"])
open class MessagePrioritizationApi(
    private val messagePrioritizationStateService: MessagePrioritizationStateService,
    private val messagePrioritizationService: MessagePrioritizationService
) {

    @GetMapping(path = ["/ping"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    suspend fun ping(): String = mdcWebCoroutineScope { "Success" }

    @GetMapping(path = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    suspend fun messagePrioritization(@PathVariable(value = "id") id: String) = mdcWebCoroutineScope {
        messagePrioritizationStateService.getMessage(id)
    }

    @PostMapping(
        path = ["/"], produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    suspend fun saveMessagePrioritization(@RequestBody messagePrioritization: MessagePrioritization) =
        mdcWebCoroutineScope {
            messagePrioritizationStateService.saveMessage(messagePrioritization)
        }

    @PostMapping(
        path = ["/prioritize"], produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    suspend fun prioritize(@RequestBody messagePrioritization: MessagePrioritization) = mdcWebCoroutineScope {
        messagePrioritizationService.prioritize(messagePrioritization)
    }

    @PostMapping(
        path = ["/update-state"], produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    suspend fun updateMessagePrioritizationState(@RequestBody updateMessageState: UpdateStateRequest) =
        mdcWebCoroutineScope {
            messagePrioritizationStateService.setMessageState(
                updateMessageState.id,
                updateMessageState.state!!
            )
        }
}
