/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.core

/**
 *
 *
 * @author Brinda Santh
 */
class BluePrintProcessorException : RuntimeException {

    var code: Int = 100

    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(cause: Throwable, message: String, vararg args: Any?) : super(format(message, *args), cause)

    constructor(code: Int, cause: Throwable) : super(cause) {
        this.code = code
    }

    constructor(code: Int, message: String) : super(message) {
        this.code = code
    }

    constructor(code: Int, message: String, cause: Throwable) : super(message, cause) {
        this.code = code
    }

    constructor(code: Int, cause: Throwable, message: String, vararg args: Any?) :
            super(String.format(message, *args), cause) {
        this.code = code
    }
}

class BluePrintRetryException : RuntimeException {
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(cause: Throwable, message: String, vararg args: Any?) : super(format(message, *args), cause)
}
