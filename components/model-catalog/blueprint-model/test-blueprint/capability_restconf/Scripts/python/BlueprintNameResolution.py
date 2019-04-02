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

from abstract_ra_processor import AbstractRAProcessor
from java.lang import Exception as JavaException


class BlueprintNameResolution(AbstractRAProcessor):

    log = globals()["log"]

    def process(self, resource_assignment):
        self.log.info("Blueprint Name Resolution - start")
        self.log.info("resource_assignment: {}", resource_assignment)

        # how to get an instance of execution_request or similar here?

        self.set_resource_data_value(resource_assignment, "my blueprint name")

        return None

    def recover(self, runtime_exception, resource_assignment):
        self.log.error("Exception in the script {}", runtime_exception)
        print self.addError(runtime_exception.cause.message)
        return None
