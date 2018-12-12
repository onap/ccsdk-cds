/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *  Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.apps.controllerblueprints.resource.dict.service

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRepoFileService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRepoService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition

/**
 * ResourceDefinitionRepoService.
 *
 * @author Brinda Santh
 */
interface ResourceDefinitionRepoService : BluePrintRepoService {

    @Throws(BluePrintException::class)
    fun getResourceDefinition(resourceDefinitionName: String): ResourceDefinition
}

/**
 * ResourceDefinitionFileRepoService.
 *
 * @author Brinda Santh
 */
open class ResourceDefinitionFileRepoService : BluePrintRepoFileService,
        ResourceDefinitionRepoService {

    private var resourceDefinitionPath: String
    private val extension = ".json"

    constructor(basePath: String) : this(basePath,
            basePath.plus(BluePrintConstants.PATH_DIVIDER)
                    .plus(BluePrintConstants.MODEL_DIR_MODEL_TYPE)
                    .plus(BluePrintConstants.PATH_DIVIDER)
                    .plus("starter-type"))

    constructor(basePath: String, modelTypePath: String) : super(modelTypePath) {
        resourceDefinitionPath = basePath.plus("/resource-dictionary/starter-dictionary")
    }

    override fun getResourceDefinition(resourceDefinitionName: String): ResourceDefinition {

        val fileName = resourceDefinitionPath.plus(BluePrintConstants.PATH_DIVIDER)
                .plus(resourceDefinitionName).plus(extension)

        return JacksonUtils.readValueFromFile(fileName, ResourceDefinition::class.java)
                ?: throw BluePrintException("couldn't get resource definition for file($fileName)")
    }
}
