/*
 *  Copyright © 2019 IBM, Bell Canada.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.error.catalog.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment

abstract class ErrorMessagesLibService {

    /**
     * Environment entity to derive it from the system to load a specific
     * property file.
     */
    @Autowired
    lateinit var env: Environment

    abstract fun getErrorMessage(domain: String, key: String): String?

    abstract fun getErrorMessage(attribute: String): String?
}
