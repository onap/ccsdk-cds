import os
import shutil
import zipfile
from unittest.mock import patch

import manager.utils
from manager.utils import FileRepository, Repository, RepositoryStrategy


class MockZipFile(zipfile.ZipFile):
    def __init__(self, *args, **kwargs):
        pass

    def extractall(self, path: str) -> None:
        pass

    def write(self, *arg, **kwargs) -> None:
        pass


def test_fetch_proper_repository():
    repo: Repository = RepositoryStrategy.get_reporitory()
    assert repo.__class__ is FileRepository


def test_blueprint_upload():
    repo: Repository = RepositoryStrategy.get_reporitory()
    # fmt: off
    with patch.object(manager.utils, "is_zipfile", return_value=True) as mock_is_zip, \
            patch.object(os, "makedirs", return_value=None) as mock_mkdirs, \
            patch.object(manager.utils, 'ZipFile', return_value=MockZipFile()
        ):
        repo.upload_blueprint(b"abcd", "test_cba", "1.0.a")
        mock_is_zip.assert_called_once()
        mock_mkdirs.assert_called_once_with('/tmp/test_cba/1.0.a', mode=0o744)
    # fmt: on


def test_blueprint_download():
    repo: Repository = RepositoryStrategy.get_reporitory()
    mock_path = [
        ("test_cba", ["1.0.a"], []),
        ("test_cba/1.0.a", [], ["file.txt"]),
    ]
    # fmt: off
    with patch.object(os, "walk", return_value=mock_path) as mock_walk, \
            patch.object(manager.utils, 'ZipFile', return_value=MockZipFile()), \
            patch.object(os.path, 'exists', return_value=True
        ):
        repo.download_blueprint("test_cba", "1.0.a")
        mock_walk.assert_called_once_with('/tmp/test_cba/1.0.a')
    # fmt: on


def test_remove_blueprint():
    repo: Repository = RepositoryStrategy.get_reporitory()
    with patch.object(shutil, "rmtree", return_value=None) as mock_rmtree:
        repo.remove_blueprint("cba", "1.0a")
        mock_rmtree.assert_called_once()
