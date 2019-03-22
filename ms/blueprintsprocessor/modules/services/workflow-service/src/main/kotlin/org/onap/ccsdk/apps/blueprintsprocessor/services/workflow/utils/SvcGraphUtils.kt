/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.blueprintsprocessor.services.workflow.utils

import org.onap.ccsdk.sli.core.sli.SvcLogicGraph
import org.onap.ccsdk.sli.core.sli.SvcLogicParser

object SvcGraphUtils {

    @JvmStatic
    fun getSvcGraphFromClassPathFile(fileName: String): SvcLogicGraph {
        val url = SvcGraphUtils::class.java.classLoader.getResource(fileName)
        return getSvcGraphFromFile(url.path)
    }

    @JvmStatic
    fun getSvcGraphFromFile(fileName: String): SvcLogicGraph {
        val svcLogicParser = SvcLogicParser()
        return svcLogicParser.parse(fileName).first
    }
}