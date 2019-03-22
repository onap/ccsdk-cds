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
from time import sleep

from org.onap.ccsdk.apps.blueprintsprocessor.functions.restconf.executor import \
    RestconfComponentFunction
from java.lang import Exception as JavaException


class RestconfConfigDeploy(RestconfComponentFunction):

    log = globals()["log"]
    seconds_to_sleep = 5
    base_mount_url = "restconf/config/network-topology:network-topology/topology/topology-netconf/node/"
    server_identifier = "sdncodl"
    configlet_template_name = "config-assign"

    def process(self, execution_request):

        self.log.info("Started execution of process method")
        try:
            self.log.info("getting resolution-key")
            resolution_key = self.getDynamicProperties("resolution-key").asText()
            self.log.info("resolution_key: {}", resolution_key)

            self.log.info("getting pnf-id")
            pnf_id = execution_request.payload.get("config-deploy-request").get("config-deploy-properties").get("entity").get("pnf-id")
            pnf_id = str(pnf_id).strip('\"')
            self.log.info("pnf-id: {}", pnf_id)

            self.log.info("mounting device {}", pnf_id)
            self.mount(pnf_id)

            self.log.info("sleeping for {} seconds", self.seconds_to_sleep)
            sleep(self.seconds_to_sleep)

            try:
                self.log.info("configuring device {}", pnf_id)
                self.apply_configuration(pnf_id, resolution_key, self.configlet_template_name)
            except Exception, err:
                self.log.error("an error occurred while configuring device {}", err)
                raise err
            finally:
                self.log.info("unmounting device {}", pnf_id)
                self.unmount(pnf_id)

            self.log.info("Ended execution of process method")

        except JavaException, err:
            self.log.error("Java Exception in the script", err)
            raise err
        except Exception, err:
            self.log.error("Python Exception in the script", err)
            raise err

    def mount(self, pnf_id):
        self.log.info("meshing mount payload")
        mount_payload = self.resolveAndGenerateMessage("config-deploy-mapping", "config-deploy-template")
        self.log.info("mount payload: \n {}", mount_payload)

        # defining custom header
        headers = {
            "Content-Type": "application/xml"
        }

        url = self.base_mount_url + str(pnf_id)
        self.log.info("sending mount request, url: {}", url)
        web_client_service = self.restClientService(self.server_identifier)
        web_client_service.exchangeResource("PUT", url, mount_payload, headers)

    def unmount(self, pnf_id):
        url = self.base_mount_url + str(pnf_id)
        self.log.info("sending unmount request, url: {}", url)
        web_client_service = self.restClientService(self.server_identifier)
        web_client_service.exchangeResource("DELETE", url, "")

    def apply_configuration(self, pnf_id, resolution_key, template_name):
        self.log.info("Retrieving configlet from database (resolution-key: {}, template_name: {}",
                      resolution_key, template_name)
        configlet = self.resolveFromDatabase(resolution_key, template_name)
        self.log.info("Configlet: {}", configlet)

        # defining custom header
        headers = {
            "Content-Type": "application/yang.patch+json"
        }

        url = "restconf/config/network-topology:network-topology/topology/topology-netconf/node/" + pnf_id \
              + "/yang-ext:mount/mynetconf:netconflist"
        self.log.info("sending patch request,  url: {}", url)
        web_client_service = self.restClientService(self.server_identifier)
        result = web_client_service.exchangeResource("PATCH", url, configlet, headers)
        self.log.info("Configuration application result: {}", result)

    def recover(self, runtime_exception, execution_request):
        self.log.info("Recover method, no code to execute")
        return None
