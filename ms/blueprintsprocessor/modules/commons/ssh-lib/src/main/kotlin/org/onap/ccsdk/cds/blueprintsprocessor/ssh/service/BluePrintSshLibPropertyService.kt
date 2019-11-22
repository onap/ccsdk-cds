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

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.BasicAuthSshClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.SshClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.ssh.SshLibConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.stereotype.Service

@Service(SshLibConstants.SERVICE_BLUEPRINT_SSH_LIB_PROPERTY)
open class BluePrintSshLibPropertyService(private var bluePrintProperties: BluePrintPropertiesService) {

    fun blueprintSshClientService(jsonNode: JsonNode): BlueprintSshClientService {
        val restClientProperties = sshClientProperties(jsonNode)
        return blueprintSshClientService(restClientProperties)
    }

    fun blueprintSshClientService(selector: String): BlueprintSshClientService {
        val prefix = "${SshLibConstants.PROPERTY_SSH_CLIENT_PREFIX}$selector"
        val sshClientProperties = sshClientProperties(prefix)
        return blueprintSshClientService(sshClientProperties)
    }

    fun sshClientProperties(prefix: String): SshClientProperties {
        val type = bluePrintProperties.propertyBeanType("$prefix.type", String::class.java)
        return when (type) {
            SshLibConstants.TYPE_BASIC_AUTH -> {
                basicAuthSshClientProperties(prefix)
            }
            else -> {
                throw BluePrintProcessorException("SSH adaptor($type) is not supported")
            }
        }
    }

    fun sshClientProperties(jsonNode: JsonNode): SshClientProperties {
        val type = jsonNode.get("type")?.textValue()
            ?: throw BluePrintProcessorException("missing type field in ssh client properties")
        return when (type) {
            SshLibConstants.TYPE_BASIC_AUTH -> {
                JacksonUtils.readValue(
                    jsonNode,
                    BasicAuthSshClientProperties::class.java
                )!!
            }
            else -> {
                throw BluePrintProcessorException("SSH adaptor($type) is not supported")
            }
        }
    }

    private fun blueprintSshClientService(sshClientProperties: SshClientProperties): BlueprintSshClientService {

        when (sshClientProperties) {
            is BasicAuthSshClientProperties -> {
                return BasicAuthSshClientService(sshClientProperties)
            }
            else -> {
                throw BluePrintProcessorException("couldn't get SSH client service for")
            }
        }
    }

    private fun basicAuthSshClientProperties(prefix: String): BasicAuthSshClientProperties {
        return bluePrintProperties.propertyBeanType(
            prefix, BasicAuthSshClientProperties::class.java
        )
    }
}
