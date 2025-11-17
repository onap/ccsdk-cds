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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain

import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * @author Brinda Santh
 */
@Converter
class JpaResourceDefinitionConverter : AttributeConverter<ResourceDefinition, String> {

    override fun convertToDatabaseColumn(resourceDefinition: ResourceDefinition): String {
        return JacksonUtils.getJson(resourceDefinition)
    }

    override fun convertToEntityAttribute(content: String): ResourceDefinition? {
        return JacksonUtils.readValue(content, ResourceDefinition::class.java)
    }
}
