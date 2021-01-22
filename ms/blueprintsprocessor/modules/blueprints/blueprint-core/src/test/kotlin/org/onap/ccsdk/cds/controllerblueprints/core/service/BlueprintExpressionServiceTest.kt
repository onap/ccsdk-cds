/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.core.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.data.ExpressionData
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 *
 *
 * @author Brinda Santh
 */
class BlueprintExpressionServiceTest {

    @Test
    fun testInputExpression() {
        val node: JsonNode = jacksonObjectMapper().readTree("{ \"get_input\" : \"input-name\" }")
        val expressionData: ExpressionData = BlueprintExpressionService.getExpressionData(node)
        assertNotNull(expressionData, " Failed to populate expression data")
        assertEquals(expressionData.isExpression, true, "Failed to identify as expression")
        assertNotNull(expressionData.inputExpression, " Failed to populate input expression data")
        assertEquals("input-name", expressionData.inputExpression?.propertyName, "Failed to get propertyName from expression data")
    }

    @Test
    fun testPropertyExpression() {
        val node: JsonNode = jacksonObjectMapper().readTree("{ \"get_property\" : [\"SELF\", \"property-name\"] }")
        val expressionData: ExpressionData = BlueprintExpressionService.getExpressionData(node)
        assertNotNull(expressionData, " Failed to populate expression data")
        assertEquals(expressionData.isExpression, true, "Failed to identify as expression")
        assertNotNull(expressionData.propertyExpression, " Failed to populate property expression data")
        assertEquals("SELF", expressionData.propertyExpression?.modelableEntityName, " Failed to get expected modelableEntityName")
        assertEquals("property-name", expressionData.propertyExpression?.propertyName, " Failed to get expected propertyName")

        val node1: JsonNode = jacksonObjectMapper().readTree("{ \"get_property\" : [\"SELF\", \"\",\"property-name\", \"resource\", \"name\"] }")
        val expressionData1: ExpressionData = BlueprintExpressionService.getExpressionData(node1)
        assertNotNull(expressionData1, " Failed to populate expression data")
        assertEquals(expressionData1.isExpression, true, "Failed to identify as nested property expression")
        assertNotNull(expressionData1.propertyExpression, " Failed to populate nested property expression data")
        assertEquals("SELF", expressionData1.propertyExpression?.modelableEntityName, " Failed to get expected modelableEntityName")
        assertEquals("property-name", expressionData1.propertyExpression?.propertyName, " Failed to get expected propertyName")
        assertEquals(
            "resource.name",
            expressionData1.propertyExpression?.subPropertyName,
            " Failed to populate nested subPropertyName expression data"
        )
    }

    @Test
    fun testAttributeExpression() {
        val node: JsonNode = jacksonObjectMapper().readTree("{ \"get_attribute\" : [\"SELF\", \"resource\"] }")
        val expressionData: ExpressionData = BlueprintExpressionService.getExpressionData(node)
        assertNotNull(expressionData, " Failed to populate expression data")
        assertEquals(expressionData.isExpression, true, "Failed to identify as expression")
        assertNotNull(expressionData.attributeExpression, " Failed to populate attribute expression data")
        assertEquals("SELF", expressionData.attributeExpression?.modelableEntityName, " Failed to get expected modelableEntityName")
        assertEquals("resource", expressionData.attributeExpression?.attributeName, " Failed to get expected attributeName")

        val node1: JsonNode = jacksonObjectMapper().readTree("{ \"get_attribute\" : [\"SELF\", \"\",\"attribute-name\", \"resource\", \"name\"] }")
        val expressionData1: ExpressionData = BlueprintExpressionService.getExpressionData(node1)
        assertNotNull(expressionData1, " Failed to populate expression data")
        assertEquals(expressionData1.isExpression, true, "Failed to identify as expression")
        assertNotNull(expressionData1.attributeExpression, " Failed to populate attribute expression data")
        assertEquals("SELF", expressionData1.attributeExpression?.modelableEntityName, " Failed to get expected modelableEntityName")
        assertEquals("attribute-name", expressionData1.attributeExpression?.attributeName, " Failed to get expected attributeName")
        assertEquals(
            "resource.name",
            expressionData1.attributeExpression?.subAttributeName,
            " Failed to populate nested subAttributeName expression data"
        )
    }

    @Test
    fun testOutputOperationExpression() {
        val node: JsonNode =
            jacksonObjectMapper().readTree("{ \"get_operation_output\": [\"SELF\", \"interface-name\", \"operation-name\", \"output-property-name\"] }")
        val expressionData: ExpressionData = BlueprintExpressionService.getExpressionData(node)
        assertNotNull(expressionData, " Failed to populate expression data")
        assertEquals(expressionData.isExpression, true, "Failed to identify as expression")
        assertNotNull(expressionData.operationOutputExpression, " Failed to populate output expression data")
        assertEquals("SELF", expressionData.operationOutputExpression?.modelableEntityName, " Failed to get expected modelableEntityName")
        assertEquals("interface-name", expressionData.operationOutputExpression?.interfaceName, " Failed to get expected interfaceName")
        assertEquals("operation-name", expressionData.operationOutputExpression?.operationName, " Failed to get expected operationName")
        assertEquals("output-property-name", expressionData.operationOutputExpression?.propertyName, " Failed to get expected propertyName")
    }

    @Test
    fun testArtifactExpression() {
        val node: JsonNode = jacksonObjectMapper().readTree("{ \"get_artifact\" : [\"SELF\", \"artifact-template\"] }")
        val expressionData: ExpressionData = BlueprintExpressionService.getExpressionData(node)
        assertNotNull(expressionData, " Failed to populate expression data")
        assertEquals(expressionData.isExpression, true, "Failed to identify as expression")
        assertNotNull(expressionData.artifactExpression, " Failed to populate Artifact expression data")
        assertEquals("SELF", expressionData.artifactExpression?.modelableEntityName, " Failed to get expected modelableEntityName")
        assertEquals("artifact-template", expressionData.artifactExpression?.artifactName, " Failed to get expected artifactName")

        val node1: JsonNode = jacksonObjectMapper().readTree("{ \"get_artifact\" : [\"SELF\", \"artifact-template\", \"location\", true] }")
        val expressionData1: ExpressionData = BlueprintExpressionService.getExpressionData(node1)
        assertNotNull(expressionData1, " Failed to populate expression data")
        assertEquals(expressionData1.isExpression, true, "Failed to identify as expression")
        assertNotNull(expressionData1.artifactExpression, " Failed to populate Artifact expression data")
        assertEquals("SELF", expressionData1.artifactExpression?.modelableEntityName, " Failed to get expected modelableEntityName")
        assertEquals("artifact-template", expressionData1.artifactExpression?.artifactName, " Failed to get expected artifactName")
        assertEquals("location", expressionData1.artifactExpression?.location, " Failed to get expected location")
        assertEquals(true, expressionData1.artifactExpression?.remove, " Failed to get expected remove")
    }

    @Test
    fun testDSLExpression() {
        val node: JsonNode = "*dynamic-rest-source".asJsonPrimitive()
        val expressionData: ExpressionData = BlueprintExpressionService.getExpressionData(node)
        assertNotNull(expressionData, " Failed to populate expression data")
        assertEquals(expressionData.isExpression, true, "Failed to identify as expression")
        assertNotNull(expressionData.dslExpression, " Failed to populate dsl expression data")
        assertEquals(
            "dynamic-rest-source", expressionData.dslExpression!!.propertyName,
            " Failed to populate dsl property name"
        )
    }
}
