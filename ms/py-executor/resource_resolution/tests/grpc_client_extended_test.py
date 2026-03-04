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
Extended tests for resource_resolution/grpc/client.py — covers secure channel,
header auth, process method, close, and context manager protocol.
"""

from unittest.mock import MagicMock, patch, call

import pytest

from resource_resolution.grpc.client import Client


class TestClientInit:

    @patch("resource_resolution.grpc.client.insecure_channel")
    def test_insecure_channel_created_by_default(self, mock_insecure):
        mock_insecure.return_value = MagicMock()
        client = Client("localhost:9111")
        mock_insecure.assert_called_once_with("localhost:9111")

    @patch("resource_resolution.grpc.client.ssl_channel_credentials")
    @patch("resource_resolution.grpc.client.secure_channel")
    def test_secure_channel_with_ssl(self, mock_secure, mock_ssl_creds):
        mock_secure.return_value = MagicMock()
        client = Client(
            "localhost:9111",
            use_ssl=True,
            root_certificates=b"root-cert",
            private_key=b"priv-key",
            certificate_chain=b"cert-chain",
        )
        mock_ssl_creds.assert_called_once_with(b"root-cert", b"priv-key", b"cert-chain")
        mock_secure.assert_called_once_with("localhost:9111", mock_ssl_creds.return_value)

    @patch("resource_resolution.grpc.client.intercept_channel")
    @patch("resource_resolution.grpc.client.insecure_channel")
    def test_header_auth_intercepts_channel(self, mock_insecure, mock_intercept):
        mock_insecure.return_value = MagicMock()
        mock_intercept.return_value = MagicMock()
        client = Client(
            "localhost:9111",
            use_header_auth=True,
            header_auth_token="Bearer xyz",
        )
        mock_intercept.assert_called_once()
        # The intercepted channel should be used
        assert client.channel == mock_intercept.return_value

    @patch("resource_resolution.grpc.client.insecure_channel")
    def test_no_header_auth_by_default(self, mock_insecure):
        mock_insecure.return_value = MagicMock()
        with patch("resource_resolution.grpc.client.intercept_channel") as mock_intercept:
            Client("localhost:9111")
            mock_intercept.assert_not_called()


class TestClientClose:

    @patch("resource_resolution.grpc.client.insecure_channel")
    def test_close_calls_channel_close(self, mock_insecure):
        mock_channel = MagicMock()
        mock_insecure.return_value = mock_channel
        client = Client("localhost:9111")
        client.close()
        mock_channel.close.assert_called_once()


class TestClientContextManager:

    @patch("resource_resolution.grpc.client.insecure_channel")
    def test_enter_returns_client(self, mock_insecure):
        mock_insecure.return_value = MagicMock()
        client = Client("localhost:9111")
        result = client.__enter__()
        assert result is client

    @patch("resource_resolution.grpc.client.insecure_channel")
    def test_exit_closes_channel(self, mock_insecure):
        mock_channel = MagicMock()
        mock_insecure.return_value = mock_channel
        client = Client("localhost:9111")
        client.__exit__(None, None, None)
        mock_channel.close.assert_called_once()

    @patch("resource_resolution.grpc.client.insecure_channel")
    def test_with_statement(self, mock_insecure):
        mock_channel = MagicMock()
        mock_insecure.return_value = mock_channel
        with Client("localhost:9111") as c:
            assert isinstance(c, Client)
        mock_channel.close.assert_called_once()


class TestClientProcess:

    @patch("resource_resolution.grpc.client.insecure_channel")
    def test_process_yields_responses(self, mock_insecure):
        mock_channel = MagicMock()
        mock_insecure.return_value = mock_channel

        client = Client("localhost:9111")

        # Mock the stub's process method to return iterable responses
        mock_response_1 = MagicMock()
        mock_response_2 = MagicMock()
        client.stub = MagicMock()
        client.stub.process.return_value = iter([mock_response_1, mock_response_2])

        messages = [MagicMock(), MagicMock()]
        responses = list(client.process(messages))

        assert len(responses) == 2
        assert responses[0] is mock_response_1
        assert responses[1] is mock_response_2

    @patch("resource_resolution.grpc.client.insecure_channel")
    def test_process_empty_messages(self, mock_insecure):
        mock_insecure.return_value = MagicMock()
        client = Client("localhost:9111")
        client.stub = MagicMock()
        client.stub.process.return_value = iter([])

        responses = list(client.process(iter([])))
        assert responses == []
