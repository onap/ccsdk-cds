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
Tests for cds_utils/payload_coder.py — MIME payload and error message
encoding used by CBA scripts to communicate results back to the
command executor.
"""

import json
import sys
from io import StringIO
from unittest.mock import patch

from cds_utils.payload_coder import (
    send_response_data_payload,
    send_response_err_msg,
    send_response_err_msg_and_exit,
)


class TestSendResponseDataPayload:

    def test_output_contains_begin_end_markers(self):
        """Output must be wrapped in BEGIN/END_EXTRA_PAYLOAD markers."""
        captured = StringIO()
        with patch("sys.stdout", captured):
            send_response_data_payload({"status": "ok"})

        output = captured.getvalue()
        assert "BEGIN_EXTRA_PAYLOAD" in output
        assert "END_EXTRA_PAYLOAD" in output

    def test_payload_is_json_encoded(self):
        """The JSON payload must appear between the markers."""
        captured = StringIO()
        with patch("sys.stdout", captured):
            send_response_data_payload({"key": "value", "count": 42})

        output = captured.getvalue()
        # The encoded JSON should be somewhere in the output
        assert '"key"' in output
        assert '"value"' in output
        assert "42" in output

    def test_output_is_mime_formatted(self):
        """Output should be MIME multipart form-data."""
        captured = StringIO()
        with patch("sys.stdout", captured):
            send_response_data_payload({"a": 1})

        output = captured.getvalue()
        assert "Content-Type:" in output
        assert "form-data" in output

    def test_empty_payload(self):
        """An empty dict should still produce valid markers and MIME."""
        captured = StringIO()
        with patch("sys.stdout", captured):
            send_response_data_payload({})

        output = captured.getvalue()
        assert "BEGIN_EXTRA_PAYLOAD" in output
        assert "END_EXTRA_PAYLOAD" in output
        assert "{}" in output

    def test_roundtrip_with_parse_cmd_exec_output(self):
        """The output of send_response_data_payload should be parseable
        by utils.parse_cmd_exec_output (integration-style test)."""
        import tempfile
        from unittest.mock import MagicMock
        import utils

        payload_data = {"result": "success", "code": 200}

        captured = StringIO()
        with patch("sys.stdout", captured):
            send_response_data_payload(payload_data)

        output = captured.getvalue()

        with tempfile.TemporaryFile(mode="w+") as f:
            f.write(output)
            results_log = []
            payload_result = {}
            err_msg_result = []
            utils.parse_cmd_exec_output(
                f, MagicMock(), payload_result, err_msg_result, results_log, {}
            )

        assert payload_result["result"] == "success"
        assert payload_result["code"] == 200


class TestSendResponseErrMsg:

    def test_output_contains_begin_end_markers(self):
        captured = StringIO()
        with patch("sys.stdout", captured):
            send_response_err_msg("something went wrong")

        output = captured.getvalue()
        assert "BEGIN_EXTRA_RET_ERR_MSG" in output
        assert "END_EXTRA_RET_ERR_MSG" in output

    def test_error_message_is_included(self):
        captured = StringIO()
        with patch("sys.stdout", captured):
            send_response_err_msg("disk full")

        output = captured.getvalue()
        assert "disk full" in output

    def test_roundtrip_with_parse_cmd_exec_output(self):
        """The err msg output should be parseable by parse_cmd_exec_output."""
        import tempfile
        from unittest.mock import MagicMock
        import utils

        captured = StringIO()
        with patch("sys.stdout", captured):
            send_response_err_msg("custom error from script")

        output = captured.getvalue()

        with tempfile.TemporaryFile(mode="w+") as f:
            f.write(output)
            results_log = []
            payload_result = {}
            err_msg_result = []
            utils.parse_cmd_exec_output(
                f, MagicMock(), payload_result, err_msg_result, results_log, {}
            )

        assert len(err_msg_result) == 1
        assert "custom error from script" in err_msg_result[0]


class TestSendResponseErrMsgAndExit:

    def test_output_contains_markers(self):
        captured = StringIO()
        with patch("sys.stdout", captured), \
             pytest.raises(SystemExit) as exc_info:
            send_response_err_msg_and_exit("fatal error")

        output = captured.getvalue()
        assert "BEGIN_EXTRA_RET_ERR_MSG" in output
        assert "END_EXTRA_RET_ERR_MSG" in output
        assert "fatal error" in output

    def test_exits_with_default_code_1(self):
        with patch("sys.stdout", StringIO()), \
             pytest.raises(SystemExit) as exc_info:
            send_response_err_msg_and_exit("oops")

        assert exc_info.value.code == 1

    def test_exits_with_custom_code(self):
        with patch("sys.stdout", StringIO()), \
             pytest.raises(SystemExit) as exc_info:
            send_response_err_msg_and_exit("oops", code=42)

        assert exc_info.value.code == 42


# Need pytest import for raises
import pytest
