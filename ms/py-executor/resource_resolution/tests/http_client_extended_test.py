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
Extended tests for resource_resolution/http/client.py — covers auth
combinations, protocol selection, URL construction, and send_request
with various parameters.
"""

from unittest.mock import MagicMock, patch

import pytest
import requests

from resource_resolution.http.client import Client


class TestClientAuth:

    def test_no_auth_when_both_none(self):
        c = Client("127.0.0.1", 8080)
        assert c.auth is None

    def test_no_auth_when_only_user(self):
        c = Client("127.0.0.1", 8080, auth_user="user")
        assert c.auth is None

    def test_no_auth_when_only_pass(self):
        c = Client("127.0.0.1", 8080, auth_pass="pass")
        assert c.auth is None

    def test_auth_tuple_when_both_provided(self):
        c = Client("127.0.0.1", 8080, auth_user="admin", auth_pass="secret")
        assert c.auth == ("admin", "secret")


class TestClientProtocol:

    def test_http_by_default(self):
        c = Client("127.0.0.1", 8080)
        assert c.protocol == "http"

    def test_https_when_ssl(self):
        c = Client("127.0.0.1", 8443, use_ssl=True)
        assert c.protocol == "https"


class TestClientUrl:

    def test_http_url(self):
        c = Client("myhost", 9090)
        assert c.url == "http://myhost:9090/api/v1"

    def test_https_url(self):
        c = Client("myhost", 9443, use_ssl=True)
        assert c.url == "https://myhost:9443/api/v1"


class TestClientSendRequest:

    @patch("resource_resolution.http.client.request")
    def test_get_request_without_auth(self, mock_request):
        mock_response = MagicMock()
        mock_response.raise_for_status = MagicMock()
        mock_request.return_value = mock_response

        c = Client("127.0.0.1", 8080)
        result = c.send_request("GET", "test-endpoint")

        mock_request.assert_called_once_with(
            method="GET",
            url="http://127.0.0.1:8080/api/v1/test-endpoint",
            verify=False,
            auth=None,
        )
        mock_response.raise_for_status.assert_called_once()
        assert result is mock_response

    @patch("resource_resolution.http.client.request")
    def test_post_request_with_auth(self, mock_request):
        mock_response = MagicMock()
        mock_response.raise_for_status = MagicMock()
        mock_request.return_value = mock_response

        c = Client("127.0.0.1", 8080, auth_user="user", auth_pass="pass")
        result = c.send_request(
            "POST", "resource",
            headers={"Content-Type": "application/json"},
            data='{"key": "val"}',
        )

        mock_request.assert_called_once_with(
            method="POST",
            url="http://127.0.0.1:8080/api/v1/resource",
            verify=False,
            auth=("user", "pass"),
            headers={"Content-Type": "application/json"},
            data='{"key": "val"}',
        )

    @patch("resource_resolution.http.client.request")
    def test_send_request_raises_on_http_error(self, mock_request):
        """raise_for_status should propagate HTTPError."""
        mock_response = MagicMock()
        mock_response.raise_for_status.side_effect = requests.HTTPError("404 Not Found")
        mock_request.return_value = mock_response

        c = Client("127.0.0.1", 8080)
        with pytest.raises(requests.HTTPError):
            c.send_request("GET", "missing")

    @patch("resource_resolution.http.client.request")
    def test_send_request_with_params(self, mock_request):
        mock_response = MagicMock()
        mock_response.raise_for_status = MagicMock()
        mock_request.return_value = mock_response

        c = Client("127.0.0.1", 8080)
        c.send_request("GET", "search", params={"q": "test"})

        mock_request.assert_called_once_with(
            method="GET",
            url="http://127.0.0.1:8080/api/v1/search",
            verify=False,
            auth=None,
            params={"q": "test"},
        )

    @patch("resource_resolution.http.client.request")
    def test_ssl_url_in_request(self, mock_request):
        mock_response = MagicMock()
        mock_response.raise_for_status = MagicMock()
        mock_request.return_value = mock_response

        c = Client("secure-host", 443, use_ssl=True)
        c.send_request("GET", "endpoint")

        mock_request.assert_called_once_with(
            method="GET",
            url="https://secure-host:443/api/v1/endpoint",
            verify=False,
            auth=None,
        )
