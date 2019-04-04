/*
 *  Copyright © 2018 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.resource.dict

/**
 * ResourceDictionaryConstants
 *
 * @author Brinda Santh
 */
object ResourceDictionaryConstants {
    const val SOURCE_INPUT = "input"
    const val SOURCE_DEFAULT = "default"
    //const val SOURCE_PRIMARY_CONFIG_DATA = "rest"
    const val SOURCE_PROCESSOR_DB = "processor-db"
    const val SOURCE_PRIMARY_CONFIG_DATA = "primary-config-data"
    const val SOURCE_PRIMARY_DB = "primary-db"
    const val SOURCE_PRIMARY_AAI_DATA = "primary-aai-data"

    const val MODEL_DIR_RESOURCE_DEFINITION: String = "resource_dictionary"

    const val PROPERTY_TYPE = "type"
    const val PROPERTY_INPUT_KEY_MAPPING = "input-key-mapping"
    const val PROPERTY_OUTPUT_KEY_MAPPING = "output-key-mapping"
    const val PROPERTY_KEY_DEPENDENCIES = "key-dependencies"

    const val PATH_RESOURCE_DEFINITION_TYPE = "resources_definition_types"
}