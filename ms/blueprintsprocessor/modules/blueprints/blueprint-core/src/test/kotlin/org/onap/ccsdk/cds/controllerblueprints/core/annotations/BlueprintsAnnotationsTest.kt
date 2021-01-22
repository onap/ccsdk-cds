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

package org.onap.ccsdk.cds.controllerblueprints.core.annotations

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.asBlueprintsDataTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asPropertyDefinitionMap
import kotlin.test.Test
import kotlin.test.assertNotNull

class BlueprintsAnnotationsTest {

    @Test
    fun testBlueprintWorkflowData() {
        val wfInput = TestBlueprintsWorkflowInput::class.asPropertyDefinitionMap()
        // println(wfInput.asJsonString(true))
        assertNotNull(wfInput, "failed to generate wfInput property map")

        val wfOutput = TestBlueprintsWorkflowOutput::class.asPropertyDefinitionMap()
        // println(wfOutput.asJsonString(true))
        assertNotNull(wfInput, "failed to generate wfOutput property map")
    }

    @Test
    fun testBlueprintDataType() {
        val dataTypes = TestBlueprintsDataType::class.asBlueprintsDataTypes()
        // println(dataTypes.asJsonString(true))
        assertNotNull(dataTypes, "failed to generate dataTypes definition")
    }
}

@BlueprintsDataType(
    name = "dt-test-datatype", description = "I am test",
    version = "1.0.0", derivedFrom = "tosca.datatypes.root"
)
data class TestBlueprintsDataType(
    @BlueprintsProperty(description = "this stringData")
    var stringData: String,
    @BlueprintsProperty(description = "this stringDataWithValue")
    @PropertyDefaultValue(value = "USA")
    val stringDataWithValue: String,
    @BlueprintsProperty(description = "this intDataWithValue")
    @PropertyDefaultValue(value = "30")
    val intDataWithValue: Int,
    @BlueprintsProperty(description = "this booleanDataWithValue")
    @PropertyDefaultValue(value = "true")
    val booleanDataWithValue: Boolean,
    @BlueprintsProperty(description = "this anyData")
    val anyData: Any,
    @BlueprintsProperty(description = "this jsonDataWithValue")
    @PropertyDefaultValue(value = """{"data" : "1234"}""")
    val jsonDataWithValue: JsonNode?,
    @BlueprintsProperty(description = "listData")
    val listData: MutableList<String>,
    @BlueprintsProperty(description = "this mapData")
    val mapData: MutableMap<String, String> = hashMapOf(),
    @BlueprintsProperty(description = "this complexData")
    val complexData: TestBlueprintsChildDataType?,
    @BlueprintsProperty(description = "this complexDataList")
    val complexDataList: MutableList<TestBlueprintsChildDataType>
)

data class TestBlueprintsChildDataType(val name: String)

@BlueprintsWorkflowInput
data class TestBlueprintsWorkflowInput(
    @BlueprintsProperty(description = "this sample name")
    @PropertyDefaultValue(value = "Brinda")
    var name: String,
    @BlueprintsProperty(description = "this sample name")
    val place: String
)

@BlueprintsWorkflowOutput
data class TestBlueprintsWorkflowOutput(
    @BlueprintsProperty(description = "this is dslExpression")
    @DSLExpression("field1")
    var dslExpression: String,

    @BlueprintsProperty(description = "this is withNodeAttributeExpression")
    @AttributeExpression(modelableEntityName = "sample-node", attributeName = "response-data")
    var withNodeAttributeExpression: String,

    @BlueprintsProperty(description = "this is withNodeAttributeExpressionSubAttribute")
    @AttributeExpression(
        modelableEntityName = "sample-node", attributeName = "response-data",
        subAttributeName = ".\$field1"
    )
    var withNodeAttributeExpressionSubAttribute: String,

    @BlueprintsProperty(description = "this is withAttributeExpressionSubAttribute")
    @AttributeExpression(attributeName = "response-data", subAttributeName = ".\$field1")
    var withAttributeExpressionSubAttribute: String,

    @BlueprintsProperty(description = "this is withAttributeExpression")
    @AttributeExpression(attributeName = "response-data")
    var withAttributeExpression: String,

    @BlueprintsProperty(description = "this is withAArtifactExpression")
    @ArtifactExpression(modelableEntityName = "test-node", artifactName = "content-template")
    var withAArtifactExpression: String,

    @BlueprintsProperty(description = "this status")
    val status: String = "success"
)
