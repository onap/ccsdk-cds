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
import org.onap.ccsdk.cds.controllerblueprints.core.asBluePrintsDataTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asPropertyDefinitionMap
import kotlin.test.Test
import kotlin.test.assertNotNull

class BluePrintsAnnotationsTest {

    @Test
    fun testBluePrintWorkflowData() {
        val wfInput = TestBluePrintsWorkflowInput::class.asPropertyDefinitionMap()
        // println(wfInput.asJsonString(true))
        assertNotNull(wfInput, "failed to generate wfInput property map")

        val wfOutput = TestBluePrintsWorkflowOutput::class.asPropertyDefinitionMap()
        // println(wfOutput.asJsonString(true))
        assertNotNull(wfInput, "failed to generate wfOutput property map")
    }

    @Test
    fun testBluePrintDataType() {
        val dataTypes = TestBluePrintsDataType::class.asBluePrintsDataTypes()
        // println(dataTypes.asJsonString(true))
        assertNotNull(dataTypes, "failed to generate dataTypes definition")
    }
}

@BluePrintsDataType(
    name = "dt-test-datatype", description = "I am test",
    version = "1.0.0", derivedFrom = "tosca.datatypes.root"
)
data class TestBluePrintsDataType(
    @BluePrintsProperty(description = "this stringData")
    var stringData: String,
    @BluePrintsProperty(description = "this stringDataWithValue")
    @PropertyDefaultValue(value = "USA")
    val stringDataWithValue: String,
    @BluePrintsProperty(description = "this intDataWithValue")
    @PropertyDefaultValue(value = "30")
    val intDataWithValue: Int,
    @BluePrintsProperty(description = "this booleanDataWithValue")
    @PropertyDefaultValue(value = "true")
    val booleanDataWithValue: Boolean,
    @BluePrintsProperty(description = "this anyData")
    val anyData: Any,
    @BluePrintsProperty(description = "this jsonDataWithValue")
    @PropertyDefaultValue(value = """{"data" : "1234"}""")
    val jsonDataWithValue: JsonNode?,
    @BluePrintsProperty(description = "listData")
    val listData: MutableList<String>,
    @BluePrintsProperty(description = "this mapData")
    val mapData: MutableMap<String, String> = hashMapOf(),
    @BluePrintsProperty(description = "this complexData")
    val complexData: TestBluePrintsChildDataType?,
    @BluePrintsProperty(description = "this complexDataList")
    val complexDataList: MutableList<TestBluePrintsChildDataType>
)

data class TestBluePrintsChildDataType(val name: String)

@BluePrintsWorkflowInput
data class TestBluePrintsWorkflowInput(
    @BluePrintsProperty(description = "this sample name")
    @PropertyDefaultValue(value = "Brinda")
    var name: String,
    @BluePrintsProperty(description = "this sample name")
    val place: String
)

@BluePrintsWorkflowOutput
data class TestBluePrintsWorkflowOutput(
    @BluePrintsProperty(description = "this is dslExpression")
    @DSLExpression("field1")
    var dslExpression: String,

    @BluePrintsProperty(description = "this is withNodeAttributeExpression")
    @AttributeExpression(modelableEntityName = "sample-node", attributeName = "response-data")
    var withNodeAttributeExpression: String,

    @BluePrintsProperty(description = "this is withNodeAttributeExpressionSubAttribute")
    @AttributeExpression(
        modelableEntityName = "sample-node", attributeName = "response-data",
        subAttributeName = ".\$field1"
    )
    var withNodeAttributeExpressionSubAttribute: String,

    @BluePrintsProperty(description = "this is withAttributeExpressionSubAttribute")
    @AttributeExpression(attributeName = "response-data", subAttributeName = ".\$field1")
    var withAttributeExpressionSubAttribute: String,

    @BluePrintsProperty(description = "this is withAttributeExpression")
    @AttributeExpression(attributeName = "response-data")
    var withAttributeExpression: String,

    @BluePrintsProperty(description = "this is withAArtifactExpression")
    @ArtifactExpression(modelableEntityName = "test-node", artifactName = "content-template")
    var withAArtifactExpression: String,

    @BluePrintsProperty(description = "this status")
    val status: String = "success"
)
