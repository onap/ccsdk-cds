#!/usr/bin/python
#
#  Copyright © 2018-2019 AT&T Intellectual Property.
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

import logging
from google.protobuf.json_format import MessageToJson
from proto import BlueprintProcessing_pb2_grpc as BlueprintProcessing_pb2_grpc
from .script_executor_configuration import ScriptExecutorConfiguration
from .executor_utils import instance_for_input


class AbstractScriptFunction:

    def set_context(self, context):
        self.context = context

    def process(self, request):
        pass

    def recover(self, runtime_exception, execution_request):
        pass


class BlueprintProcessingServer(BlueprintProcessing_pb2_grpc.BlueprintProcessingServiceServicer):

    def __init__(self, configuration: ScriptExecutorConfiguration):
        self.logger = logging.getLogger(self.__class__.__name__)
        self.configuration = configuration

    def process(self, request_iterator, context):
        for request in request_iterator:
            jsonObj = MessageToJson(request.payload)
            self.logger.info(jsonObj)
            # Get the Dynamic Process Instance based on request
            instance: AbstractScriptFunction = instance_for_input(self.configuration, request)
            instance.set_context(context)
            yield from instance.process(request)
