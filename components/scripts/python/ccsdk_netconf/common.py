#  Copyright (c) 2019 Bell Canada.
#  Modifications Copyright (c) 2018 - 2019 IBM, Bell Canada.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
from netconf_constant import *
from org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution import ResourceResolutionExtensionsKt
from blueprint_runtime_service import BluePrintRuntimeService


class ResolutionHelper:

    def __init__(self, component_function):
        self.component_function = component_function
        self.blueprint_runtime = BluePrintRuntimeService(component_function.bluePrintRuntimeService)

    def resolve_and_generate_message_from_template_prefix(self, artifact_prefix):
        """Get the template resolved in the current workflow execution by the artifact prefix

        :param artifact_prefix:
        :return: template
        """
        return ResourceResolutionExtensionsKt.contentFromResolvedArtifact(self.component_function, artifact_prefix)

    def retrieve_resolved_template_from_database(self, key, artifact_template):
        """Get the template resolved and stored with resolution-key and matching with the artifact name

        :param key:
        :param artifact_template:
        :return:
        """
        return ResourceResolutionExtensionsKt.storedContentFromResolvedArtifact(self.component_function, key,
                                                                                artifact_template)

    def set_execution_attribute_response_data(self, response_data):
        """For the current node execution, set the attribute value of response-data

        :param response_data:
        :return:
        """
        self.component_function.setAttribute(ATTRIBUTE_RESPONSE_DATA, response_data)

    def get_node_template_attribute(self, node_template_name, attribute_key):
        """get attribute value for a specific node template of the current workflow

        :param node_template_name:
        :param attribute_key:
        :return: JsonNode
        """
        return self.component_function.getNodeTemplateAttributeValue(node_template_name, attribute_key)

    def get_blueprint_runtime(self):
        """get blueprint runtime environment

        :param
        :return: BluePrintRuntimeService
        """
        return self.blueprint_runtime

