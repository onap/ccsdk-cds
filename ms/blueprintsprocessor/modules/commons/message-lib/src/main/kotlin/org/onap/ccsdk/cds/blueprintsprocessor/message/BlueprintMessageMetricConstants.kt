/*
 * Copyright © 2021 Bell Canada.
 *
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
 */

package org.onap.ccsdk.cds.blueprintsprocessor.message

object BlueprintMessageMetricConstants {

    private const val METRIC_PREFIX = "cds.kafka"

    private const val PRODUCED_MESSAGES_PREFIX = "$METRIC_PREFIX.produced.messages"
    private const val CONSUMED_MESSAGES_PREFIX = "$METRIC_PREFIX.consumed.messages"

    // COUNTERS
    const val KAFKA_PRODUCED_MESSAGES_COUNTER = "$PRODUCED_MESSAGES_PREFIX.total"
    const val KAFKA_PRODUCED_MESSAGES_ERROR_COUNTER = "$PRODUCED_MESSAGES_PREFIX.error"

    const val KAFKA_CONSUMED_MESSAGES_COUNTER = "$CONSUMED_MESSAGES_PREFIX.total"
    const val KAFKA_CONSUMED_MESSAGES_ERROR_COUNTER = "$CONSUMED_MESSAGES_PREFIX.error"
}
