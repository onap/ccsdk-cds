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
Tests for utils.py — gRPC response building, request field extraction,
output parsing, and log truncation.
"""

import json
import tempfile
from types import SimpleNamespace
from unittest.mock import patch, MagicMock

import pytest

import proto.CommandExecutor_pb2 as CommandExecutor_pb2
import utils


# ---------------------------------------------------------------------------
# Helpers — lightweight request stub
# ---------------------------------------------------------------------------

def _make_identifiers(name="bp1", version="1.0.0", uuid="abc-123"):
    return SimpleNamespace(blueprintName=name, blueprintVersion=version, blueprintUUID=uuid)


def _make_request(name="bp1", version="1.0.0", uuid="abc-123",
                  timeout=30, request_id="req-1", sub_request_id="sub-1",
                  originator_id="orig-1"):
    return SimpleNamespace(
        identifiers=_make_identifiers(name, version, uuid),
        timeOut=timeout,
        requestId=request_id,
        subRequestId=sub_request_id,
        originatorId=originator_id,
    )


# ===================================================================
# Request field extraction
# ===================================================================

class TestRequestFieldExtraction:
    """Tests for the simple accessor functions at the top of utils.py."""

    def test_blueprint_name_version_uuid(self):
        req = _make_request(name="my-bp", version="2.0.0", uuid="u-1")
        assert utils.blueprint_name_version_uuid(req) == "my-bp/2.0.0/u-1"

    def test_blueprint_name_version(self):
        req = _make_request(name="my-bp", version="2.0.0")
        assert utils.blueprint_name_version(req) == "my-bp/2.0.0"

    def test_get_blueprint_name(self):
        req = _make_request(name="test-cba")
        assert utils.get_blueprint_name(req) == "test-cba"

    def test_get_blueprint_version(self):
        req = _make_request(version="3.1.0")
        assert utils.get_blueprint_version(req) == "3.1.0"

    def test_get_blueprint_uuid(self):
        req = _make_request(uuid="12345")
        assert utils.get_blueprint_uuid(req) == "12345"

    def test_get_blueprint_timeout(self):
        req = _make_request(timeout=120)
        assert utils.get_blueprint_timeout(req) == 120

    def test_get_blueprint_requestid(self):
        req = _make_request(request_id="r-99")
        assert utils.get_blueprint_requestid(req) == "r-99"

    def test_get_blueprint_subRequestId(self):
        req = _make_request(sub_request_id="sr-42")
        assert utils.get_blueprint_subRequestId(req) == "sr-42"


# ===================================================================
# getExtraLogData
# ===================================================================

class TestGetExtraLogData:

    def test_with_request(self):
        req = _make_request(request_id="r1", sub_request_id="s1", originator_id="o1")
        extra = utils.getExtraLogData(req)
        assert extra == {
            'request_id': 'r1',
            'subrequest_id': 's1',
            'originator_id': 'o1',
        }

    def test_without_request(self):
        extra = utils.getExtraLogData()
        assert extra == {
            'request_id': '',
            'subrequest_id': '',
            'originator_id': '',
        }

    def test_with_none(self):
        extra = utils.getExtraLogData(None)
        assert extra == {
            'request_id': '',
            'subrequest_id': '',
            'originator_id': '',
        }


# ===================================================================
# build_ret_data
# ===================================================================

class TestBuildRetData:

    def test_success_minimal(self):
        ret = utils.build_ret_data(True)
        assert ret[utils.CDS_IS_SUCCESSFUL_KEY] is True
        assert ret[utils.RESULTS_LOG_KEY] == []
        assert utils.ERR_MSG_KEY not in ret
        assert utils.REUPLOAD_CBA_KEY not in ret

    def test_failure_with_error(self):
        ret = utils.build_ret_data(False, error="something broke")
        assert ret[utils.CDS_IS_SUCCESSFUL_KEY] is False
        assert ret[utils.ERR_MSG_KEY] == "something broke"

    def test_with_results_log(self):
        logs = ["line1", "line2"]
        ret = utils.build_ret_data(True, results_log=logs)
        assert ret[utils.RESULTS_LOG_KEY] == logs

    def test_reupload_cba_flag(self):
        ret = utils.build_ret_data(False, reupload_cba=True)
        assert ret[utils.REUPLOAD_CBA_KEY] is True

    def test_reupload_cba_absent_when_false(self):
        ret = utils.build_ret_data(False, reupload_cba=False)
        assert utils.REUPLOAD_CBA_KEY not in ret


# ===================================================================
# build_grpc_response
# ===================================================================

class TestBuildGrpcResponse:

    def test_success_response(self):
        data = {
            utils.CDS_IS_SUCCESSFUL_KEY: True,
            utils.RESULTS_LOG_KEY: ["log line 1"],
        }
        resp = utils.build_grpc_response("req-1", data)

        assert isinstance(resp, CommandExecutor_pb2.ExecutionOutput)
        assert resp.requestId == "req-1"
        assert resp.status == CommandExecutor_pb2.SUCCESS
        assert resp.errMsg == ""
        # CDS_IS_SUCCESSFUL_KEY and RESULTS_LOG_KEY should be popped from data
        payload = json.loads(resp.payload)
        assert utils.CDS_IS_SUCCESSFUL_KEY not in payload
        assert utils.RESULTS_LOG_KEY not in payload

    def test_failure_response_with_error(self):
        data = {
            utils.CDS_IS_SUCCESSFUL_KEY: False,
            utils.RESULTS_LOG_KEY: ["log1"],
            utils.ERR_MSG_KEY: ["Error line 1", "Error line 2"],
        }
        resp = utils.build_grpc_response("req-2", data)

        assert resp.status == CommandExecutor_pb2.FAILURE
        assert "Error line 1" in resp.errMsg
        assert "Error line 2" in resp.errMsg

    def test_response_contains_timestamp(self):
        data = {
            utils.CDS_IS_SUCCESSFUL_KEY: True,
            utils.RESULTS_LOG_KEY: [],
        }
        resp = utils.build_grpc_response("req-3", data)
        assert resp.timestamp.seconds > 0

    def test_response_logs_preserved(self):
        data = {
            utils.CDS_IS_SUCCESSFUL_KEY: True,
            utils.RESULTS_LOG_KEY: ["line A", "line B", "line C"],
        }
        resp = utils.build_grpc_response("req-4", data)
        assert list(resp.response) == ["line A", "line B", "line C"]

    def test_extra_payload_fields_preserved(self):
        data = {
            utils.CDS_IS_SUCCESSFUL_KEY: True,
            utils.RESULTS_LOG_KEY: [],
            "custom_key": "custom_value",
        }
        resp = utils.build_grpc_response("req-5", data)
        payload = json.loads(resp.payload)
        assert payload["custom_key"] == "custom_value"


# ===================================================================
# build_grpc_blueprint_validation_response
# ===================================================================

class TestBuildGrpcBlueprintValidationResponse:

    def test_success(self):
        resp = utils.build_grpc_blueprint_validation_response(
            "req-1", "sub-1", "uuid-1", success=True
        )
        assert isinstance(resp, CommandExecutor_pb2.BlueprintValidationOutput)
        assert resp.requestId == "req-1"
        assert resp.subRequestId == "sub-1"
        assert resp.cbaUUID == "uuid-1"
        assert resp.status == CommandExecutor_pb2.SUCCESS

    def test_failure(self):
        resp = utils.build_grpc_blueprint_validation_response(
            "req-2", "sub-2", "uuid-2", success=False
        )
        assert resp.status == CommandExecutor_pb2.FAILURE

    def test_has_timestamp(self):
        resp = utils.build_grpc_blueprint_validation_response("r", "s", "u")
        assert resp.timestamp.seconds > 0


# ===================================================================
# build_grpc_blueprint_upload_response
# ===================================================================

class TestBuildGrpcBlueprintUploadResponse:

    def test_success(self):
        resp = utils.build_grpc_blueprint_upload_response("req-1", "sub-1", success=True)
        assert isinstance(resp, CommandExecutor_pb2.UploadBlueprintOutput)
        assert resp.status == CommandExecutor_pb2.SUCCESS
        assert resp.requestId == "req-1"
        assert resp.subRequestId == "sub-1"

    def test_failure_with_payload(self):
        resp = utils.build_grpc_blueprint_upload_response(
            "req-2", "sub-2", success=False, payload=["err1", "err2"]
        )
        assert resp.status == CommandExecutor_pb2.FAILURE
        payload = json.loads(resp.payload)
        assert payload == ["err1", "err2"]

    def test_has_timestamp(self):
        resp = utils.build_grpc_blueprint_upload_response("r", "s")
        assert resp.timestamp.seconds > 0


# ===================================================================
# truncate_execution_output
# ===================================================================

class TestTruncateExecutionOutput:

    def test_small_output_not_truncated(self):
        eo = CommandExecutor_pb2.ExecutionOutput(requestId="r1", payload="{}")
        eo.response.append("short log line")
        result = utils.truncate_execution_output(eo)
        assert list(result.response) == ["short log line"]

    def test_large_output_is_truncated(self):
        eo = CommandExecutor_pb2.ExecutionOutput(requestId="r1", payload="{}")
        # Fill with enough data to exceed RESPONSE_MAX_SIZE (4MB)
        large_line = "x" * 10000
        for _ in range(500):
            eo.response.append(large_line)

        result = utils.truncate_execution_output(eo)
        # The last entry should be the truncation message
        assert result.response[-1].startswith("[...] TRUNCATED CHARS")
        assert result.ByteSize() <= utils.RESPONSE_MAX_SIZE + 1000  # small overhead ok

    def test_truncation_message_contains_char_count(self):
        eo = CommandExecutor_pb2.ExecutionOutput(requestId="r1", payload="{}")
        large_line = "y" * 10000
        for _ in range(500):
            eo.response.append(large_line)

        result = utils.truncate_execution_output(eo)
        last_line = result.response[-1]
        # Extract the number from "[...] TRUNCATED CHARS : NNN"
        assert "TRUNCATED CHARS" in last_line
        count_str = last_line.split(":")[-1].strip()
        assert int(count_str) > 0


# ===================================================================
# parse_cmd_exec_output
# ===================================================================

class TestParseCmdExecOutput:

    def _make_logger(self):
        logger = MagicMock()
        return logger

    def test_plain_output(self):
        """Lines without special markers go to results_log."""
        content = "line 1\nline 2\nline 3\n"
        with tempfile.TemporaryFile(mode="w+") as f:
            f.write(content)
            results_log = []
            payload = {}
            err_msg = []
            utils.parse_cmd_exec_output(f, self._make_logger(), payload, err_msg, results_log, {})

        assert results_log == ["line 1", "line 2", "line 3"]
        assert payload == {}
        assert err_msg == []

    def test_error_message_section(self):
        """Lines between BEGIN/END_EXTRA_RET_ERR_MSG go to err_msg_result."""
        content = (
            "some output\n"
            "BEGIN_EXTRA_RET_ERR_MSG\n"
            "error detail 1\n"
            "error detail 2\n"
            "END_EXTRA_RET_ERR_MSG\n"
            "more output\n"
        )
        with tempfile.TemporaryFile(mode="w+") as f:
            f.write(content)
            results_log = []
            payload = {}
            err_msg = []
            utils.parse_cmd_exec_output(f, self._make_logger(), payload, err_msg, results_log, {})

        assert "error detail 1\nerror detail 2" in err_msg[0]
        assert "some output" in results_log
        assert "more output" in results_log

    def test_payload_section(self):
        """Lines between BEGIN/END_EXTRA_PAYLOAD are parsed as MIME payload."""
        # Build the MIME payload the same way payload_coder does
        from email.mime import multipart, text as mime_text
        m = multipart.MIMEMultipart("form-data")
        data = mime_text.MIMEText("response_payload", "json", "utf8")
        data.set_payload(json.JSONEncoder().encode({"key": "value"}))
        m.attach(data)

        content = (
            "output before\n"
            "BEGIN_EXTRA_PAYLOAD\n"
            + m.as_string() + "\n"
            "END_EXTRA_PAYLOAD\n"
            "output after\n"
        )
        with tempfile.TemporaryFile(mode="w+") as f:
            f.write(content)
            results_log = []
            payload = {}
            err_msg = []
            utils.parse_cmd_exec_output(f, self._make_logger(), payload, err_msg, results_log, {})

        assert payload.get("key") == "value"
        assert "output before" in results_log
        assert "output after" in results_log

    def test_empty_file(self):
        """Empty output produces empty results."""
        with tempfile.TemporaryFile(mode="w+") as f:
            results_log = []
            payload = {}
            err_msg = []
            utils.parse_cmd_exec_output(f, self._make_logger(), payload, err_msg, results_log, {})

        assert results_log == []
        assert payload == {}
        assert err_msg == []
