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

import os
import shutil
from abc import ABC, abstractmethod
from io import BytesIO
from pathlib import Path
from zipfile import ZipFile, is_zipfile

from manager.configuration import config
from manager.errors import ArtifactNotFoundError, ArtifactOverwriteError, InvalidRequestError


class Repository(ABC):
    """Abstract repository class.

    Defines repository methods.
    """

    @abstractmethod
    def upload_blueprint(self, file: bytes, name: str, version: str) -> None:
        """Store blueprint file in the repository.

        :param file: File to save
        :param name: Blueprint name
        :param version: Blueprint version
        """

    @abstractmethod
    def download_blueprint(self, name: str, version: str) -> bytes:
        """Download blueprint file from repository.

        :param name: Blueprint name
        :param version: Blueprint version
        :return: Zipped Blueprint file bytes
        """

    @abstractmethod
    def remove_blueprint(self, name: str, version: str) -> None:
        """Remove blueprint file from repository.

        :param name: Blueprint name
        :param version: Blueprint version
        """


class FileRepository(Repository):
    """Store blueprints on local directory."""

    base_path = None

    def __init__(self, base_path: Path) -> None:
        """Initialize the repository while passing the needed path.

        :param base_path: Local OS path on which blueprint files reside.
        """
        self.base_path = base_path

    def __remove_directory_tree(self, full_path: str) -> None:
        """Remove specified path.

        :param full_path: Full path to a directory.
        :raises: FileNotFoundError
        """
        try:
            shutil.rmtree(full_path, ignore_errors=False)
        except OSError:
            raise ArtifactNotFoundError

    def __create_directory_tree(self, full_path: str, mode: int = 0o744, retry_on_error: bool = True) -> None:
        """Create directory or overwrite existing one.

        This method will handle a directory tree creation. If there is a collision
        in directory structure - old directory tree will be removed
        and creation will be attempted one more time. If the creation fails for the second time
        the exception will be raised.

        :param full_path: Full directory tree path (eg. one/two/tree) as string.
        :param mode: Permission mask for the directories.
        :param retry_on_error: Flag that indicates if there should be a attempt to retry the operation.
        """
        try:
            os.makedirs(full_path, mode=mode)
        except FileExistsError:
            # In this case we know that cba of same name and version need to be overwritten
            if retry_on_error:
                self.__remove_directory_tree(full_path)
                self.__create_directory_tree(full_path, mode=mode, retry_on_error=False)
            else:
                # This way we won't try for ever if something goes wrong
                raise ArtifactOverwriteError

    def upload_blueprint(self, cba_bytes: bytes, name: str, version: str) -> None:
        """Store blueprint file in the repository.

        :param cba_bytes: Bytes to save
        :param name: Blueprint name
        :param version: Blueprint version
        """
        temporary_file: BytesIO = BytesIO(cba_bytes)

        if not is_zipfile(temporary_file):
            raise InvalidRequestError

        target_path: str = str(Path(self.base_path.absolute(), name, version))
        self.__create_directory_tree(target_path)

        with ZipFile(temporary_file, "r") as zip_file:  # type: ZipFile
            zip_file.extractall(target_path)

    def download_blueprint(self, name: str, version: str) -> bytes:
        """Download blueprint file from repository.

        This method does the in-memory zipping the files and returns bytes

        :param name: Blueprint name
        :param version: Blueprint version
        :return: Zipped Blueprint file bytes
        """
        temporary_file: BytesIO = BytesIO()
        files_path: str = str(Path(self.base_path.absolute(), name, version))
        if not os.path.exists(files_path):
            raise ArtifactNotFoundError

        with ZipFile(temporary_file, "w") as zip_file:  # type: ZipFile
            for directory_name, subdirectory_names, filenames in os.walk(files_path):  # type: str, list, list
                for filename in filenames:  # type: str
                    zip_file.write(Path(directory_name, filename))

        # Rewind the fake file to allow reading
        temporary_file.seek(0)

        zip_as_bytes: bytes = temporary_file.read()
        temporary_file.close()
        return zip_as_bytes

    def remove_blueprint(self, name: str, version: str) -> None:
        """Remove blueprint file from repository.

        :param name: Blueprint name
        :param version: Blueprint version
        :raises: FileNotFoundError
        """
        files_path: str = str(Path(self.base_path.absolute(), name, version))
        self.__remove_directory_tree(files_path)


class RepositoryStrategy(ABC):
    """Strategy class.

    It has only one public method `get_repository`, which returns valid repository
    instance for the the configuration value.
    You can create many Repository subclasses, but repository clients doesn't have
    to know which one you use.
    """

    @classmethod
    def get_reporitory(cls) -> Repository:
        """Get the valid repository instance for the configuration value.

        Currently it returns FileRepository because it is an only Repository implementation.
        """
        return FileRepository(Path(config["artifactManagerServer"]["fileRepositoryBasePath"]))
