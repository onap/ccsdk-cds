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

from org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor import ResourceAssignmentProcessor
from org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils import ResourceAssignmentUtils
from java.lang import Exception as JavaException

class Sid(ResourceAssignmentProcessor):

    def process(self, resource_assignment):
        try:
            loopback_ip = self.raRuntimeService.getStringFromResolutionStore("address")
            ip = loopback_ip.split("/")[0]
            net_id = self.compute_net_id(ip)
            ResourceAssignmentUtils.Companion.setResourceDataValue(resource_assignment, self.raRuntimeService, net_id)
        except JavaException, err:
          log.error("Java Exception in the script {}", err)
          ResourceAssignmentUtils.Companion.setFailedResourceDataValue(resource_assignment, err.message)
        except Exception, err:
          log.error("Python Exception in the script {}", err)
          ResourceAssignmentUtils.Companion.setFailedResourceDataValue(resource_assignment, err.message)
        return None

    def recover(self, runtime_exception, resource_assignment):
        print "NoOp"
        return None

    def compute_net_id(self, loopback_ip):
        # FIXME use proper SRMS
        srms = 10512

        sr_pfx_sid = 0

        octet1, octet2, octet3, octet4 = loopback_ip.split(".")
        srch_loop = octet1+"."+octet2+"."+octet3+".0"

        sr_pfx_sid = int(octet4) + srms
        print("SR PREFIX SID: %s" %sr_pfx_sid)

        octet1_l = len(octet1)
        octet2_l = len(octet2)
        octet3_l = len(octet3)
        octet4_l = len(octet4)

        isis_net_id = "49.0000."

        if octet1_l < 3:
          isis_net_id = isis_net_id + "0" + octet1
        else:
          isis_net_id = isis_net_id + octet1

        if octet2_l == 2 :
          isis_net_id = isis_net_id + "0" + "." + octet2[0] + octet2[1]
        elif octet2_l == 1:
          isis_net_id = isis_net_id + "00" + "." + octet2[0]
        else:
          isis_net_id = isis_net_id + octet2[0] + "." + octet2[1] + octet2[2]

        if octet3_l == 2:
          isis_net_id = isis_net_id + "0" + octet3[0] + "." + octet3[1]
        elif octet3_l == 1:
          isis_net_id = isis_net_id + "00" + "." + octet3[0]
        else:
          isis_net_id = isis_net_id + octet3[0] + octet3[1] + "." + octet3[2]

        if octet4_l == 2:
          isis_net_id = isis_net_id + "0" + octet4[0] + octet4[1] + ".00"
        elif octet4_l == 1:
          isis_net_id = isis_net_id + "00" + octet4[0] + ".00"
        else:
          isis_net_id = isis_net_id + octet4[0] + octet4[1] + octet4[2] + ".00"

        print("ISIS NET ID: %s" % isis_net_id)
        return isis_net_id