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
Tests for request_header_validator_interceptor.py — gRPC server interceptor
that validates authorization headers on incoming requests.
"""

from unittest.mock import MagicMock, patch

import grpc

from request_header_validator_interceptor import (
    RequestHeaderValidatorInterceptor,
    _unary_unary_rpc_terminator,
)


# ===================================================================
# _unary_unary_rpc_terminator
# ===================================================================

class TestUnaryUnaryRpcTerminator:

    def test_returns_rpc_method_handler(self):
        handler = _unary_unary_rpc_terminator(
            grpc.StatusCode.UNAUTHENTICATED, "Access denied"
        )
        # grpc.unary_unary_rpc_method_handler returns an RpcMethodHandler
        assert handler is not None
        assert handler.unary_unary is not None

    def test_terminator_aborts_context(self):
        handler = _unary_unary_rpc_terminator(
            grpc.StatusCode.UNAUTHENTICATED, "No access"
        )
        mock_context = MagicMock()
        # Call the actual terminate function
        handler.unary_unary(None, mock_context)
        mock_context.abort.assert_called_once_with(
            grpc.StatusCode.UNAUTHENTICATED, "No access"
        )


# ===================================================================
# RequestHeaderValidatorInterceptor
# ===================================================================

class TestRequestHeaderValidatorInterceptor:

    def _make_interceptor(self, header="authorization", value="Basic abc123"):
        return RequestHeaderValidatorInterceptor(
            header, value,
            grpc.StatusCode.UNAUTHENTICATED, "Access denied!"
        )

    def test_valid_header_continues(self):
        interceptor = self._make_interceptor(
            header="authorization", value="Basic abc123"
        )

        # Mock handler_call_details with matching metadata
        mock_details = MagicMock()
        mock_details.invocation_metadata = [
            ("authorization", "Basic abc123"),
            ("other-header", "other-value"),
        ]

        mock_continuation = MagicMock(return_value="continued_handler")
        result = interceptor.intercept_service(mock_continuation, mock_details)

        mock_continuation.assert_called_once_with(mock_details)
        assert result == "continued_handler"

    def test_missing_header_returns_terminator(self):
        interceptor = self._make_interceptor(
            header="authorization", value="Basic abc123"
        )

        mock_details = MagicMock()
        mock_details.invocation_metadata = [
            ("other-header", "other-value"),
        ]

        mock_continuation = MagicMock()
        result = interceptor.intercept_service(mock_continuation, mock_details)

        # Continuation should NOT be called
        mock_continuation.assert_not_called()
        # Result should be the terminator (an RpcMethodHandler)
        assert result is not None

    def test_wrong_value_returns_terminator(self):
        interceptor = self._make_interceptor(
            header="authorization", value="Basic abc123"
        )

        mock_details = MagicMock()
        mock_details.invocation_metadata = [
            ("authorization", "Basic WRONG"),
        ]

        mock_continuation = MagicMock()
        result = interceptor.intercept_service(mock_continuation, mock_details)

        mock_continuation.assert_not_called()

    def test_empty_metadata_returns_terminator(self):
        interceptor = self._make_interceptor()

        mock_details = MagicMock()
        mock_details.invocation_metadata = []

        mock_continuation = MagicMock()
        result = interceptor.intercept_service(mock_continuation, mock_details)

        mock_continuation.assert_not_called()
