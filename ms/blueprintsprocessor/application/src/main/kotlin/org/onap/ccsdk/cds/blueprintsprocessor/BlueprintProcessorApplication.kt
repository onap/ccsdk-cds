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

package org.onap.ccsdk.cds.blueprintsprocessor

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.ComponentScan

/**
 * BlueprintProcessorApplication
 *
 * @author Brinda Santh
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class, HazelcastAutoConfiguration::class])
@ComponentScan(
    basePackages = [
        "org.onap.ccsdk.cds.error.catalog",
        "org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"
    ]
)
open class BlueprintProcessorApplication

fun main(args: Array<String>) {
    // This is required for TemplateController.getStoredResult to accept a content-type value
    // as a request parameter, e.g. &format=application%2Fxml is accepted
    System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true")

    SpringApplication.run(BlueprintProcessorApplication::class.java, *args)
}
