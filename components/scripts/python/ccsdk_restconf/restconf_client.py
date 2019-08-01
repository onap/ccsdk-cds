#
# ============LICENSE_START=======================================================
#  Copyright (C) 2019 Nordix Foundation.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# ============LICENSE_END=========================================================
#
from time import sleep
from org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor import RestconfExecutorExtensionsKt
from org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution import ResourceResolutionExtensionsKt


class RestconfClient:
    __base_odl_url = "restconf/config/network-topology:network-topology/topology/topology-netconf/node/"
    __odl_status_check_limit = 10
    __odl_status_check_pause = 1
    # Once confirmed to be reliable, the check should change to use the connection-status API
    __odl_status_check_url = "restconf/operational/network-topology:network-topology/topology/topology-netconf/node/"

    def __init__(self, log, restconf_component_function):
        self.__log = log
        self.__component_function = restconf_component_function

    def web_client_service(self, identifier):
        return RestconfExecutorExtensionsKt.restconfClientService(self.__component_function, identifier)

    def resolve_and_generate_message_from_template_prefix(self, artifact_prefix):
        return ResourceResolutionExtensionsKt.contentFromResolvedArtifact(self.__component_function, artifact_prefix)

    def retrieve_resolved_template_from_database(self, key, artifact_template):
        return ResourceResolutionExtensionsKt.storedContentFromResolvedArtifact(self.__component_function, key,
                                                                                artifact_template)

    def mount_device(self, web_client_service, nf_id, mount_payload):
        self.__log.debug("mounting device {}", nf_id)
        headers = {"Content-Type": "application/xml"}
        url = self.__base_odl_url + nf_id
        self.__log.debug("sending mount request, url: {}", url)
        web_client_service.exchangeResource("PUT", url, mount_payload, headers)
        self.__wait_for_odl_to_mount(web_client_service, nf_id)

    def __wait_for_odl_to_mount(self, web_client_service, nf_id):
        counter = 0
        url = self.__odl_status_check_url + nf_id
        self.__log.info("url for ODL status check: {}", url)
        # TODO: allow JSON format change
        expected_result = '"netconf-node-topology:connection-status":"connected"'
        while counter < self.__odl_status_check_limit:
            result = web_client_service.exchangeResource("GET", url, "")
            if expected_result in result.body:
                self.__log.info("NF was mounted successfully on ODL")
                return None
            sleep(self.__odl_status_check_pause)
            counter += 1
        raise Exception("NF was not mounted on ODL, aborting configuration procedure")

    def configure_device_json_patch(self, web_client_service, nf_id, configlet_resource_path, configlet_to_apply):
        headers = {"Content-Type": "application/yang.patch+json"}
        self.__configure_device(web_client_service, nf_id, configlet_resource_path, configlet_to_apply, headers)

    def configure_device_xml_patch(self, web_client_service, nf_id, configlet_resource_path, configlet_to_apply):
        headers = {"Content-Type": "application/yang.patch+xml"}
        self.__configure_device(web_client_service, nf_id, configlet_resource_path, configlet_to_apply, headers)

    def __configure_device(self, web_client_service, nf_id, configlet_resource_path, configlet_to_apply, headers):
        self.__log.debug("headers: {}", headers)
        self.__log.info("configuring device: {}, Configlet: {}", nf_id, configlet_to_apply)
        url = self.__base_odl_url + nf_id + configlet_resource_path
        self.__log.debug("sending patch request,  url: {}", url)
        result = web_client_service.exchangeResource("PATCH", url, configlet_to_apply, headers)
        self.__log.info("Configuration application result: {}", result)

    def retrieve_device_configuration_subtree(self, web_client_service, nf_id, configlet_resource_path):
        url = self.__base_odl_url + nf_id + configlet_resource_path
        self.__log.debug("sending GET request,  url: {}", url)
        result = web_client_service.exchangeResource("GET", url, "")
        return result

    def unmount_device(self, web_client_service, nf_id):
        url = self.__base_odl_url + nf_id
        self.__log.debug("sending unmount request, url: {}", url)
        web_client_service.exchangeResource("DELETE", url, "")
