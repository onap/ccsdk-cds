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

import com.google.common.collect.Maps
import org.mockito.ArgumentMatcher

class RequiredMapEntriesMatcher<K, V>(private val requiredEntries: Map<K, V>) : ArgumentMatcher<Map<K, V>> {

    override fun matches(argument: Map<K, V>?): Boolean {
        val missingEntries = Maps.difference(requiredEntries, argument).entriesOnlyOnLeft()
        return missingEntries.isEmpty()
    }

    override fun toString(): String {
        return requiredEntries.toString()
    }
}
