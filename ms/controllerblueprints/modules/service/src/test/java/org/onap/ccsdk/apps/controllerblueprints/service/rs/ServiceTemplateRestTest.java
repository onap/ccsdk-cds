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

package org.onap.ccsdk.apps.controllerblueprints.service.rs;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.apps.controllerblueprints.TestApplication;
import org.onap.ccsdk.apps.controllerblueprints.core.ConfigModelConstant;
import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModelContent;
import org.onap.ccsdk.apps.controllerblueprints.service.model.AutoMapResponse;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {"blueprints.load.initial-data=true"})
@ContextConfiguration(classes = {TestApplication.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServiceTemplateRestTest {

    private static EELFLogger log = EELFManager.getInstance().getLogger(ServiceTemplateRestTest.class);
    @Autowired
    ModelTypeRest modelTypeRest;

    @Autowired
    private ServiceTemplateRest serviceTemplateRest;

    @Test
    public void test02EnrichServiceTemplate() throws Exception {
        log.info("*********** test02EnrichServiceTemplate  ***********************");
        String file = "src/test/resources/enhance/enhance-template.json";

        String serviceTemplateContent = FileUtils.readFileToString(new File(file), Charset.defaultCharset());

        ServiceTemplate serviceTemplate = JacksonUtils.readValue(serviceTemplateContent, ServiceTemplate.class);

        serviceTemplate = serviceTemplateRest.enrichServiceTemplate(serviceTemplate);

        String enhancedFile = "src/test/resources/enhance/enhanced-template.json";

        FileUtils.write(new File(enhancedFile),
                JacksonUtils.getJson(serviceTemplate, true), Charset.defaultCharset());

        Assert.assertNotNull("Failed to get Enriched Blueprints, Return object is Null", serviceTemplate);
        Assert.assertNotNull("Failed to get Enriched Blueprints Data Type, Return object is Null",
                serviceTemplate.getDataTypes());
        Assert.assertNotNull("Failed to get Enriched Blueprints Node Type, Return object is Null",
                serviceTemplate.getNodeTypes());
        log.trace("Enriched Service Template :\n" + JacksonUtils.getJson(serviceTemplate, true));
    }

    @Test
    public void test03ValidateServiceTemplate() throws Exception {
        log.info("*********** test03ValidateServiceTemplate  *******************************************");
        String enhancedFile = "src/test/resources/enhance/enhanced-template.json";
        String serviceTemplateContent = FileUtils.readFileToString(new File(enhancedFile), Charset.defaultCharset());

        ServiceTemplate serviceTemplate =
                JacksonUtils.readValue(serviceTemplateContent, ServiceTemplate.class);

        serviceTemplate = serviceTemplateRest.validateServiceTemplate(serviceTemplate);

        Assert.assertNotNull("Failed to validate Service Template, Return object is Null", serviceTemplate);
        Assert.assertNotNull("Failed to get Service Template Data Type, Return object is Null",
                serviceTemplate.getDataTypes());
        Assert.assertNotNull("Failed to get Service Template Node Type, Return object is Null",
                serviceTemplate.getNodeTypes());

        log.trace("Validated Service Template :\n" + JacksonUtils.getJson(serviceTemplate, true));

    }


    @Test
    public void test04GenerateResourceAssignments() throws Exception {
        log.info("*********** test04GenerateResourceAssignments  *******************************************");
        ConfigModelContent baseConfigConfigModelContent = new ConfigModelContent();
        String baseConfigContent = FileUtils.readFileToString(new File("load/blueprints/vrr-test/Templates/base-config-template.vtl")
                , Charset.defaultCharset());
        baseConfigConfigModelContent.setName("base-config-template");
        baseConfigConfigModelContent.setContentType(ConfigModelConstant.MODEL_CONTENT_TYPE_TEMPLATE);
        baseConfigConfigModelContent.setContent(baseConfigContent);

        List<ResourceAssignment> resourceAssignments =
                serviceTemplateRest.generateResourceAssignments(baseConfigConfigModelContent);

        Assert.assertNotNull("Failed to get ResourceAssignments, Return object is Null", resourceAssignments);
        Assert.assertTrue("Failed to get ResourceAssignments count", resourceAssignments.size() > 0);

        log.trace("Validated Service Template :\n" + JacksonUtils.getJson(resourceAssignments, true));


    }

    @Test
    public void test05AutoMap() throws Exception {
        log.info("*********** test05AutoMap  *******************************************");

        String resourceAssignmentContent = FileUtils.readFileToString(
                new File("src/test/resources/resourcedictionary/automap.json"), Charset.defaultCharset());
        List<ResourceAssignment> batchResourceAssignment =
                JacksonUtils.getListFromJson(resourceAssignmentContent, ResourceAssignment.class);
        AutoMapResponse autoMapResponse = serviceTemplateRest.autoMap(batchResourceAssignment);

        Assert.assertNotNull("Failed to get ResourceAssignments, Return object is Null",
                autoMapResponse.getResourceAssignments());
        Assert.assertNotNull("Failed to get Data Dictionary from ResourceAssignments",
                autoMapResponse.getDataDictionaries());
        Assert.assertTrue("Failed to get ResourceAssignments count",
                CollectionUtils.isNotEmpty(autoMapResponse.getDataDictionaries()));

        List<ResourceAssignment> autoMappedResourceAssignment = autoMapResponse.getResourceAssignments();
        autoMappedResourceAssignment.forEach(resourceAssignment -> {
            if ("sample-db-source".equals(resourceAssignment.getName())) {
                Assert.assertEquals("Failed to assign default first source", "db",
                        resourceAssignment.getDictionarySource());
            }
        });

    }


}
