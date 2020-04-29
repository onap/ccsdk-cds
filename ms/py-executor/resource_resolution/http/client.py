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
from typing import Optional, Tuple

from requests import Session, request, Request, Response, PreparedRequest


class Client:
    """HTTP client class."""

    API_VERSION = "v1"

    def __init__(
        self, server_address: str, server_port: int, auth_user: str = None, auth_pass: str = None, use_ssl: bool = False
    ) -> None:
        """HTTP client class initialization.
        
        Args:
            server_address (str): HTTP server address
            server_port (int): HTTP server port
            auth_user (str, optional): Username used for authorization. Defaults to None.
            auth_pass (str, optional): Password used for authorization. Defaults to None.
            use_ssl (bool, optional): Determines if secure connection has to be used. Defaults to False.
        """
        self.server_address: str = server_address
        self.server_port: int = server_port
        self.use_ssl: bool = use_ssl

        self.auth_user: str = auth_user
        self.auth_pass: str = auth_pass

    @property
    def auth(self) -> Optional[Tuple[str, str]]:
        """Authorization data tuple or None.

        Returns None if not both auth_user and auth_pass values are set.
        
        Returns:
            Optional[Tuple[str, str]]: Authorization tuple (auth_user, auth_pass) or None
        """
        if all([self.auth_user, self.auth_pass]):
            return (self.auth_user, self.auth_pass)
        return None

    @property
    def protocol(self) -> str:
        """Protocol which is going to be used for request call.
        
        Returns:
            str: http or https
        """
        if self.use_ssl:
            return "https"
        return "http"

    @property
    def url(self) -> str:
        """Url to call requests.
        
        Returns:
            str: Url string
        """
        return f"{self.protocol}://{self.server_address}:{self.server_port}/api/{self.API_VERSION}"

    def send_request(self, method: str, endpoint: str, **kwargs) -> Response:
        """Send request to server.

        Send request with `method` method to server. Pass any additional values as **kwargs.

        Args:
            method (str): HTTP method
            endpoint (str): Endpoint to call a request

        Raises:
            requests.HTTPError: An HTTP error occurred.

        Returns:
            Response: `requests.Response` object.
        """
        response: Response = request(
            method=method, url=f"{self.url}/{endpoint}", verify=False, auth=self.auth, **kwargs
        )
        response.raise_for_status()
        return response
