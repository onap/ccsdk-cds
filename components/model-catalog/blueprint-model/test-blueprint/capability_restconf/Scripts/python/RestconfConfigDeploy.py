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
# ============LICENSE_END=========================================================

from time import sleep

from org.onap.ccsdk.apps.blueprintsprocessor.functions.restconf.executor import \
    RestconfComponentFunction
from java.lang import Exception as JavaException


class RestconfConfigDeploy(RestconfComponentFunction):

    log = globals()["log"]
    odl_status_check_limit = 10
    odl_status_check_pause = 1
    odl_status_check_url = "restconf/operational/network-topology:network-topology/topology/topology-netconf/node/"
    base_odl_url = "restconf/config/network-topology:network-topology/topology/topology-netconf/node/"
    server_identifier = "sdncodl"
    configlet_template_name = "config-assign"
    configlet_odl_resource = "/yang-ext:mount/mynetconf:netconflist"

    def process(self, execution_request):

        self.log.info("Started execution of process method")
        try:
            pnf_id, resolution_key = self.retrieve_parameters(execution_request)
            self.interact_with_odl(pnf_id, resolution_key)
        except JavaException, err:
            self.log.error("Java Exception in the script", err)
            raise err
        except Exception, err:
            self.log.error("Python Exception in the script:" + str(err), err)
            raise err
        self.log.info("Ended execution of process method")

    def retrieve_parameters(self, execution_request):
        resolution_key = self.getDynamicProperties("resolution-key").asText()
        self.log.info("resolution_key: {}", resolution_key)
        pnf_id = execution_request.payload.get("config-deploy-request").get("config-deploy-properties").get("pnf-id")
        pnf_id = str(pnf_id).strip('\"')
        self.log.info("pnf-id: {}", pnf_id)
        return pnf_id, resolution_key

    def interact_with_odl(self, pnf_id, resolution_key):
        try:
            self.mount(pnf_id)
            self.log_current_configlet(pnf_id)
            self.apply_configuration(pnf_id, resolution_key, self.configlet_template_name)
        except Exception, err:
            self.log.error("an error occurred while configuring device {}", err)
            raise err
        finally:
            self.log.info("unmounting device {}", pnf_id)
            self.unmount(pnf_id)

    def mount(self, pnf_id):
        self.log.info("mounting device {}", pnf_id)
        mount_payload = self.resolveAndGenerateMessage("config-deploy-mapping", "config-deploy-template")
        self.log.info("mount payload: \n {}", mount_payload)
        headers = {"Content-Type": "application/xml"}  # defining custom header
        url = self.base_odl_url + str(pnf_id)
        self.log.info("sending mount request, url: {}", url)
        web_client_service = self.restClientService(self.server_identifier)
        web_client_service.exchangeResource("PUT", url, mount_payload, headers)
        self.wait_for_odl_to_mount(pnf_id)

    def wait_for_odl_to_mount(self, pnf_id):
        counter = 0
        url = self.odl_status_check_url + pnf_id
        self.log.info("url for ODL status check: {}", url)
        web_client_service = self.restClientService(self.server_identifier)
        expected_result = '"netconf-node-topology:connection-status":"connected"'
        while counter < self.odl_status_check_limit:
            result = web_client_service.exchangeResource("GET", url, "")
            self.log.info("ODL status check result: {}", result)
            if expected_result in result:
                self.log.info("PNF was mounted successfully on ODL")
                return None
            sleep(1)
            counter += 1
        raise JavaException("PNF was not mounted on ODL, aborting configuration procedure")

    def log_current_configlet(self, pnf_id):
        self.log.info("retrieving configuration for device {}", pnf_id)
        url = self.base_odl_url + pnf_id + self.configlet_odl_resource
        self.log.info("sending GET request,  url: {}", url)
        web_client_service = self.restClientService(self.server_identifier)
        result = web_client_service.exchangeResource("GET", url, "")
        self.log.info("Current configuration: {}", result)

    def apply_configuration(self, pnf_id, resolution_key, template_name):
        self.log.info("configuring device {}", pnf_id)
        self.log.info("Retrieving configlet from database (resolution-key: {}, template_name: {}",
                      resolution_key, template_name)
        configlet = self.resolveFromDatabase(resolution_key, template_name)
        self.log.info("Configlet: {}", configlet)
        headers = { "Content-Type": "application/yang.patch+json" }  # defining custom header
        url = self.base_odl_url + pnf_id + self.configlet_odl_resource
        self.log.info("sending patch request,  url: {}", url)
        web_client_service = self.restClientService(self.server_identifier)
        result = web_client_service.exchangeResource("PATCH", url, configlet, headers)
        self.log.info("Configuration application result: {}", result)

    def unmount(self, pnf_id):
        url = self.base_odl_url + str(pnf_id)
        self.log.info("sending unmount request, url: {}", url)
        web_client_service = self.restClientService(self.server_identifier)
        web_client_service.exchangeResource("DELETE", url, "")

    def recover(self, runtime_exception, execution_request):
        self.log.info("Recover function called!")
        self.log.error(runtime_exception.getMessage())
        print self.bluePrintRuntimeService.getBluePrintError().addError(runtime_exception.getMessage())
        return None