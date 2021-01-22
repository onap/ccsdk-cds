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

package org.onap.ccsdk.cds.blueprintsprocessor.ssh

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.service.BlueprintSshLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.service.BlueprintSshClientService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintDependencyService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan
@EnableConfigurationProperties
open class BlueprintSshLibConfiguration

/**
 * Exposed Dependency Service by this SSH Lib Module
 */
fun BlueprintDependencyService.sshLibPropertyService(): BlueprintSshLibPropertyService =
    instance(SshLibConstants.SERVICE_BLUEPRINT_SSH_LIB_PROPERTY)

fun BlueprintDependencyService.sshClientService(selector: String): BlueprintSshClientService =
    sshLibPropertyService().blueprintSshClientService(selector)

fun BlueprintDependencyService.sshClientService(jsonNode: JsonNode): BlueprintSshClientService =
    sshLibPropertyService().blueprintSshClientService(jsonNode)

class SshLibConstants {
    companion object {

        const val SERVICE_BLUEPRINT_SSH_LIB_PROPERTY = "blueprint-ssh-lib-property-service"
        const val PROPERTY_SSH_CLIENT_PREFIX = "blueprintsprocessor.sshclient."
        const val TYPE_BASIC_AUTH = "basic-auth"
    }
}
