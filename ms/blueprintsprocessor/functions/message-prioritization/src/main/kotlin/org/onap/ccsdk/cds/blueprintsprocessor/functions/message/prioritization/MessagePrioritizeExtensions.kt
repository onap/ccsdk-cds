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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization

import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintDependencyService

/**
 * Register the MessagePrioritizationStateService and exposed dependency
 */
fun BlueprintDependencyService.messagePrioritizationStateService(): MessagePrioritizationStateService =
    instance(MessagePrioritizationStateService::class)

/**
 * Expose messagePrioritizationStateService to AbstractComponentFunction
 */
fun AbstractComponentFunction.messagePrioritizationStateService() =
    BlueprintDependencyService.messagePrioritizationStateService()

/**
 * MessagePrioritization correlation extensions
 */

/**
 * Arrange comma separated correlation keys in ascending order.
 */
fun MessagePrioritization.toFormatedCorrelation(): String {
    return this.correlationId!!.split(",")
        .map { it.trim() }.sorted().joinToString(",")
}

/**
 * Used to group the correlation with respect to types.
 */
fun MessagePrioritization.toTypeNCorrelation(): TypeCorrelationKey {
    return TypeCorrelationKey(this.type, this.toFormatedCorrelation())
}

/** get list of message ids **/
fun List<MessagePrioritization>.ids(): List<String> {
    return this.map { it.id }
}

/** Ordered by highest priority and updated date **/
fun List<MessagePrioritization>.orderByHighestPriority(): List<MessagePrioritization> {
    return this.sortedWith(compareBy(MessagePrioritization::priority, MessagePrioritization::updatedDate))
}

/** Ordered by Updated date **/
fun List<MessagePrioritization>.orderByUpdatedDate(): List<MessagePrioritization> {
    return this.sortedWith(compareBy(MessagePrioritization::updatedDate))
}
