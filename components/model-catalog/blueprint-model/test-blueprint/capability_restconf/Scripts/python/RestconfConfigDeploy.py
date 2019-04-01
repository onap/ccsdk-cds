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

from org.onap.ccsdk.cds.blueprintsprocessor.functions.restconf.executor import \
    RestconfComponentFunction
from java.lang import Exception as JavaException

from restconf_client import RestconfClient


class RestconfConfigDeploy(RestconfComponentFunction):

    log = globals()["log"]
    configlet_template_name = "config-assign"
    configlet_resource_path = "/yang-ext:mount/mynetconf:netconflist"
    restconf_server_identifier = "sdncodl"

    def process(self, execution_request):
        self.log.info("Started execution of process method")
        try:
            restconf_client = RestconfClient(self.log, self)
            pnf_id, resolution_key = self.retrieve_parameters(execution_request)
            web_client_service = self.restClientService(self.restconf_server_identifier)

            try:
                # mount the device
                mount_payload = self.resolveAndGenerateMessage("config-deploy-mapping", "config-deploy-template")
                restconf_client.mount_device(web_client_service, pnf_id, mount_payload)

                # log the current configuration subtree
                current_configuration = restconf_client.retrieve_device_configuration_subtree(
                    web_client_service, pnf_id, self.configlet_resource_path)
                self.log.info("Current configuration subtree: {}", current_configuration)

                # apply configuration
                configlet = self.resolveFromDatabase(resolution_key, self.configlet_template_name)
                restconf_client.configure_device_json_patch(
                    web_client_service, pnf_id, self.configlet_resource_path, configlet)
            except Exception, err:
                self.log.error("an error occurred while configuring device {}", err)
                raise err
            finally:
                restconf_client.unmount_device(web_client_service, pnf_id)

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

    def recover(self, runtime_exception, execution_request):
        self.log.info("Recover function called!")
        self.log.info("Execution request", execution_request)
        self.log.error("Exception", runtime_exception)
        print self.bluePrintRuntimeService.getBluePrintError().addError(runtime_exception.getMessage())
        return None
