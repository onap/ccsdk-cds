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

package org.onap.ccsdk.cds.controllerblueprints.core.dsl

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.cds.controllerblueprints.core.data.AttributeDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.Implementation
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.Step

/**
 * This is simplified version of DSL, which is used for generating the Service template
 * @author Brinda Santh
 */

class DSLBluePrint {

    var metadata: MutableMap<String, String> = hashMapOf()
    var properties: MutableMap<String, JsonNode>? = null
    var dataTypes: MutableMap<String, DataType> = hashMapOf()
    var artifactTypes: MutableMap<String, ArtifactType> = hashMapOf()
    var components: MutableMap<String, DSLComponent> = hashMapOf()
    var registryComponents: MutableMap<String, DSLRegistryComponent> = hashMapOf()
    var workflows: MutableMap<String, DSLWorkflow> = hashMapOf()
}

class DSLWorkflow {

    @get:JsonIgnore
    var id: String? = null
    lateinit var description: String
    lateinit var actionName: String
    lateinit var steps: MutableMap<String, Step>
    var inputs: MutableMap<String, PropertyDefinition>? = null
    var outputs: MutableMap<String, PropertyDefinition>? = null
}

class DSLComponent {

    @get:JsonIgnore
    lateinit var id: String
    lateinit var type: String
    lateinit var version: String
    lateinit var description: String
    var implementation: Implementation? = null
    var attributes: MutableMap<String, AttributeDefinition>? = null
    var properties: MutableMap<String, PropertyDefinition>? = null
    var artifacts: MutableMap<String, ArtifactDefinition>? = null
    var inputs: MutableMap<String, PropertyDefinition>? = null
    var outputs: MutableMap<String, PropertyDefinition>? = null
}

class DSLRegistryComponent {

    lateinit var id: String
    lateinit var type: String
    lateinit var version: String
    lateinit var interfaceName: String
    lateinit var description: String
    var implementation: Implementation? = null
    var properties: MutableMap<String, JsonNode>? = null
    var artifacts: MutableMap<String, ArtifactDefinition>? = null
    var inputs: MutableMap<String, JsonNode>? = null
    var outputs: MutableMap<String, JsonNode>? = null
}
