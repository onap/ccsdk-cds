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
Tests for blueprints_grpc/blueprint_processing_server.py —
AbstractScriptFunction and BluePrintProcessingServer.
"""

from unittest.mock import MagicMock, patch

import pytest

from google.protobuf import struct_pb2
from proto.BluePrintProcessing_pb2 import (
    ExecutionServiceInput,
    ExecutionServiceOutput,
)
from proto.BluePrintCommon_pb2 import (
    CommonHeader,
    ActionIdentifiers,
)

from blueprints_grpc.blueprint_processing_server import (
    AbstractScriptFunction,
    BluePrintProcessingServer,
)
from blueprints_grpc.script_executor_configuration import ScriptExecutorConfiguration


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_input(bp_name="test-bp", bp_version="1.0.0", action_name="TestAction",
                request_id="req-1"):
    inp = ExecutionServiceInput()
    inp.commonHeader.requestId = request_id
    inp.commonHeader.subRequestId = "sub-1"
    inp.commonHeader.originatorId = "CDS"
    inp.actionIdentifiers.blueprintName = bp_name
    inp.actionIdentifiers.blueprintVersion = bp_version
    inp.actionIdentifiers.actionName = action_name
    inp.payload.update({"key": "value"})
    return inp


# ---------------------------------------------------------------------------
# AbstractScriptFunction
# ---------------------------------------------------------------------------

class TestAbstractScriptFunction:

    def test_set_context(self):
        func = AbstractScriptFunction()
        mock_ctx = MagicMock()
        func.set_context(mock_ctx)
        assert func.context is mock_ctx

    def test_process_returns_none(self):
        func = AbstractScriptFunction()
        assert func.process(MagicMock()) is None

    def test_recover_returns_none(self):
        func = AbstractScriptFunction()
        assert func.recover(RuntimeError("test"), MagicMock()) is None


# ---------------------------------------------------------------------------
# BluePrintProcessingServer
# ---------------------------------------------------------------------------

class TestBluePrintProcessingServer:

    @pytest.fixture
    def config(self, tmp_path):
        config_file = tmp_path / "config.ini"
        config_file.write_text(
            "[blueprintsprocessor]\nblueprintDeployPath=/opt/blueprints\n"
            "[scriptExecutor]\nport=50052\n"
        )
        return ScriptExecutorConfiguration(str(config_file))

    def test_init_stores_configuration(self, config):
        server = BluePrintProcessingServer(config)
        assert server.configuration is config

    def test_init_creates_logger(self, config):
        server = BluePrintProcessingServer(config)
        assert server.logger is not None
        assert server.logger.name == "BluePrintProcessingServer"

    @patch("blueprints_grpc.blueprint_processing_server.instance_for_input")
    def test_process_yields_from_instance(self, mock_instance_for_input, config):
        """Verify process iterates over requests, creates instances, and yields responses."""
        # Create a mock script function instance that yields two responses
        mock_script = MagicMock(spec=AbstractScriptFunction)
        response1 = ExecutionServiceOutput()
        response1.commonHeader.requestId = "resp-1"
        response2 = ExecutionServiceOutput()
        response2.commonHeader.requestId = "resp-2"
        mock_script.process.return_value = iter([response1, response2])
        mock_instance_for_input.return_value = mock_script

        server = BluePrintProcessingServer(config)
        request = _make_input()
        context = MagicMock()

        responses = list(server.process(iter([request]), context))

        mock_instance_for_input.assert_called_once_with(config, request)
        mock_script.set_context.assert_called_once_with(context)
        mock_script.process.assert_called_once_with(request)
        assert len(responses) == 2
        assert responses[0].commonHeader.requestId == "resp-1"
        assert responses[1].commonHeader.requestId == "resp-2"

    @patch("blueprints_grpc.blueprint_processing_server.instance_for_input")
    def test_process_handles_multiple_requests(self, mock_instance_for_input, config):
        """Multiple requests should each get their own instance."""
        mock_script1 = MagicMock(spec=AbstractScriptFunction)
        resp1 = ExecutionServiceOutput()
        resp1.commonHeader.requestId = "r1"
        mock_script1.process.return_value = iter([resp1])

        mock_script2 = MagicMock(spec=AbstractScriptFunction)
        resp2 = ExecutionServiceOutput()
        resp2.commonHeader.requestId = "r2"
        mock_script2.process.return_value = iter([resp2])

        mock_instance_for_input.side_effect = [mock_script1, mock_script2]

        server = BluePrintProcessingServer(config)
        req1 = _make_input(request_id="req-1")
        req2 = _make_input(request_id="req-2")
        context = MagicMock()

        responses = list(server.process(iter([req1, req2]), context))

        assert len(responses) == 2
        assert mock_instance_for_input.call_count == 2

    @patch("blueprints_grpc.blueprint_processing_server.instance_for_input")
    def test_process_empty_iterator(self, mock_instance_for_input, config):
        """An empty request iterator should yield no responses."""
        server = BluePrintProcessingServer(config)
        context = MagicMock()

        responses = list(server.process(iter([]), context))

        assert responses == []
        mock_instance_for_input.assert_not_called()
