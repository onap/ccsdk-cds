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

package org.onap.ccsdk.cds.blueprintsprocessor;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.ccsdk.cds.controllerblueprints.core.config.BluePrintLoadConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * BlueprintProcessorApplicationTest
 *
 * @author Brinda Santh
 * DATE : 8/14/2018
 */

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BlueprintProcessorApplication.class, BluePrintLoadConfiguration.class})
@SpringBootTest(classes = BlueprintProcessorApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BlueprintProcessorApplicationTest {

    @Autowired
    private ApplicationContext context;

    @Before
    public void setUp() {

    }

    @Test
    public void testSample() {
        Assert.assertNotNull("Failed to create Application Context ", context);
    }

}
