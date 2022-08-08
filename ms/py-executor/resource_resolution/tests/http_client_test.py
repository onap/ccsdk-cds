"""Copyright 2020 Deutsche Telekom.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

from unittest.mock import MagicMock, patch

from resource_resolution.http.client import Client


@patch("resource_resolution.http.client.request")
def test_http_client(request_mock):
    c = Client("127.0.0.1", 8080)
    assert c.auth is None
    c = Client("127.0.0.1", 8080, auth_user="user")
    assert c.auth is None
    c = Client("127.0.0.1", 8080, auth_user="user", auth_pass="pass")
    assert c.auth == ("user", "pass")

    assert c.protocol == "http"
    assert c.url == "http://127.0.0.1:8080/api/v1"

    c = Client("127.0.0.1", 8080, use_ssl=True)
    assert c.protocol == "https"
    assert c.url == "https://127.0.0.1:8080/api/v1"

    c.send_request("GET", "something")
    request_mock.assert_called_once_with(method="GET", url=f"{c.url}/something", verify=False, auth=None)
