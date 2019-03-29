#  Copyright (c) 2019 Bell Canada.
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

import netconf_constant
from common import ResolutionHelper
from netconfclient import NetconfClient
from org.onap.ccsdk.cds.blueprintsprocessor.functions.netconf.executor import \
  NetconfComponentFunction


class NetconfRpcExample(NetconfComponentFunction):

  def process(self, execution_request):
      log = globals()[netconf_constant.SERVICE_LOG]
      print(globals())

      nc = NetconfClient(log, self, "netconf-connection")
      rr = ResolutionHelper(self)

      # Get meshed template from DB
      resolution_key = self.getDynamicProperties("resolution-key").asText()
      artifact_name = "hostname"
      payloadHostname = rr.retrieve_resolved_template_from_database(resolution_key, artifact_name)

      # resolve param and mesh template
      payloadInterface = rr.resolve_and_generate_message_from_template_prefix("vfw-interface")

      nc.connect()
      nc.lock()
      #if not response.isSuccess():
      #  og.error(response.errorMessage)
      nc.discard_change()
      nc.edit_config(message_content=payloadInterface, edit_default_peration="none")
      nc.edit_config(message_content=payloadHostname, edit_default_peration="none")
      nc.validate()
      nc.commit()
      #nc.commit(confirmed = True, confirm_timeout=15)
      nc.unlock()
      nc.disconnect()

  def recover(self, runtime_exception, execution_request):
        log.error("Exception in the script {}", runtime_exception)
        print self.addError(runtime_exception.cause.message)
        return None
