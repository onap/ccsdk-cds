/*
 * Copyright © 2021 Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution

object ComponentLockMetricConstants {
    private const val METRICS_PREFIX = "cds.lock"

    private const val METRICS_COMPONENT_PREFIX = "$METRICS_PREFIX.component"

    // COUNTERS
    const val LOCK_COUNTER_COMPONENT = "$METRICS_COMPONENT_PREFIX.counter"

    // TIMERS
    const val LOCK_TIMER_COMPONENT = "$METRICS_COMPONENT_PREFIX.timer"

    const val LOCK_NOT_ACQUIRED_COUNTER_COMPONENT = "$METRICS_COMPONENT_PREFIX.notAcquiredCounter"
}
