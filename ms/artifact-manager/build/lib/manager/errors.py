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


class ArtifactManagerError(Exception):
    """Base Artifact Manager exception class."""

    status_code: int = 0
    message: str = "Error"

    def __init__(self, message: str = None) -> None:
        """Initialize exception with optional message."""
        if message:
            self.message: str = message

    @property
    def status_code(self) -> int:
        """Artifact Manager error class status code.

        Base class has no and shouldn't have any status code.
        """
        if self.status_code == 0:
            raise NotImplementedError
        return self.status_code


class InvalidRequestError(ArtifactManagerError):
    """Raised when request has invalid or incomplete data."""

    status_code: int = 500
    message: str = "Invalid request"


class ArtifactNotFoundError(ArtifactManagerError):
    """Raised when requested artifact doesn't exist in system."""

    status_code: int = 500
    message: str = "Artifact not found"


class ArtifactIOError(ArtifactManagerError):
    """Raised on input/output error."""

    status_code: int = 500
    message: str = "Artifact is corrupted"


class ArtifactOverwriteError(ArtifactManagerError):
    """Raised when we cannot remove old artifact to save new."""

    status_code: int = 500
    message: str = "Artifact already exists and cannot be overwritten"
