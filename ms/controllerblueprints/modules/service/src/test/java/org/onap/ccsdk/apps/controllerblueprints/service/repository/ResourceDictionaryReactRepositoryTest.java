/*
 *  Copyright © 2018 IBM.
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

package org.onap.ccsdk.apps.controllerblueprints.service.repository;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.apps.controllerblueprints.TestApplication;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ResourceDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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

    @Autowired
    protected ResourceDictionaryReactRepository resourceDictionaryReactRepository;

    @Test
    public void test01FindByNameReact() throws Exception {
        ResourceDictionary dbResourceDictionary = resourceDictionaryReactRepository.findByName("db-source").block();
        Assert.assertNotNull("Failed to query React Resource Dictionary by Name", dbResourceDictionary);
    }

    @Test
    public void test02FindByNameInReact() throws Exception {
        List<ResourceDictionary> dbResourceDictionaries =
                resourceDictionaryReactRepository.findByNameIn(Arrays.asList("db-source")).collectList().block();
        Assert.assertNotNull("Failed to query React Resource Dictionary by Names", dbResourceDictionaries);
    }

    @Test
    public void test03FindByTagsContainingIgnoreCaseReact() throws Exception {
        List<ResourceDictionary> dbTagsResourceDictionaries =
                resourceDictionaryReactRepository.findByTagsContainingIgnoreCase("db-source").collectList().block();
        Assert.assertNotNull("Failed to query React Resource Dictionary by Tags", dbTagsResourceDictionaries);
    }
}
