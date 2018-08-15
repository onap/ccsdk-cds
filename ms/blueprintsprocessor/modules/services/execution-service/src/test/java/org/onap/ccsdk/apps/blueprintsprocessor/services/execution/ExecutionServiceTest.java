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

package org.onap.ccsdk.apps.blueprintsprocessor.services.execution;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput;
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceOutput;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.charset.Charset;


/**
 * ExecutionServiceTest
 *
 * @author Brinda Santh
 * DATE : 8/15/2018
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ExecutionService.class)
public class ExecutionServiceTest {
    private static Logger log = LoggerFactory.getLogger(ExecutionServiceTest.class);

    @Autowired
    private ExecutionService executionService;

    @Test
    public void testExecutionService() throws Exception {

        Assert.assertNotNull("failed to create ResourceResolutionService", executionService);

        String resourceResolutionInputContent = FileUtils.readFileToString(
                new File("src/test/resources/payload/requests/sample-execution-request.json"), Charset.defaultCharset());

        ExecutionServiceInput executionServiceInput = JacksonUtils.readValue(resourceResolutionInputContent, ExecutionServiceInput.class );
        Assert.assertNotNull("failed to populate executionServiceInput request ",executionServiceInput);

        ObjectNode inputContent = (ObjectNode)JacksonUtils.jsonNodeFromFile("src/test/resources/payload/inputs/input.json");
        Assert.assertNotNull("failed to populate input payload ",inputContent);
        executionServiceInput.setPayload(inputContent);

        ExecutionServiceOutput executionServiceOutput = executionService.process(executionServiceInput);
        Assert.assertNotNull("failed to populate output",executionServiceOutput);

    }
}