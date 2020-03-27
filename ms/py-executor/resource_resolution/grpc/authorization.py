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
from collections import namedtuple
from typing import Any, Callable, List

from grpc import ClientCallDetails, StreamStreamClientInterceptor


class NewClientCallDetails(
    namedtuple("_ClientCallDetails", ("method", "timeout", "metadata", "credentials")), ClientCallDetails
):
    """Namedtuple class to store metadata.

    It's impossible to change original metadata in ClientCallDetails object
    passed as a parameter to intercept method, so this class is going to get
    original metadata tuple and add the authorization one.
    """

    pass


class AuthTokenInterceptor(StreamStreamClientInterceptor):
    """Interceptor class to set authorization header.

    Set authorization header (but it can be any header also) for a gRPC call.
    """

    def __init__(self, token: str, header: str = "authorization") -> None:
        """Initialize interceptor.

        Set token and header which should be set into call. By default header is "authorization".
        Header have to be lowercase.

        Args:
            token (str): Token value to be set.
            header (str, optional): Header name. It must be lowercase. Defaults to "authorization".
        """
        self.token: str = token
        if not header.islower():
            raise ValueError("Header must be lowercase.")
        self.header: str = header

    def intercept_stream_stream(
        self, continuation: Callable, client_call_details: ClientCallDetails, request_iterator: Any
    ) -> Any:
        """Add header into metadata."""
        metadata: List = list(client_call_details.metadata) if client_call_details.metadata is not None else []
        metadata.append((self.header, self.token,))
        new_client_call_details: NewClientCallDetails = NewClientCallDetails(
            client_call_details.method, client_call_details.timeout, metadata, client_call_details.credentials
        )
        return continuation(new_client_call_details, request_iterator)
