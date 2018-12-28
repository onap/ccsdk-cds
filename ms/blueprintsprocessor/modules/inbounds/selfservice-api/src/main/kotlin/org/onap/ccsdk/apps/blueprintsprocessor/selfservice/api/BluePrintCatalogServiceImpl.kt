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

package org.onap.ccsdk.apps.blueprintsprocessor.selfservice.api

import org.onap.ccsdk.apps.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.springframework.stereotype.Service
import java.io.File

@Service
class BluePrintCatalogServiceImpl(private val bluePrintCoreConfiguration: BluePrintCoreConfiguration) : BluePrintCatalogService {

    override fun uploadToDataBase(file: String, validate : Boolean): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun downloadFromDataBase(name: String, version: String, path: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun prepareBluePrint(name: String, version: String): String {
        //TODO("Get the Blueprint from the DB")
        return bluePrintCoreConfiguration.deployPath.plus(File.separator)
                .plus(name).plus(File.separator).plus(version)

    }

    override fun downloadFromDataBase(uuid: String, path: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}