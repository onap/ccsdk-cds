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

package org.onap.ccsdk.apps.controllerblueprints.core.interfaces

interface BluePrintCatalogService {

    /**
     * Upload the CBA Zip fle to data base and return the Database identifier
     */
    fun uploadToDataBase(file: String): String

    /**
     * Download the CBA zip file from the data base and place it in a path and return the CBA zip absolute path
     */
    fun downloadFromDataBase(name: String, version: String, path: String): String

    /**
     * Get the Blueprint from Data Base and Download it under working directory and return the path path
     */
    fun prepareBluePrint(name: String, version: String): String
}