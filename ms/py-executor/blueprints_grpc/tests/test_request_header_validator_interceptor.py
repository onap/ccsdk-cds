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
Tests for blueprints_grpc/request_header_validator_interceptor.py —
gRPC server-side interceptor for authorization header validation.
"""

from unittest.mock import MagicMock

import grpc
import pytest

from blueprints_grpc.request_header_validator_interceptor import (
    _unary_unary_rpc_terminator,
    RequestHeaderValidatorInterceptor,
)


# ---------------------------------------------------------------------------
# _unary_unary_rpc_terminator
# ---------------------------------------------------------------------------

class TestUnaryUnaryRpcTerminator:

    def test_returns_rpc_method_handler(self):
        handler = _unary_unary_rpc_terminator(
            grpc.StatusCode.UNAUTHENTICATED, "Access denied"
        )
        assert handler is not None

    def test_handler_aborts_context(self):
        handler = _unary_unary_rpc_terminator(
            grpc.StatusCode.UNAUTHENTICATED, "No access"
        )
        context = MagicMock()
        # The handler wraps a function; invoke the unary_unary handler
        handler.unary_unary(MagicMock(), context)
        context.abort.assert_called_once_with(
            grpc.StatusCode.UNAUTHENTICATED, "No access"
        )


# ---------------------------------------------------------------------------
# RequestHeaderValidatorInterceptor
# ---------------------------------------------------------------------------

class TestRequestHeaderValidatorInterceptor:

    def test_init_stores_values(self):
        interceptor = RequestHeaderValidatorInterceptor(
            "authorization", "Bearer token123",
            grpc.StatusCode.UNAUTHENTICATED, "Denied"
        )
        assert interceptor._header == "authorization"
        assert interceptor._value == "Bearer token123"

    def test_valid_header_continues(self):
        interceptor = RequestHeaderValidatorInterceptor(
            "authorization", "Bearer valid",
            grpc.StatusCode.UNAUTHENTICATED, "Denied"
        )
        continuation = MagicMock()
        handler_call_details = MagicMock()
        handler_call_details.invocation_metadata = [
            ("authorization", "Bearer valid"),
            ("other-header", "other-value"),
        ]

        result = interceptor.intercept_service(continuation, handler_call_details)

        continuation.assert_called_once_with(handler_call_details)
        assert result == continuation.return_value

    def test_missing_header_returns_terminator(self):
        interceptor = RequestHeaderValidatorInterceptor(
            "authorization", "Bearer valid",
            grpc.StatusCode.UNAUTHENTICATED, "Denied"
        )
        continuation = MagicMock()
        handler_call_details = MagicMock()
        handler_call_details.invocation_metadata = [
            ("other-header", "other-value"),
        ]

        result = interceptor.intercept_service(continuation, handler_call_details)

        continuation.assert_not_called()
        assert result is interceptor._terminator

    def test_wrong_value_returns_terminator(self):
        interceptor = RequestHeaderValidatorInterceptor(
            "authorization", "Bearer correct",
            grpc.StatusCode.UNAUTHENTICATED, "Denied"
        )
        continuation = MagicMock()
        handler_call_details = MagicMock()
        handler_call_details.invocation_metadata = [
            ("authorization", "Bearer wrong"),
        ]

        result = interceptor.intercept_service(continuation, handler_call_details)

        continuation.assert_not_called()
        assert result is interceptor._terminator

    def test_empty_metadata_returns_terminator(self):
        interceptor = RequestHeaderValidatorInterceptor(
            "authorization", "Bearer token",
            grpc.StatusCode.UNAUTHENTICATED, "No creds"
        )
        continuation = MagicMock()
        handler_call_details = MagicMock()
        handler_call_details.invocation_metadata = []

        result = interceptor.intercept_service(continuation, handler_call_details)

        continuation.assert_not_called()
        assert result is interceptor._terminator
