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

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.apps.controllerblueprints.TestApplication;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ResourceDictionary;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {TestApplication.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ResourceDictionaryRestTest {

    private static EELFLogger log = EELFManager.getInstance().getLogger(ResourceDictionaryRestTest.class);

    @Autowired
    protected ResourceDictionaryRest resourceDictionaryRest;

    @Test
    public void test01SaveDataDictionary() throws Exception {
        String definition = IOUtils.toString(
                getClass().getClassLoader().getResourceAsStream("resourcedictionary/default_definition.json"),
                Charset.defaultCharset());

        ResourceDictionary dataDictionary = new ResourceDictionary();
        dataDictionary.setResourcePath("test/vnf/ipaddress");
        dataDictionary.setName("test-name");
        dataDictionary.setDefinition(definition);
        dataDictionary.setValidValues("127.0.0.1");
        dataDictionary.setResourceType("ONAP");
        dataDictionary.setDataType("string");
        dataDictionary.setDescription("Sample Resource Mapping");
        dataDictionary.setTags("test, ipaddress");
        dataDictionary.setUpdatedBy("xxxxxx@xxx.com");

        dataDictionary = resourceDictionaryRest.saveResourceDictionary(dataDictionary);

        Assert.assertNotNull("Failed to get Saved Resource Dictionary", dataDictionary);
        Assert.assertNotNull("Failed to get Saved Resource Dictionary, Id", dataDictionary.getName());

        ResourceDictionary dbDataDictionary =
                resourceDictionaryRest.getResourceDictionaryByName(dataDictionary.getName());
        Assert.assertNotNull("Failed to query Resource Dictionary for ID (" + dataDictionary.getName() + ")",
                dbDataDictionary);
        Assert.assertNotNull("Failed to query Resource Dictionary definition for ID (" + dataDictionary.getName() + ")",
                dbDataDictionary.getDefinition());

        log.trace("Saved Dictionary " + dbDataDictionary.getDefinition());

    }

    @Test
    public void test02GetDataDictionary() throws Exception {

        ResourceDictionary dbResourceDictionary = resourceDictionaryRest.getResourceDictionaryByName("test-name");
        Assert.assertNotNull("Failed to query Resource Dictionary by Name", dbResourceDictionary);

        String tags = "ipaddress";

        List<ResourceDictionary> dbResourceDictionaries = resourceDictionaryRest.searchResourceDictionaryByTags(tags);
        Assert.assertNotNull("Failed to search ResourceDictionary by tags", dbResourceDictionaries);
        Assert.assertTrue("Failed to search searchResourceDictionaryByTags by tags by count",
                dbResourceDictionaries.size() > 0);

        List<String> names = new ArrayList<>();
        names.add("test-name");
        dbResourceDictionaries = resourceDictionaryRest.searchResourceDictionaryByNames(names);
        Assert.assertNotNull("Failed to search ResourceDictionary by Names", dbResourceDictionaries);
        Assert.assertTrue("Failed to search searchResourceDictionaryByNames by tags by count",
                dbResourceDictionaries.size() > 0);

    }

}
