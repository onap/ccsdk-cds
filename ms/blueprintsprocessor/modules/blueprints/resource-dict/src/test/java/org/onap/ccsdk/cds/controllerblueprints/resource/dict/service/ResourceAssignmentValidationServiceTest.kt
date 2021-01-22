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

package org.onap.ccsdk.cds.controllerblueprints.resource.dict.service

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.utils.ResourceDictionaryTestUtils
import org.slf4j.LoggerFactory

/**
 * ResourceAssignmentValidationServiceTest.
 *
 * @author Brinda Santh
 */
class ResourceAssignmentValidationServiceTest {

    private val log = LoggerFactory.getLogger(ResourceAssignmentValidationServiceTest::class.java)

    @Before
    fun setUp() {
        // Setup dummy Source Instance Mapping
        ResourceDictionaryTestUtils.setUpResourceSourceMapping()
    }

    @Test
    fun testValidateSuccess() {
        log.info("**************** testValidateSuccess *****************")
        val assignments = JacksonUtils.getListFromClassPathFile("validation/success.json", ResourceAssignment::class.java)
        val resourceAssignmentValidator = ResourceAssignmentValidationServiceImpl()
        val result = resourceAssignmentValidator.validate(assignments!!)
        Assert.assertTrue("Failed to Validate", result)
    }

    @Test(expected = BlueprintException::class)
    fun testValidateDuplicate() {
        log.info(" **************** testValidateDuplicate *****************")
        val assignments = JacksonUtils.getListFromClassPathFile("validation/duplicate.json", ResourceAssignment::class.java)
        val resourceAssignmentValidator = ResourceAssignmentValidationServiceImpl()
        resourceAssignmentValidator.validate(assignments!!)
    }

    @Test(expected = BlueprintException::class)
    fun testValidateCyclic() {
        log.info(" ****************  testValidateCyclic *****************")
        val assignments = JacksonUtils.getListFromClassPathFile("validation/cyclic.json", ResourceAssignment::class.java)
        val resourceAssignmentValidator = ResourceAssignmentValidationServiceImpl()
        resourceAssignmentValidator.validate(assignments!!)
    }
}
