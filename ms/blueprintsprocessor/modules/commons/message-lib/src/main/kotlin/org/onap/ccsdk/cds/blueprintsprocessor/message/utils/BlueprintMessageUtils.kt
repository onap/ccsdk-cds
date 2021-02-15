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

package org.onap.ccsdk.cds.blueprintsprocessor.message.utils

import io.micrometer.core.instrument.Tag
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.defaultToUUID
import kotlin.math.max

class BlueprintMessageUtils {
    companion object {
        fun kafkaMetricTag(topic: String): MutableList<Tag> =
            mutableListOf(
                Tag.of(BlueprintConstants.METRIC_TAG_TOPIC, topic)
            )

        /**
         * get OS hostname's last 5 characters
         * Used to generate unique client ID.
         */
        fun getHostnameSuffix(): String =
            System.getenv("HOSTNAME").defaultToUUID().let {
                it.substring(max(0, it.length - 5))
            }

        fun getMessageLogData(message: Any): String =
            when (message) {
                is ExecutionServiceInput -> {
                    val actionIdentifiers = message.actionIdentifiers
                    "CBA(${actionIdentifiers.blueprintName}/${actionIdentifiers.blueprintVersion}/${actionIdentifiers.actionName})"
                }
                is ExecutionServiceOutput -> {
                    val actionIdentifiers = message.actionIdentifiers
                    "CBA(${actionIdentifiers.blueprintName}/${actionIdentifiers.blueprintVersion}/${actionIdentifiers.actionName})"
                }
                else -> "message($message)"
            }
    }
}
