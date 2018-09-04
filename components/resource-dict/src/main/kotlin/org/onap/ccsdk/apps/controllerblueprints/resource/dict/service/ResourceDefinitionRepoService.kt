/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
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
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRepoFileService
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRepoService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonReactorUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition
import reactor.core.publisher.Mono
/**
 * ResourceDefinitionRepoService.
 *
 * @author Brinda Santh
 */
interface ResourceDefinitionRepoService : BluePrintRepoService {

    fun getResourceDefinition(resourceDefinitionName: String): Mono<ResourceDefinition>?
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
                    .plus(BluePrintConstants.MODEL_DIR_MODEL_TYPE))

    constructor(basePath: String, modelTypePath: String) : super(modelTypePath) {
        resourceDefinitionPath = basePath.plus("/resource_dictionary")
    }

    override fun getResourceDefinition(resourceDefinitionName: String): Mono<ResourceDefinition>? {

        val fileName = resourceDefinitionPath.plus(BluePrintConstants.PATH_DIVIDER)
                .plus(resourceDefinitionName).plus(extension)

        return JacksonReactorUtils.readValueFromFile(fileName, ResourceDefinition::class.java)
    }
}
