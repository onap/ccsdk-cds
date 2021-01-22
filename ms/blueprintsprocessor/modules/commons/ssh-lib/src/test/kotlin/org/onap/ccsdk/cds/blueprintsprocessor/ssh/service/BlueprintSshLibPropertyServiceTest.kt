/*
 *  Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.ssh.service

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.BasicAuthSshClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.BlueprintSshLibConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [
        BlueprintSshLibConfiguration::class,
        BlueprintPropertyConfiguration::class, BlueprintPropertiesService::class
    ]
)
@TestPropertySource(
    properties =
        [
            "blueprintsprocessor.sshclient.sample.type=basic-auth",
            "blueprintsprocessor.sshclient.sample.host=127.0.0.1",
            "blueprintsprocessor.sshclient.sample.port=22",
            "blueprintsprocessor.sshclient.sample.password=1234",
            "blueprintsprocessor.sshclient.sample.username=dummy"
        ]
)
class BlueprintSshLibPropertyServiceTest {

    @Autowired
    lateinit var bluePrintSshLibPropertyService: BlueprintSshLibPropertyService

    @Test
    fun testRestClientProperties() {
        val properties = bluePrintSshLibPropertyService
            .sshClientProperties("blueprintsprocessor.sshclient.sample") as BasicAuthSshClientProperties
        assertNotNull(properties, "failed to create property bean")
        assertEquals(properties.host, "127.0.0.1", "failed to match host property")
        assertEquals(properties.port, 22, "failed to match port property")
        assertEquals(properties.password, "1234", "failed to match host property")
        assertEquals(properties.username, "dummy", "failed to match host property")
    }
}
