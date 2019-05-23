package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils

import com.fasterxml.jackson.databind.node.TextNode
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import kotlin.test.assertEquals

class ResourceAssignmentUtilsTest {

    @Test
    fun `generateResourceDataForAssignments - positive test`() {
        //given a valid resource assignment
        val validResourceAssignment = createResourceAssignmentForTest("valid_value")

        //and a list containing that resource assignment
        val resourceAssignmentList = listOf<ResourceAssignment>(validResourceAssignment)

        //when the values of the resources are evaluated
        val outcome = ResourceAssignmentUtils.generateResourceDataForAssignments(resourceAssignmentList)

        //then the assignment should produce a valid result
        val expected = "{\n" + "  \"pnf-id\" : \"valid_value\"\n" + "}"
        assertEquals(expected, outcome, "unexpected outcome generated")

    }

    @Test
    fun `generateResourceDataForAssignments - resource has null value`() {
        //given a valid resource assignment
        val resourceAssignmentWithNullValue = createResourceAssignmentForTest(null)

        //and a list containing that resource assignment
        val resourceAssignmentList = listOf<ResourceAssignment>(resourceAssignmentWithNullValue)

        //when the values of the resources are evaluated
        val outcome = ResourceAssignmentUtils.generateResourceDataForAssignments(resourceAssignmentList)

        //then the assignment should produce a valid result
        val expected = "{\n" + "  \"pnf-id\" : \"\$pnf-id\"\n" + "}"
        assertEquals(expected, outcome, "unexpected outcome generated")

    }

    private fun createResourceAssignmentForTest(resourceValue: String?): ResourceAssignment {
        val valueForTest = if (resourceValue == null) null else TextNode(resourceValue)
        val resourceAssignmentForTest = ResourceAssignment().apply {
            name = "pnf-id"
            dictionaryName = "pnf-id"
            dictionarySource = "input"
            property = PropertyDefinition().apply {
                type = "string"
                value = valueForTest
            }
        }
        return resourceAssignmentForTest
    }
}