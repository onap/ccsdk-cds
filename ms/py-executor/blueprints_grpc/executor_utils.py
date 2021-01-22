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

import datetime
import importlib
import importlib.util
import json
import logging
import sys
import time

from google.protobuf import json_format, struct_pb2
from google.protobuf.timestamp_pb2 import Timestamp
from proto.BlueprintCommon_pb2 import (
    EVENT_COMPONENT_EXECUTED,
    EVENT_COMPONENT_NOTIFICATION,
    EVENT_COMPONENT_PROCESSING,
    EVENT_COMPONENT_TRACE,
    Status
)
from proto.BlueprintProcessing_pb2 import (
    ExecutionServiceInput,
    ExecutionServiceOutput,
)

from .script_executor_configuration import ScriptExecutorConfiguration

logger = logging.getLogger("Utils")


def current_time():
    ts = time.time()
    return datetime.datetime.fromtimestamp(ts).strftime("%Y-%m-%dT%H:%M:%S.%fZ")


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


def log_response(input: ExecutionServiceInput, message: str):
    payload = struct_pb2.Struct()
    payload['message'] = message
    status = Status()
    status.timestamp = current_time()
    status.eventType = EVENT_COMPONENT_TRACE
    return ExecutionServiceOutput(commonHeader=input.commonHeader,
                                  actionIdentifiers=input.actionIdentifiers,
                                  payload=payload, status=status)


def send_notification(input: ExecutionServiceInput, message: str):
    payload = struct_pb2.Struct()
    payload['message'] = message
    status = Status()
    status.timestamp = current_time()
    status.eventType = EVENT_COMPONENT_NOTIFICATION
    return ExecutionServiceOutput(commonHeader=input.commonHeader,
                                  actionIdentifiers=input.actionIdentifiers,
                                  payload=payload, status=status)


def ack_response(input: ExecutionServiceInput):
    timestamp = Timestamp()
    timestamp.GetCurrentTime()
    response_common_header = input.commonHeader
    status = Status()
    status.timestamp = current_time()
    status.eventType = EVENT_COMPONENT_PROCESSING
    return ExecutionServiceOutput(commonHeader=response_common_header,
                                  actionIdentifiers=input.actionIdentifiers,
                                  status=status)


def success_response(input: ExecutionServiceInput, property_json: json, code: int):
    timestamp = Timestamp()
    timestamp.GetCurrentTime()
    status = Status()
    status.timestamp = current_time()
    status.eventType = EVENT_COMPONENT_EXECUTED
    status.code = code
    status.message = 'success'
    payload_struct = create_response_payload_from_json(input.actionIdentifiers.actionName, property_json)
    return ExecutionServiceOutput(commonHeader=input.commonHeader,
                                  actionIdentifiers=input.actionIdentifiers, status=status, payload=payload_struct)


def failure_response(input: ExecutionServiceInput, property_json: json, error_code: int,
                     error_message: str):
    timestamp = Timestamp()
    timestamp.GetCurrentTime()
    status = Status()
    status.timestamp = current_time()
    status.eventType = EVENT_COMPONENT_EXECUTED
    status.code = error_code
    status.message = 'failure'
    payload_struct = create_response_payload_from_json(input.actionIdentifiers.actionName, property_json)
    status.errorMessage = error_message
    return ExecutionServiceOutput(commonHeader=input.commonHeader,
                                  actionIdentifiers=input.actionIdentifiers, status=status, payload=payload_struct)


def create_response_payload_from_json(action_name, property_json: json):
    # Create response Pay load json from property Json
    payload_key = action_name + '-response'
    response_payload = {}
    response_payload[payload_key] = property_json
    response_payload_json = json.dumps(response_payload)
    # Create Struct from Json
    payload_struct = struct_pb2.Struct()
    json_format.Parse(str(response_payload_json), payload_struct, ignore_unknown_fields=True)
    return payload_struct
