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

from google.protobuf.timestamp_pb2 import Timestamp
from google.protobuf import struct_pb2
import sys, importlib, importlib.util
import logging
from .proto.BluePrintProcessing_pb2 import ExecutionServiceInput, ExecutionServiceOutput
from .proto.BluePrintCommon_pb2 import Status, EVENT_COMPONENT_TRACE, EVENT_COMPONENT_PROCESSING, \
    EVENT_COMPONENT_EXECUTED
from .script_executor_configuration import ScriptExecutorConfiguration

logger = logging.getLogger("Utils")


def blueprint_id(input: ExecutionServiceInput):
    blueprint_name = input.actionIdentifiers.blueprintName
    blueprint_version = input.actionIdentifiers.blueprintVersion
    return blueprint_name + '/' + blueprint_version


def blueprint_location(config: ScriptExecutorConfiguration, input: ExecutionServiceInput):
    blueprint_name = input.actionIdentifiers.blueprintName
    blueprint_version = input.actionIdentifiers.blueprintVersion
    return config.blueprints_processor('blueprintDeployPath') + '/' + blueprint_name + '/' + blueprint_version


def instance_for_input(config: ScriptExecutorConfiguration, input: ExecutionServiceInput):
    blueprint_name = input.actionIdentifiers.blueprintName
    blueprint_version = input.actionIdentifiers.blueprintVersion
    action_name = input.actionIdentifiers.actionName
    # Get Blueprint python script location
    script_location = blueprint_location(config, input) + '/' + 'Scripts/python/__init__.py'
    logger.info(script_location)

    # Create Dynamic Module Name
    module_name = blueprint_name + '-' + blueprint_version
    spec = importlib.util.spec_from_file_location(module_name, script_location)
    logger.info(spec)
    dynamic_module = importlib.util.module_from_spec(spec)
    # Add blueprint modules
    sys.modules[spec.name] = dynamic_module
    spec.loader.exec_module(dynamic_module)
    script_clazz = getattr(dynamic_module, action_name)
    return script_clazz()


def log_response(input: ExecutionServiceInput, message):
    payload = struct_pb2.Struct()
    payload['message'] = message
    status = Status()
    status.eventType = EVENT_COMPONENT_TRACE
    return ExecutionServiceOutput(commonHeader=input.commonHeader,
                                  actionIdentifiers=input.actionIdentifiers,
                                  payload=payload, status=status)


def ack_response(input: ExecutionServiceInput):
    timestamp = Timestamp()
    timestamp.GetCurrentTime()
    response_common_header = input.commonHeader
    status = Status()
    status.eventType = EVENT_COMPONENT_PROCESSING
    return ExecutionServiceOutput(commonHeader=response_common_header,
                                  actionIdentifiers=input.actionIdentifiers,
                                  status=status)


def build_response(input: ExecutionServiceInput, results, is_success=True):
    timestamp = Timestamp()
    timestamp.GetCurrentTime()
    status = Status()
    status.eventType = EVENT_COMPONENT_EXECUTED
    return ExecutionServiceOutput(commonHeader=input.commonHeader,
                                  actionIdentifiers=input.actionIdentifiers, status=status)
