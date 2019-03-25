/*
 *  Copyright © 2018 IBM.
 *  Modifications Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.controllerblueprints.service.repository;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.cds.controllerblueprints.TestApplication;
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition;
import org.onap.ccsdk.cds.controllerblueprints.service.domain.ResourceDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

/**
 * ResourceDictionaryReactRepositoryTest.
 *
 * @author Brinda Santh
 */

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestApplication.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ResourceDictionaryReactRepositoryTest {

    private String sourceName = "test-source";

    @Autowired
    protected ResourceDictionaryReactRepository resourceDictionaryReactRepository;

    @Test
    @Commit
    public void test01Save() {
        ResourceDefinition resourceDefinition = JacksonUtils.Companion.readValueFromFile("./../../../../components/model-catalog/resource-dictionary/starter-dictionary/sample-primary-db-source.json", ResourceDefinition.class);
        Assert.assertNotNull("Failed to get resourceDefinition from content", resourceDefinition);
        resourceDefinition.setName(sourceName);

        ResourceDictionary resourceDictionary = transformResourceDictionary(resourceDefinition);
        ResourceDictionary dbResourceDictionary = resourceDictionaryReactRepository.save(resourceDictionary).block();
        Assert.assertNotNull("Failed to save ResourceDictionary", dbResourceDictionary);
    }

    @Test
    public void test02FindByNameReact() {
        ResourceDictionary dbResourceDictionary = resourceDictionaryReactRepository.findByName(sourceName).block();
        Assert.assertNotNull("Failed to query React Resource Dictionary by Name", dbResourceDictionary);
    }

    @Test
    public void test03FindByNameInReact() {
        List<ResourceDictionary> dbResourceDictionaries =
                resourceDictionaryReactRepository.findByNameIn(Arrays.asList(sourceName)).collectList().block();
        Assert.assertNotNull("Failed to query React Resource Dictionary by Names", dbResourceDictionaries);
    }

    @Test
    public void test04FindByTagsContainingIgnoreCaseReact() {
        List<ResourceDictionary> dbTagsResourceDictionaries =
                resourceDictionaryReactRepository.findByTagsContainingIgnoreCase(sourceName).collectList().block();
        Assert.assertNotNull("Failed to query React Resource Dictionary by Tags", dbTagsResourceDictionaries);
    }

    @Test
    @Commit
    public void test05Delete() {
        resourceDictionaryReactRepository.deleteByName(sourceName).block();
    }

    private ResourceDictionary transformResourceDictionary(ResourceDefinition resourceDefinition) {
        ResourceDictionary resourceDictionary = new ResourceDictionary();
        resourceDictionary.setName(resourceDefinition.getName());
        resourceDictionary.setDataType(resourceDefinition.getProperty().getType());
        resourceDictionary.setDescription(resourceDefinition.getProperty().getDescription());
        resourceDictionary.setTags(resourceDefinition.getTags());
        resourceDictionary.setUpdatedBy(resourceDefinition.getUpdatedBy());
        resourceDictionary.setDefinition(resourceDefinition);
        return resourceDictionary;
    }
}
