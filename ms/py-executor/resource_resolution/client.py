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
from logging import getLogger, Logger
from types import TracebackType
from typing import Iterable, List, Optional, Type

from grpc import Channel, insecure_channel, secure_channel, ssl_channel_credentials

from blueprints_grpc.proto.BluePrintProcessing_pb2 import ExecutionServiceInput, ExecutionServiceOutput
from blueprints_grpc.proto.BluePrintProcessing_pb2_grpc import BluePrintProcessingServiceStub


class Client:
    """Resource resoulution client class."""

    def __init__(
        self,
        server_address: str,
        *,
        use_ssl: bool = False,
        root_certificates: bytes = None,
        private_key: bytes = None,
        certificate_chain: bytes = None,
    ) -> None:
        """Client class initialization.

        :param server_address: Address to server to connect.
        :param use_ssl: Boolean flag to determine if secure channel should be created or not. Keyword argument.
        :param root_certificates: The PEM-encoded root certificates. None if it shouldn't be used. Keyword argument.
        :param private_key: The PEM-encoded private key as a byte string, or None if no private key should be used. Keyword argument.
        :param certificate_chain: The PEM-encoded certificate chain as a byte string to use or or None if no certificate chain should be used. Keyword argument.
        """
        self.logger = getLogger(__name__)
        if use_ssl:
            self.channel: Channel = secure_channel(
                server_address, ssl_channel_credentials(root_certificates, private_key, certificate_chain)
            )
            self.logger.debug(f"Create secure channel to connect with {server_address}")
        else:
            self.channel: Channel = insecure_channel(server_address)
            self.logger.debug(f"Create insecure channel to connect to {server_address}")
        self.stub: BluePrintProcessingServiceStub = BluePrintProcessingServiceStub(self.channel)

    def close(self) -> None:
        """Close client session.

        Closes client's channel.
        """
        self.logger.debug("Close channel connection")
        self.channel.close()

    def __enter__(self) -> Channel:
        """Enter Client instance context.

        Return Client instance. In the context user can call methods to communicate with server.
        On exit connection with the server is going to be closed.
        """
        self.logger.debug("Enter Client instance context")
        return self

    def __exit__(
        self,
        unused_exc_type: Optional[Type[BaseException]],
        unused_exc_value: Optional[BaseException],
        unused_traceback: Optional[TracebackType],
    ) -> None:
        """Exit Client instance context.

        Close connection with the server.
        """
        self.logger.debug("Exit Client instance context")
        self.close()

    def process(self, messages: Iterable[ExecutionServiceInput]) -> Iterable[ExecutionServiceOutput]:
        """Send messages to server and return responses.

        :param messages: Iterable messages to send
        :return: Iterable responses
        """
        for message in self.stub.process(messages):
            self.logger.debug(f"Get response message: {message}")
            yield message
