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

package org.onap.ccsdk.cds.blueprintsprocessor.message

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeader
import java.nio.charset.Charset

fun <T : Headers> T?.toMap(): MutableMap<String, String> {
    val map: MutableMap<String, String> = hashMapOf()
    this?.forEach { map[it.key()] = String(it.value(), Charset.defaultCharset()) }
    return map
}

fun Headers.addHeader(key: String, value: String) {
    this.add(RecordHeader(key, value.toByteArray()))
}
