/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.controllerblueprints.service.common;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.cds.controllerblueprints.service.SchemaGeneratorService;

import java.io.File;
import java.nio.charset.Charset;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SchemaGeneratorServiceTest {

    private static EELFLogger log = EELFManager.getInstance().getLogger(SchemaGeneratorServiceTest.class);

    @Test
    public void test01GenerateSwaggerData() throws Exception {
        log.info("******************* test01GenerateSwaggerData  ******************************");

        String file = "src/test/resources/enhance/enhanced-template.json";
        String serviceTemplateContent = FileUtils.readFileToString(new File(file), Charset.defaultCharset());
        SchemaGeneratorService schemaGeneratorService = new SchemaGeneratorService();
        String schema = schemaGeneratorService.generateSchema(serviceTemplateContent);
        log.trace("Generated Schema " + schema);
        Assert.assertNotNull("failed to generate Sample Data", schema);

    }

}
