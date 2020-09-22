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
package org.onap.ccsdk.cds.blueprintsprocessor.uat.utils

import com.github.tomakehurst.wiremock.common.Notifier
import org.slf4j.LoggerFactory
import org.slf4j.Marker

class MarkedSlf4jNotifier(private val marker: Marker) : Notifier {

    override fun info(message: String) {
        log.info(marker, message)
    }

    override fun error(message: String) {
        log.error(marker, message)
    }

    override fun error(message: String, t: Throwable) {
        log.error(marker, message, t)
    }

    companion object {

        private val log = LoggerFactory.getLogger("uat.WireMock")
    }
}
