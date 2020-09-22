/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2018-2019 AT&T Intellectual Property.
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

import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ResourceDictionary
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants.DATA_TYPE_JSON
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants.DEFAULT_VERSION_NUMBER
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants.TOSCA_SPEC
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment

class BootstrapRequest {

    var loadModelType: Boolean = false
    var loadResourceDictionary: Boolean = false
    var loadCBA: Boolean = false
}

class WorkFlowsResponse {

    lateinit var blueprintName: String
    var version: String = DEFAULT_VERSION_NUMBER
    var workflows: MutableSet<String> = mutableSetOf()
}

class WorkFlowSpecRequest {

    lateinit var blueprintName: String
    var version: String = DEFAULT_VERSION_NUMBER
    var returnContent: String = DATA_TYPE_JSON
    lateinit var workflowName: String
    var specType: String = TOSCA_SPEC
}

class WorkFlowSpecResponse {

    lateinit var blueprintName: String
    var version: String = DEFAULT_VERSION_NUMBER
    lateinit var workFlowData: WorkFlowData
    var dataTypes: MutableMap<String, DataType>? = mutableMapOf()
}

class WorkFlowData {

    lateinit var workFlowName: String
    var inputs: MutableMap<String, PropertyDefinition>? = null
    var outputs: MutableMap<String, PropertyDefinition>? = null
}

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
