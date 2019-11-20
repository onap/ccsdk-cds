/*
 *  Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.io.Serializable
import java.util.Date
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ResourceDictionary
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment

/**
 * ArtifactRequest.java Purpose: Provide Configuration Generator ArtifactRequest Model
 *
 * @author Brinda Santh
 * @version 1.0
 */
class AutoMapResponse {

    var resourceAssignments: List<ResourceAssignment>? = null
    var dataDictionaries: List<ResourceDictionary>? = null
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName("errorMessage")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
class ErrorMessage(var message: String?, var code: Int?, var debugMessage: String?) : Serializable {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    var timestamp = Date()
}
