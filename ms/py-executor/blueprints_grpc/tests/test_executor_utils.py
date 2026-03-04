#
# Copyright (C) 2026 Deutsche Telekom.
#
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
#

"""
Tests for blueprints_grpc/executor_utils.py — utility functions for building
gRPC responses, loading dynamic script instances, and working with protobuf
messages.
"""

import json
import re
import tempfile
import os
from unittest.mock import patch, MagicMock

import pytest

from proto.BluePrintCommon_pb2 import (
    EVENT_COMPONENT_EXECUTED,
    EVENT_COMPONENT_NOTIFICATION,
    EVENT_COMPONENT_PROCESSING,
    EVENT_COMPONENT_TRACE,
    ActionIdentifiers,
    CommonHeader,
)
from proto.BluePrintProcessing_pb2 import (
    ExecutionServiceInput,
    ExecutionServiceOutput,
)
from google.protobuf import json_format

from blueprints_grpc.executor_utils import (
    current_time,
    blueprint_id,
    blueprint_location,
    instance_for_input,
    log_response,
    send_notification,
    ack_response,
    success_response,
    failure_response,
    create_response_payload_from_json,
)
from blueprints_grpc.script_executor_configuration import ScriptExecutorConfiguration


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_input(bp_name="test-bp", bp_version="1.0.0", action_name="TestAction",
                request_id="req-1", sub_request_id="sub-1", originator_id="CDS"):
    """Create a properly populated ExecutionServiceInput."""
    inp = ExecutionServiceInput()
    inp.commonHeader.requestId = request_id
    inp.commonHeader.subRequestId = sub_request_id
    inp.commonHeader.originatorId = originator_id
    inp.actionIdentifiers.blueprintName = bp_name
    inp.actionIdentifiers.blueprintVersion = bp_version
    inp.actionIdentifiers.actionName = action_name
    return inp


# ---------------------------------------------------------------------------
# current_time
# ---------------------------------------------------------------------------

class TestCurrentTime:

    def test_returns_string(self):
        result = current_time()
        assert isinstance(result, str)

    def test_format_is_iso_like(self):
        result = current_time()
        # Pattern: YYYY-MM-DDTHH:MM:SS.ffffffZ
        assert re.match(r"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+Z", result)


# ---------------------------------------------------------------------------
# blueprint_id
# ---------------------------------------------------------------------------

class TestBlueprintId:

    def test_returns_name_slash_version(self):
        inp = _make_input(bp_name="my-bp", bp_version="2.0.0")
        assert blueprint_id(inp) == "my-bp/2.0.0"

    def test_different_values(self):
        inp = _make_input(bp_name="other", bp_version="3.1.0")
        assert blueprint_id(inp) == "other/3.1.0"


# ---------------------------------------------------------------------------
# blueprint_location
# ---------------------------------------------------------------------------

class TestBlueprintLocation:

    def test_returns_deploy_path_plus_name_version(self, tmp_path):
        config_file = tmp_path / "config.ini"
        config_file.write_text(
            "[blueprintsprocessor]\nblueprintDeployPath=/opt/blueprints\n"
            "[scriptExecutor]\nport=50052\n"
        )
        config = ScriptExecutorConfiguration(str(config_file))
        inp = _make_input(bp_name="test-bp", bp_version="1.0.0")

        result = blueprint_location(config, inp)
        assert result == "/opt/blueprints/test-bp/1.0.0"


# ---------------------------------------------------------------------------
# instance_for_input
# ---------------------------------------------------------------------------

class TestInstanceForInput:

    def test_loads_class_from_sample_cba(self):
        """Use the sample-cba test resource to verify dynamic module loading."""
        # The sample CBA is at test/resources/sample-cba/1.0.0/Scripts/python/__init__.py
        # and the action class is SampleScript
        test_resources_dir = os.path.join(
            os.path.dirname(os.path.dirname(os.path.dirname(__file__))),
            "test", "resources"
        )

        config_file = os.path.join(
            os.path.dirname(os.path.dirname(os.path.dirname(__file__))),
            "..", "configuration-local.ini"
        )

        if not os.path.exists(config_file):
            pytest.skip("configuration-local.ini not found")

        config = ScriptExecutorConfiguration(config_file)
        inp = _make_input(bp_name="sample-cba", bp_version="1.0.0", action_name="SampleScript")

        instance = instance_for_input(config, inp)
        # It should have the process method from AbstractScriptFunction subclass
        assert hasattr(instance, "process")
        assert instance.__class__.__name__ == "SampleScript"

    def test_raises_on_missing_module(self, tmp_path):
        """If the script file doesn't exist, loading should fail."""
        config_file = tmp_path / "config.ini"
        config_file.write_text(
            f"[blueprintsprocessor]\nblueprintDeployPath={tmp_path}\n"
            "[scriptExecutor]\nport=50052\n"
        )
        config = ScriptExecutorConfiguration(str(config_file))
        inp = _make_input(bp_name="nonexistent", bp_version="0.0.0", action_name="Missing")

        with pytest.raises((FileNotFoundError, ModuleNotFoundError)):
            instance_for_input(config, inp)

    def test_raises_on_missing_action_class(self, tmp_path):
        """If the script file exists but the action class doesn't, should raise AttributeError."""
        bp_dir = tmp_path / "mybp" / "1.0.0" / "Scripts" / "python"
        bp_dir.mkdir(parents=True)
        (bp_dir / "__init__.py").write_text("class Exists:\n    pass\n")

        config_file = tmp_path / "config.ini"
        config_file.write_text(
            f"[blueprintsprocessor]\nblueprintDeployPath={tmp_path}\n"
            "[scriptExecutor]\nport=50052\n"
        )
        config = ScriptExecutorConfiguration(str(config_file))
        inp = _make_input(bp_name="mybp", bp_version="1.0.0", action_name="NonExistentClass")

        with pytest.raises(AttributeError):
            instance_for_input(config, inp)


# ---------------------------------------------------------------------------
# log_response
# ---------------------------------------------------------------------------

class TestLogResponse:

    def test_returns_execution_output(self):
        inp = _make_input()
        result = log_response(inp, "test message")
        assert isinstance(result, ExecutionServiceOutput)

    def test_event_type_is_trace(self):
        inp = _make_input()
        result = log_response(inp, "trace msg")
        assert result.status.eventType == EVENT_COMPONENT_TRACE

    def test_payload_contains_message(self):
        inp = _make_input()
        result = log_response(inp, "hello world")
        payload_dict = json_format.MessageToDict(result.payload)
        assert payload_dict["message"] == "hello world"

    def test_preserves_common_header(self):
        inp = _make_input(request_id="req-99")
        result = log_response(inp, "msg")
        assert result.commonHeader.requestId == "req-99"

    def test_preserves_action_identifiers(self):
        inp = _make_input(bp_name="bp1", action_name="Act1")
        result = log_response(inp, "msg")
        assert result.actionIdentifiers.blueprintName == "bp1"
        assert result.actionIdentifiers.actionName == "Act1"

    def test_timestamp_is_set(self):
        inp = _make_input()
        result = log_response(inp, "msg")
        assert result.status.timestamp != ""


# ---------------------------------------------------------------------------
# send_notification
# ---------------------------------------------------------------------------

class TestSendNotification:

    def test_returns_execution_output(self):
        inp = _make_input()
        result = send_notification(inp, "notification msg")
        assert isinstance(result, ExecutionServiceOutput)

    def test_event_type_is_notification(self):
        inp = _make_input()
        result = send_notification(inp, "notify")
        assert result.status.eventType == EVENT_COMPONENT_NOTIFICATION

    def test_payload_contains_message(self):
        inp = _make_input()
        result = send_notification(inp, "alert!")
        payload_dict = json_format.MessageToDict(result.payload)
        assert payload_dict["message"] == "alert!"


# ---------------------------------------------------------------------------
# ack_response
# ---------------------------------------------------------------------------

class TestAckResponse:

    def test_returns_execution_output(self):
        inp = _make_input()
        result = ack_response(inp)
        assert isinstance(result, ExecutionServiceOutput)

    def test_event_type_is_processing(self):
        inp = _make_input()
        result = ack_response(inp)
        assert result.status.eventType == EVENT_COMPONENT_PROCESSING

    def test_preserves_action_identifiers(self):
        inp = _make_input(bp_name="ack-bp", bp_version="2.0.0")
        result = ack_response(inp)
        assert result.actionIdentifiers.blueprintName == "ack-bp"
        assert result.actionIdentifiers.blueprintVersion == "2.0.0"

    def test_timestamp_is_set(self):
        inp = _make_input()
        result = ack_response(inp)
        assert result.status.timestamp != ""


# ---------------------------------------------------------------------------
# success_response
# ---------------------------------------------------------------------------

class TestSuccessResponse:

    def test_returns_execution_output(self):
        inp = _make_input()
        result = success_response(inp, {"key": "val"}, 200)
        assert isinstance(result, ExecutionServiceOutput)

    def test_event_type_is_executed(self):
        inp = _make_input()
        result = success_response(inp, {}, 200)
        assert result.status.eventType == EVENT_COMPONENT_EXECUTED

    def test_status_code(self):
        inp = _make_input()
        result = success_response(inp, {}, 200)
        assert result.status.code == 200

    def test_status_message_is_success(self):
        inp = _make_input()
        result = success_response(inp, {}, 200)
        assert result.status.message == "success"

    def test_payload_contains_action_response(self):
        inp = _make_input(action_name="MyAction")
        result = success_response(inp, {"result": "ok"}, 200)
        payload_dict = json_format.MessageToDict(result.payload)
        assert "MyAction-response" in payload_dict
        assert payload_dict["MyAction-response"]["result"] == "ok"

    def test_custom_status_code(self):
        inp = _make_input()
        result = success_response(inp, {}, 201)
        assert result.status.code == 201


# ---------------------------------------------------------------------------
# failure_response
# ---------------------------------------------------------------------------

class TestFailureResponse:

    def test_returns_execution_output(self):
        inp = _make_input()
        result = failure_response(inp, {}, 500, "it broke")
        assert isinstance(result, ExecutionServiceOutput)

    def test_event_type_is_executed(self):
        inp = _make_input()
        result = failure_response(inp, {}, 500, "error")
        assert result.status.eventType == EVENT_COMPONENT_EXECUTED

    def test_status_code(self):
        inp = _make_input()
        result = failure_response(inp, {}, 503, "unavailable")
        assert result.status.code == 503

    def test_status_message_is_failure(self):
        inp = _make_input()
        result = failure_response(inp, {}, 500, "bad")
        assert result.status.message == "failure"

    def test_error_message(self):
        inp = _make_input()
        result = failure_response(inp, {}, 500, "disk full")
        assert result.status.errorMessage == "disk full"

    def test_payload_contains_action_response(self):
        inp = _make_input(action_name="FailAction")
        result = failure_response(inp, {"err": "details"}, 500, "error")
        payload_dict = json_format.MessageToDict(result.payload)
        assert "FailAction-response" in payload_dict
        assert payload_dict["FailAction-response"]["err"] == "details"


# ---------------------------------------------------------------------------
# create_response_payload_from_json
# ---------------------------------------------------------------------------

class TestCreateResponsePayloadFromJson:

    def test_returns_struct(self):
        from google.protobuf import struct_pb2
        result = create_response_payload_from_json("action", {"key": "val"})
        assert isinstance(result, struct_pb2.Struct)

    def test_wraps_in_action_response_key(self):
        result = create_response_payload_from_json("DoThing", {"x": 1})
        result_dict = json_format.MessageToDict(result)
        assert "DoThing-response" in result_dict

    def test_empty_properties(self):
        result = create_response_payload_from_json("Empty", {})
        result_dict = json_format.MessageToDict(result)
        assert "Empty-response" in result_dict

    def test_nested_properties(self):
        props = {"outer": {"inner": "deep"}}
        result = create_response_payload_from_json("Nested", props)
        result_dict = json_format.MessageToDict(result)
        assert result_dict["Nested-response"]["outer"]["inner"] == "deep"
