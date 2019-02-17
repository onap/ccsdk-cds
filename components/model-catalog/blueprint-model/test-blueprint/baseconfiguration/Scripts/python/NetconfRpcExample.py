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
from java.lang import Exception as JavaException
from org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor import \
  NetconfComponentFunction


class NetconfRpcExample(NetconfComponentFunction):

  def process(self, execution_request):
    try:
      log = globals()[netconf_constant.SERVICE_LOG]
      print(globals())
      nc = NetconfClient(log, self, "netconf-connection")
      rr = ResolutionHelper(self)

      payload = rr.resolve_and_generate_message_from_template_prefix("hostname")

      nc.connect()
      response = nc.lock(message_id="lock-123")
      if not response.isSuccess():
        log.error(response.errorMessage)

      nc.edit_config(message_id="edit-config-1", message_content=payload,
                     edit_default_peration="none")
      # nc.validate(message_id="validate-123")
      # nc.discard_change(message_id="discard-123")
      nc.validate(message_id="validate-123")
      nc.commit(message_id="commit-123")
      nc.unlock(message_id="unlock-123")
      nc.disconnect()

    except JavaException, err:
      log.error("Java Exception in the script {}", err)
    except Exception, err:
      log.error("Python Exception in the script {}", err)

  def recover(self, runtime_exception, execution_request):
    print "Recovering calling.." + PROPERTY_BLUEPRINT_BASE_PATH
    return None
