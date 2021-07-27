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
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import kotlin.test.assertEquals

class BlueprintMessageUtilsTest {

    @Test
    fun testKafkaMetricTag() {
        val expected = mutableListOf<Tag>(
            Tag.of(BluePrintConstants.METRIC_TAG_TOPIC, "my-topic")
        )
        val tags = BlueprintMessageUtils.kafkaMetricTag("my-topic")

        assertEquals(expected, tags)
    }
}
