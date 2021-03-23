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

package org.onap.ccsdk.cds.controllerblueprints.core

class BlueprintError {

    private val errors: MutableMap<String, MutableList<String>> = mutableMapOf()

    fun addError(type: String, name: String, error: String, stepName: String) {
        addError("$type : $name : $error", stepName)
    }

    fun addError(error: String, stepName: String) {
        errors.getOrPut(stepName, { mutableListOf() }).add(error)
    }

    fun addErrors(stepName: String, errorList: List<String>) {
        errors.getOrPut(stepName, { mutableListOf() }).addAll(errorList)
    }

    fun allErrors(): List<String> = errors.values.flatten()

    fun stepErrors(stepName: String): MutableList<String>? = errors[stepName]
}
