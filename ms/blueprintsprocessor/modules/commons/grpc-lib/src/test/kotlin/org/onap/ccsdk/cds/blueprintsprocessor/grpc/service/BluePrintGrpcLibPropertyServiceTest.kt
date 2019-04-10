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

package org.onap.ccsdk.cds.blueprintsprocessor.grpc.service

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.BasicAuthGrpcClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.BluePrintGrpcLibConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [BluePrintGrpcLibConfiguration::class,
    BlueprintPropertyConfiguration::class, BluePrintProperties::class])
@TestPropertySource(properties =
["blueprintsprocessor.grpcclient.sample.type=basic-auth",
    "blueprintsprocessor.grpcclient.sample.host=127.0.0.1",
    "blueprintsprocessor.grpcclient.sample.port=50505",
    "blueprintsprocessor.grpcclient.sample.username=sampleuser",
    "blueprintsprocessor.grpcclient.sample.password=sampleuser"
])
class BluePrintGrpcLibPropertyServiceTest {

    @Autowired
    lateinit var bluePrintGrpcLibPropertyService: BluePrintGrpcLibPropertyService

    @Test
    fun testGrpcClientProperties() {
        val properties = bluePrintGrpcLibPropertyService.grpcClientProperties(
                "blueprintsprocessor.grpcclient.sample") as BasicAuthGrpcClientProperties
        assertNotNull(properties, "failed to create property bean")
        assertNotNull(properties.host, "failed to get host property in property bean")
        assertNotNull(properties.port, "failed to get host property in property bean")
        assertNotNull(properties.username, "failed to get host property in property bean")
        assertNotNull(properties.password, "failed to get host property in property bean")
    }
}