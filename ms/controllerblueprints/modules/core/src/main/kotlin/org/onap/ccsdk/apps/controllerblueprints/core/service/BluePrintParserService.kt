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

package org.onap.ccsdk.apps.controllerblueprints.core.service

import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.apps.controllerblueprints.core.utils.ServiceTemplateUtils
import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import java.io.File
import java.io.Serializable

/**
 *
 *
 * @author Brinda Santh
 */
interface BluePrintParserService : Serializable {
    fun readBlueprint(content: String) : BluePrintContext
    fun readBlueprintFile(fileName: String) : BluePrintContext
    /**
     * Read Blueprint from CSAR structure Directory
     */
    fun readBlueprintFile(fileName: String, basePath : String) : BluePrintContext
}

class BluePrintParserDefaultService : BluePrintParserService {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(this::class.toString())

    var basePath : String = javaClass.classLoader.getResource(".").path

    override fun readBlueprint(content: String): BluePrintContext {
        return BluePrintContext(ServiceTemplateUtils.getServiceTemplateFromContent(content))
    }

    override fun readBlueprintFile(fileName: String): BluePrintContext {
        return readBlueprintFile(fileName, basePath )
    }

    override fun readBlueprintFile(fileName: String, basePath : String): BluePrintContext {
        val rootFilePath: String = StringBuilder().append(basePath).append(File.separator).append(fileName).toString()
        val rootServiceTemplate : ServiceTemplate = ServiceTemplateUtils.getServiceTemplate(rootFilePath)
        // TODO("Nested Lookup Implementation based on Import files")
        return BluePrintContext(rootServiceTemplate)
    }


}
