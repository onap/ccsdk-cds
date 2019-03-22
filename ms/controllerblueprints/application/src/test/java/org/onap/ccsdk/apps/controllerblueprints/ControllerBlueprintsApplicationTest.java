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

package org.onap.ccsdk.apps.controllerblueprints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ControllerBlueprintsApplicationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setUp(){
        BasicAuthorizationInterceptor bai = new BasicAuthorizationInterceptor("ccsdkapps", "ccsdkapps");
        this.restTemplate.getRestTemplate().getInterceptors().add(bai);
    }

    @Test
    public void testConfigModel() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
//        ResponseEntity<BlueprintModel> entity = this.restTemplate
//                .exchange("/api/v1/config-model/1", HttpMethod.GET, new HttpEntity<>(headers),BlueprintModel.class);
//        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
//        Assert.assertNotNull("failed to get response Config model",entity.getBody());
    }

    @Test
    public void testConfigModelFailure() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
//        ResponseEntity<BlueprintModel> entity = this.restTemplate
//                .exchange("/api/v1/config-model-not-found/1", HttpMethod.GET, new HttpEntity<>(headers),BlueprintModel.class);
//        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
//        Assert.assertNotNull("failed to get response Config model",entity.getBody());
    }
}
