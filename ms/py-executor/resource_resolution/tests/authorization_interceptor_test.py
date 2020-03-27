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

from unittest.mock import MagicMock, _Call

import pytest

from resource_resolution.grpc.authorization import AuthTokenInterceptor, NewClientCallDetails


def test_resource_resolution_auth_token_interceptor():
    """Test AuthTokenInterceptor class.

     - Checks if it's correctly set default value.
     - Checks if it's correctly set passed values.
     - Checks if it's correctly checked if all header characters are lowercase.
     - Checks if continuation function is called with headers setted 
    """
    interceptor: AuthTokenInterceptor = AuthTokenInterceptor("test_token", header="header")
    assert interceptor.token == "test_token"
    assert interceptor.header == "header"

    interceptor: AuthTokenInterceptor = AuthTokenInterceptor("test_token")
    assert interceptor.token == "test_token"
    assert interceptor.header == "authorization"

    with pytest.raises(ValueError):
        AuthTokenInterceptor("test_token", header="Auth")

    continuation_mock: MagicMock = MagicMock()
    client_call_details: MagicMock = MagicMock()
    request_iterator: MagicMock = MagicMock()

    interceptor.intercept_stream_stream(continuation_mock, client_call_details, request_iterator)
    continuation_mock.assert_called_once()
    client_call_details_argument: _Call = continuation_mock.call_args_list[0][0][0]  # Get NewClientCallDetails instance
    assert isinstance(client_call_details_argument, NewClientCallDetails)
    assert client_call_details_argument.metadata[0] == (interceptor.header, interceptor.token)
