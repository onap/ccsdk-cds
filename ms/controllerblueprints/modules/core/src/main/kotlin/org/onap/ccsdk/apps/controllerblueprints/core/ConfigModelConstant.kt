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

package org.onap.ccsdk.apps.controllerblueprints.core

/**
 *
 *
 * @author Brinda Santh
 */
object ConfigModelConstant {

    const val MODEL_CONTENT_TYPE_TOSCA_JSON = "TOSCA_JSON"
    const val MODEL_CONTENT_TYPE_TEMPLATE = "TEMPLATE"

    const val MODEL_TYPE_DATA_TYPE = "tosca.datatypes.Root"
    const val MODEL_TYPE_DATA_TYPE_DYNAMIC = "tosca.datatypes.Dynamic"

    const val MODEL_TYPE_NODE_DG = "tosca.nodes.DG"
    const val MODEL_TYPE_NODE_COMPONENT = "tosca.nodes.Component"
    const val MODEL_TYPE_NODE_VNF = "tosca.nodes.Vnf"
    const val MODEL_TYPE_NODE_ARTIFACT = "tosca.nodes.Artifact"

    const val MODEL_TYPE_CAPABILITY_NETCONF = "tosca.capability.Netconf"
    const val MODEL_TYPE_CAPABILITY_SSH = "tosca.capability.Ssh"
    const val MODEL_TYPE_CAPABILITY_SFTP = "tosca.capability.Sftp"
    const val MODEL_TYPE_CAPABILITY_CHEF = "tosca.capability.Chef"
    const val MODEL_TYPE_CAPABILITY_ANSIBLEF = "tosca.capability.Ansible"

    const val CAPABILITY_PROPERTY_MAPPING = "mapping"

    const val SOURCE_INPUT = "input"
    const val SOURCE_DEFAULT = "default"
    const val SOURCE_MDSAL = "mdsal"
    const val SOURCE_DB = "db"

    const val PROPERTY_RECIPE_NAMES = "action-names"

}
