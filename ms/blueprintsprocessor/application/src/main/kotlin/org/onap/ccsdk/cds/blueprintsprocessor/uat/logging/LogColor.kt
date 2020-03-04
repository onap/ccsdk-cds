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
package org.onap.ccsdk.cds.blueprintsprocessor.uat.logging

import org.slf4j.MDC
import org.slf4j.MarkerFactory

object LogColor {

    const val COLOR_SERVICES = "green"
    const val COLOR_TEST_CLIENT = "yellow"
    const val COLOR_MOCKITO = "cyan"
    const val COLOR_WIREMOCK = "blue"

    // The Slf4j MDC key that will hold the global color
    const val MDC_COLOR_KEY = "color"

    fun setContextColor(color: String) {
        MDC.put(MDC_COLOR_KEY, color)
    }

    fun resetContextColor() {
        MDC.remove(MDC_COLOR_KEY)
    }

    fun markerOf(color: String): ColorMarker =
        ColorMarker(MarkerFactory.getMarker(color))
}
