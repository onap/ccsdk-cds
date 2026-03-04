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
Integration tests for command_executor_server.py — CommandExecutorServer gRPC servicer.

Includes:
  - Unit-level tests that mock CommandExecutorHandler to verify branching logic
  - Full gRPC integration tests that start a real server, register the servicer,
    and make real RPC calls through a channel/stub with mocked handler internals
"""

import io
import json
import logging
import os
import zipfile
from concurrent import futures
from types import SimpleNamespace
from unittest.mock import patch, MagicMock

import grpc
import pytest

import proto.CommandExecutor_pb2 as pb2
import proto.CommandExecutor_pb2_grpc as pb2_grpc
import utils
from command_executor_server import CommandExecutorServer


# ---------------------------------------------------------------------------
# Helpers — build real protobuf messages
# ---------------------------------------------------------------------------

def _identifiers(name="test-bp", version="1.0.0", uuid="uuid-123"):
    ids = pb2.Identifiers()
    ids.blueprintName = name
    ids.blueprintVersion = version
    ids.blueprintUUID = uuid
    return ids


def _upload_request(name="test-bp", version="1.0.0", uuid="uuid-123",
                    request_id="req-1", sub_request_id="sub-1",
                    originator_id="orig-1", archive_type="CBA_ZIP",
                    bin_data=b""):
    req = pb2.UploadBlueprintInput()
    req.identifiers.CopyFrom(_identifiers(name, version, uuid))
    req.requestId = request_id
    req.subRequestId = sub_request_id
    req.originatorId = originator_id
    req.archiveType = archive_type
    req.binData = bin_data
    return req


def _prepare_env_request(name="test-bp", version="1.0.0", uuid="uuid-123",
                         request_id="req-2", sub_request_id="sub-2",
                         originator_id="orig-1", timeout=30):
    req = pb2.PrepareEnvInput()
    req.identifiers.CopyFrom(_identifiers(name, version, uuid))
    req.requestId = request_id
    req.subRequestId = sub_request_id
    req.originatorId = originator_id
    req.timeOut = timeout
    return req


def _execution_request(name="test-bp", version="1.0.0", uuid="uuid-123",
                       request_id="req-3", sub_request_id="sub-3",
                       originator_id="orig-1", command="python test.py",
                       timeout=30):
    req = pb2.ExecutionInput()
    req.identifiers.CopyFrom(_identifiers(name, version, uuid))
    req.requestId = request_id
    req.subRequestId = sub_request_id
    req.originatorId = originator_id
    req.command = command
    req.timeOut = timeout
    return req


def _create_valid_zip_bytes():
    """Create a valid in-memory zip with a single entry."""
    buf = io.BytesIO()
    with zipfile.ZipFile(buf, 'w', zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("test.txt", "hello world")
    return buf.getvalue()


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture
def server_instance():
    """A plain CommandExecutorServer (no gRPC wiring)."""
    return CommandExecutorServer()


@pytest.fixture
def mock_context():
    """A MagicMock standing in for gRPC ServicerContext."""
    return MagicMock()


# ---------------------------------------------------------------------------
# Unit tests — mock CommandExecutorHandler to test server branch logic
# ---------------------------------------------------------------------------

class TestUploadBlueprint:
    """uploadBlueprint delegates to handler.uploadBlueprint and returns result."""

    @patch("command_executor_server.CommandExecutorHandler")
    def test_returns_handler_result(self, MockHandler, server_instance, mock_context):
        fake_response = pb2.UploadBlueprintOutput()
        fake_response.requestId = "req-1"
        fake_response.status = pb2.SUCCESS
        MockHandler.return_value.uploadBlueprint.return_value = fake_response

        req = _upload_request()
        result = server_instance.uploadBlueprint(req, mock_context)

        MockHandler.assert_called_once_with(req)
        MockHandler.return_value.uploadBlueprint.assert_called_once_with(req)
        assert result == fake_response

    @patch("command_executor_server.CommandExecutorHandler")
    def test_passes_request_to_handler(self, MockHandler, server_instance, mock_context):
        MockHandler.return_value.uploadBlueprint.return_value = pb2.UploadBlueprintOutput()

        req = _upload_request(name="my-bp", version="2.0.0", uuid="u-456")
        server_instance.uploadBlueprint(req, mock_context)

        # Handler was constructed with the exact request
        MockHandler.assert_called_once_with(req)


class TestPrepareEnv:
    """prepareEnv delegates to handler.prepare_env, logs, and builds grpc response."""

    @patch("command_executor_server.CommandExecutorHandler")
    def test_successful_prepare_env(self, MockHandler, server_instance, mock_context):
        MockHandler.return_value.prepare_env.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: True,
            utils.RESULTS_LOG_KEY: ["pkg installed"],
            utils.ERR_MSG_KEY: "",
        }

        req = _prepare_env_request(request_id="req-ok")
        result = server_instance.prepareEnv(req, mock_context)

        MockHandler.assert_called_once_with(req)
        MockHandler.return_value.prepare_env.assert_called_once_with(req)
        # Result is an ExecutionOutput
        assert isinstance(result, pb2.ExecutionOutput)
        assert result.requestId == "req-ok"
        assert result.status == pb2.SUCCESS

    @patch("command_executor_server.CommandExecutorHandler")
    def test_failed_prepare_env(self, MockHandler, server_instance, mock_context):
        MockHandler.return_value.prepare_env.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: False,
            utils.RESULTS_LOG_KEY: [],
            utils.ERR_MSG_KEY: "pip install failed",
        }

        req = _prepare_env_request(request_id="req-fail")
        result = server_instance.prepareEnv(req, mock_context)

        assert isinstance(result, pb2.ExecutionOutput)
        assert result.requestId == "req-fail"
        assert result.status == pb2.FAILURE

    @patch("command_executor_server.CommandExecutorHandler")
    def test_prepare_env_logs_success(self, MockHandler, server_instance, mock_context, caplog):
        MockHandler.return_value.prepare_env.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: True,
            utils.RESULTS_LOG_KEY: ["ok"],
            utils.ERR_MSG_KEY: "",
        }

        req = _prepare_env_request()
        with caplog.at_level(logging.INFO, logger="CommandExecutorServer"):
            server_instance.prepareEnv(req, mock_context)

        assert any("Package installation logs" in r.message for r in caplog.records)

    @patch("command_executor_server.CommandExecutorHandler")
    def test_prepare_env_logs_failure(self, MockHandler, server_instance, mock_context, caplog):
        MockHandler.return_value.prepare_env.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: False,
            utils.RESULTS_LOG_KEY: [],
            utils.ERR_MSG_KEY: "missing dep",
        }

        req = _prepare_env_request()
        with caplog.at_level(logging.INFO, logger="CommandExecutorServer"):
            server_instance.prepareEnv(req, mock_context)

        assert any("Failed to prepare python environment" in r.message for r in caplog.records)


class TestExecuteCommand:
    """executeCommand delegates to handler.execute_command, logs, builds grpc response."""

    @patch("command_executor_server.CommandExecutorHandler")
    def test_successful_execution(self, MockHandler, server_instance, mock_context):
        MockHandler.return_value.execute_command.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: True,
            utils.RESULTS_LOG_KEY: ["all ok"],
            utils.ERR_MSG_KEY: "",
        }

        req = _execution_request(request_id="req-exec")
        result = server_instance.executeCommand(req, mock_context)

        MockHandler.assert_called_once_with(req)
        MockHandler.return_value.execute_command.assert_called_once_with(req)
        assert isinstance(result, pb2.ExecutionOutput)
        assert result.requestId == "req-exec"
        assert result.status == pb2.SUCCESS

    @patch("command_executor_server.CommandExecutorHandler")
    def test_failed_execution_with_err_msg(self, MockHandler, server_instance, mock_context):
        MockHandler.return_value.execute_command.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: False,
            utils.RESULTS_LOG_KEY: ["step1"],
            utils.ERR_MSG_KEY: "script crashed",
        }

        req = _execution_request(request_id="req-fail")
        result = server_instance.executeCommand(req, mock_context)

        assert result.status == pb2.FAILURE

    @patch("command_executor_server.CommandExecutorHandler")
    def test_failed_execution_without_err_msg(self, MockHandler, server_instance, mock_context, caplog):
        """When ERR_MSG_KEY is missing from the response dict, no error text is logged."""
        MockHandler.return_value.execute_command.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: False,
            utils.RESULTS_LOG_KEY: ["partial output"],
        }

        req = _execution_request()
        with caplog.at_level(logging.INFO, logger="CommandExecutorServer"):
            result = server_instance.executeCommand(req, mock_context)

        assert result.status == pb2.FAILURE
        # The "Error returned:" substring should NOT appear since ERR_MSG_KEY is absent
        failure_msgs = [r.message for r in caplog.records if "Failed to executeCommand" in r.message]
        assert len(failure_msgs) == 1
        assert "Error returned:" not in failure_msgs[0]

    @patch("command_executor_server.CommandExecutorHandler")
    def test_logs_success(self, MockHandler, server_instance, mock_context, caplog):
        MockHandler.return_value.execute_command.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: True,
            utils.RESULTS_LOG_KEY: [],
            utils.ERR_MSG_KEY: "",
        }

        req = _execution_request()
        with caplog.at_level(logging.INFO, logger="CommandExecutorServer"):
            server_instance.executeCommand(req, mock_context)

        assert any("Execution finished successfully" in r.message for r in caplog.records)

    @patch("command_executor_server.CommandExecutorHandler")
    def test_logs_failure_with_error_detail(self, MockHandler, server_instance, mock_context, caplog):
        MockHandler.return_value.execute_command.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: False,
            utils.RESULTS_LOG_KEY: ["started"],
            utils.ERR_MSG_KEY: "timeout!",
        }

        req = _execution_request()
        with caplog.at_level(logging.INFO, logger="CommandExecutorServer"):
            server_instance.executeCommand(req, mock_context)

        failure_msgs = [r.message for r in caplog.records if "Failed to executeCommand" in r.message]
        assert len(failure_msgs) == 1
        assert "Error returned: timeout!" in failure_msgs[0]

    @patch.dict(os.environ, {"CE_DEBUG": "true"})
    @patch("command_executor_server.CommandExecutorHandler")
    def test_ce_debug_logs_request(self, MockHandler, server_instance, mock_context, caplog):
        """When CE_DEBUG=true, the full request is logged."""
        MockHandler.return_value.execute_command.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: True,
            utils.RESULTS_LOG_KEY: [],
            utils.ERR_MSG_KEY: "",
        }

        req = _execution_request()
        with caplog.at_level(logging.INFO, logger="CommandExecutorServer"):
            server_instance.executeCommand(req, mock_context)

        # At least 3 log entries: received + request dump + success + payload
        assert len(caplog.records) >= 3

    @patch.dict(os.environ, {"CE_DEBUG": "false"})
    @patch("command_executor_server.CommandExecutorHandler")
    def test_ce_debug_off_does_not_log_request(self, MockHandler, server_instance, mock_context, caplog):
        MockHandler.return_value.execute_command.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: True,
            utils.RESULTS_LOG_KEY: [],
            utils.ERR_MSG_KEY: "",
        }

        req = _execution_request()
        with caplog.at_level(logging.INFO, logger="CommandExecutorServer"):
            server_instance.executeCommand(req, mock_context)

        # The request object itself should not appear in logs
        logged_messages = " ".join(r.message for r in caplog.records if isinstance(r.message, str))
        assert "Received executeCommand" in logged_messages


# ---------------------------------------------------------------------------
# gRPC integration tests — real server + channel + stub
# ---------------------------------------------------------------------------

class TestGrpcIntegration:
    """
    Spin up a real gRPC server with CommandExecutorServer registered,
    make RPC calls through a channel, and verify end-to-end message
    serialization / deserialization with mocked handler internals.
    """

    @pytest.fixture(autouse=True)
    def grpc_server(self):
        """Start a gRPC server on a free port, yield a stub, then shut down."""
        self.server = grpc.server(futures.ThreadPoolExecutor(max_workers=4))
        self.servicer = CommandExecutorServer()
        pb2_grpc.add_CommandExecutorServiceServicer_to_server(self.servicer, self.server)
        port = self.server.add_insecure_port("[::]:0")  # OS assigns a free port
        self.server.start()

        channel = grpc.insecure_channel(f"localhost:{port}")
        self.stub = pb2_grpc.CommandExecutorServiceStub(channel)

        yield

        channel.close()
        self.server.stop(grace=0)

    # -- uploadBlueprint over gRPC --

    @patch("command_executor_server.CommandExecutorHandler")
    def test_upload_blueprint_grpc_roundtrip(self, MockHandler):
        expected = pb2.UploadBlueprintOutput()
        expected.requestId = "req-grpc-1"
        expected.status = pb2.SUCCESS
        expected.payload = '{"result": "uploaded"}'
        MockHandler.return_value.uploadBlueprint.return_value = expected

        req = _upload_request(
            name="grpc-bp", version="3.0.0", uuid="grpc-uuid",
            request_id="req-grpc-1", bin_data=_create_valid_zip_bytes(),
        )
        response = self.stub.uploadBlueprint(req)

        assert response.requestId == "req-grpc-1"
        assert response.status == pb2.SUCCESS
        assert response.payload == '{"result": "uploaded"}'

    @patch("command_executor_server.CommandExecutorHandler")
    def test_upload_blueprint_grpc_failure(self, MockHandler):
        expected = pb2.UploadBlueprintOutput()
        expected.requestId = "req-grpc-fail"
        expected.status = pb2.FAILURE
        expected.payload = '{"error": "bad archive"}'
        MockHandler.return_value.uploadBlueprint.return_value = expected

        req = _upload_request(request_id="req-grpc-fail")
        response = self.stub.uploadBlueprint(req)

        assert response.status == pb2.FAILURE

    # -- prepareEnv over gRPC --

    @patch("command_executor_server.CommandExecutorHandler")
    def test_prepare_env_grpc_success(self, MockHandler):
        MockHandler.return_value.prepare_env.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: True,
            utils.RESULTS_LOG_KEY: ["dep1 installed"],
            utils.ERR_MSG_KEY: "",
        }

        req = _prepare_env_request(request_id="req-prep-grpc")
        response = self.stub.prepareEnv(req)

        assert response.requestId == "req-prep-grpc"
        assert response.status == pb2.SUCCESS

    @patch("command_executor_server.CommandExecutorHandler")
    def test_prepare_env_grpc_failure(self, MockHandler):
        MockHandler.return_value.prepare_env.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: False,
            utils.RESULTS_LOG_KEY: [],
            utils.ERR_MSG_KEY: "network error during pip install",
        }

        req = _prepare_env_request(request_id="req-prep-fail")
        response = self.stub.prepareEnv(req)

        assert response.requestId == "req-prep-fail"
        assert response.status == pb2.FAILURE

    # -- executeCommand over gRPC --

    @patch("command_executor_server.CommandExecutorHandler")
    def test_execute_command_grpc_success(self, MockHandler):
        MockHandler.return_value.execute_command.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: True,
            utils.RESULTS_LOG_KEY: ["output line"],
            utils.ERR_MSG_KEY: "",
        }

        req = _execution_request(request_id="req-exec-grpc", command="python run.py")
        response = self.stub.executeCommand(req)

        assert response.requestId == "req-exec-grpc"
        assert response.status == pb2.SUCCESS

    @patch("command_executor_server.CommandExecutorHandler")
    def test_execute_command_grpc_failure(self, MockHandler):
        MockHandler.return_value.execute_command.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: False,
            utils.RESULTS_LOG_KEY: ["partial"],
            utils.ERR_MSG_KEY: "script error",
        }

        req = _execution_request(request_id="req-exec-fail")
        response = self.stub.executeCommand(req)

        assert response.requestId == "req-exec-fail"
        assert response.status == pb2.FAILURE

    @patch("command_executor_server.CommandExecutorHandler")
    def test_execute_command_grpc_preserves_payload(self, MockHandler):
        """Verify payload round-trips correctly through gRPC serialization."""
        payload_data = json.dumps({"key": "value", "number": 42})
        MockHandler.return_value.execute_command.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: True,
            utils.RESULTS_LOG_KEY: [payload_data],
            utils.ERR_MSG_KEY: "",
        }

        req = _execution_request(request_id="req-payload")
        response = self.stub.executeCommand(req)

        assert response.requestId == "req-payload"
        assert response.status == pb2.SUCCESS

    @patch("command_executor_server.CommandExecutorHandler")
    def test_request_fields_received_by_handler(self, MockHandler):
        """Verify that the proto request fields survive serialization to the server."""
        MockHandler.return_value.execute_command.return_value = {
            utils.CDS_IS_SUCCESSFUL_KEY: True,
            utils.RESULTS_LOG_KEY: [],
            utils.ERR_MSG_KEY: "",
        }

        req = _execution_request(
            name="field-bp", version="5.0.0", uuid="field-uuid",
            request_id="field-req", command="python check.py",
        )
        self.stub.executeCommand(req)

        # The handler was constructed with a request that has the right identifiers
        call_args = MockHandler.call_args[0][0]
        assert call_args.identifiers.blueprintName == "field-bp"
        assert call_args.identifiers.blueprintVersion == "5.0.0"
        assert call_args.requestId == "field-req"
        assert call_args.command == "python check.py"
