"""Copyright 2019 Deutsche Telekom.

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

from resource_resolution.client import Client


@patch("resource_resolution.client.insecure_channel")
def test_resource_resoulution_insecure_channel(insecure_channel_mock: MagicMock):
    """Test if insecure_channel connection is called."""
    with patch.object(Client, "close") as client_close_method_mock:  # Type MagicMock
        with Client("127.0.0.1:3333"):
            pass
    insecure_channel_mock.called_once_with()
    client_close_method_mock.called_once_with()
