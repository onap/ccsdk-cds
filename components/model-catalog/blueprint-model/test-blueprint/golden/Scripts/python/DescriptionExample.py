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

from abstract_ra_processor import AbstractRAProcessor
from blueprint_constants import *
from java.lang import Exception as JavaException

class DescriptionExample(AbstractRAProcessor):

    def process(self, resource_assignment):
        # get dependencies result
        value = self.raRuntimeService.getStringFromResolutionStore("vf-module-type")
        
        # logic based on dependency outcome
        result = ""
        if value == "vfw":
            result = "This is the Virtual Firewall entity"
        elif value == "vsn":
            result = "This is the Virtual Sink entity"
        elif value == "vpg":
            result = "This is the Virtual Packet Generator"

        # set value for resource getting currently resolved
        self.set_resource_data_value(resource_assignment, result)

        return None

    def recover(self, runtime_exception, resource_assignment):
        log.error("Exception in the script {}", runtime_exception)
        print self.addError(runtime_exception.cause.message)
        return None
