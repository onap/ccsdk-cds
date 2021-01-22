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
import zipfile
from unittest.mock import patch

import manager.utils
from manager.servicer import ArtifactManagerServicer
from proto.BlueprintCommon_pb2 import ActionIdentifiers, CommonHeader
from proto.BlueprintManagement_pb2 import (
    BlueprintDownloadInput,
    BlueprintManagementOutput,
    BlueprintRemoveInput,
    BlueprintUploadInput,
    FileChunk,
)
from proto.BlueprintManagement_pb2_grpc import (
    BlueprintManagementServiceStub,
    add_BlueprintManagementServiceServicer_to_server,
)
from pytest import fixture

ZIP_FILE_BINARY = b"PK\x05\x06\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00"


class MockZipFile(zipfile.ZipFile):
    def __init__(self, *args, **kwargs):
        pass

    def extractall(self, path: str) -> None:
        pass

    def write(self, *arg, **kwargs) -> None:
        pass


@fixture(scope="module")
def grpc_add_to_server():
    """pytest-grpcio required function."""
    return add_BlueprintManagementServiceServicer_to_server


@fixture(scope="module")
def grpc_servicer():
    """pytest-grpcio required function."""
    return ArtifactManagerServicer()


@fixture(scope="module")  # noqa
def grpc_stub_cls(grpc_channel):
    """pytest-grpcio required function."""
    return BlueprintManagementServiceStub


def test_servicer_upload_handler_header_failure(grpc_stub):
    """Test servicer upload handler."""
    request: BlueprintUploadInput = BlueprintUploadInput()
    output: BlueprintManagementOutput = grpc_stub.uploadBlueprint(request)
    assert output.status.code == 500
    assert output.status.message == "failure"
    assert output.status.errorMessage == "Request has to have set both Blueprint name and version"


def test_servicer_download_handler_header_failure(grpc_stub):
    """Test servicer download handler."""
    request: BlueprintDownloadInput = BlueprintDownloadInput()
    output: BlueprintManagementOutput = grpc_stub.downloadBlueprint(request)
    assert output.status.code == 500
    assert output.status.message == "failure"
    assert output.status.errorMessage == "Request has to have set both Blueprint name and version"


def test_servicer_remove_handler_header_failure(grpc_stub):
    """Test servicer remove handler."""
    request: BlueprintRemoveInput = BlueprintRemoveInput()
    output: BlueprintManagementOutput = grpc_stub.removeBlueprint(request)
    assert output.status.code == 500
    assert output.status.message == "failure"
    assert output.status.errorMessage == "Request has to have set both Blueprint name and version"


def test_servicer_upload_handler_failure(grpc_stub):
    """Test servicer upload handler."""
    action_identifiers: ActionIdentifiers = ActionIdentifiers()
    action_identifiers.blueprintName = "sample-cba"
    action_identifiers.blueprintVersion = "1.0.0"
    request: BlueprintUploadInput = BlueprintUploadInput(actionIdentifiers=action_identifiers)
    output: BlueprintManagementOutput = grpc_stub.uploadBlueprint(request)
    assert output.status.code == 500
    assert output.status.message == "failure"
    assert output.status.errorMessage == "Invalid request"


def test_servicer_download_handler_failure(grpc_stub):
    """Test servicer download handler."""
    action_identifiers: ActionIdentifiers = ActionIdentifiers()
    action_identifiers.blueprintName = "sample-cba"
    action_identifiers.blueprintVersion = "2.0.0"
    request: BlueprintDownloadInput = BlueprintDownloadInput(actionIdentifiers=action_identifiers)
    output: BlueprintManagementOutput = grpc_stub.downloadBlueprint(request)
    assert output.status.code == 500
    assert output.status.message == "failure"
    assert output.status.errorMessage == "Artifact not found"


def test_servicer_remove_handler_failure(grpc_stub):
    """Test servicer remove handler."""
    action_identifiers: ActionIdentifiers = ActionIdentifiers()
    action_identifiers.blueprintName = "sample-cba"
    action_identifiers.blueprintVersion = "1.0.0"
    request: BlueprintRemoveInput = BlueprintRemoveInput(actionIdentifiers=action_identifiers)
    output: BlueprintManagementOutput = grpc_stub.removeBlueprint(request)
    assert output.status.code == 500
    assert output.status.message == "failure"
    assert output.status.errorMessage == "Artifact not found"


def test_servicer_upload_handler_success(grpc_stub):
    """Test servicer upload handler."""
    header: CommonHeader = CommonHeader()
    header.requestId = "1234"
    header.subRequestId = "1234-1"
    header.originatorId = "CDS"

    action_identifiers: ActionIdentifiers = ActionIdentifiers()
    action_identifiers.blueprintName = "sample-cba"
    action_identifiers.blueprintVersion = "1.0.0"
    action_identifiers.actionName = "SampleScript"

    file_chunk = FileChunk()
    file_chunk.chunk = ZIP_FILE_BINARY

    # fmt: off
    with patch.object(os, "makedirs", return_value=None), \
            patch.object(manager.utils, 'ZipFile', return_value=MockZipFile()):
        request: BlueprintUploadInput = BlueprintUploadInput(
            commonHeader=header, fileChunk=file_chunk, actionIdentifiers=action_identifiers
        )
        output: BlueprintManagementOutput = grpc_stub.uploadBlueprint(request)
    # fmt: on
    assert output.status.code == 200
    assert output.status.message == "success"


def test_servicer_download_handler_success(grpc_stub):
    """Test servicer download handler."""
    header: CommonHeader = CommonHeader()
    header.requestId = "1234"
    header.subRequestId = "1234-1"
    header.originatorId = "CDS"

    action_identifiers: ActionIdentifiers = ActionIdentifiers()
    action_identifiers.blueprintName = "sample-cba"
    action_identifiers.blueprintVersion = "1.0.0"
    action_identifiers.actionName = "SampleScript"

    with patch.object(os.path, "exists", return_value=True):
        request: BlueprintDownloadInput = BlueprintDownloadInput(
            commonHeader=header, actionIdentifiers=action_identifiers
        )
        output: BlueprintManagementOutput = grpc_stub.downloadBlueprint(request)
    assert output.status.code == 200
    assert output.status.message == "success"
    assert output.fileChunk.chunk == ZIP_FILE_BINARY


def test_servicer_remove_handler_success(grpc_stub):
    """Test servicer remove handler."""
    header: CommonHeader = CommonHeader()
    header.requestId = "1234"
    header.subRequestId = "1234-1"
    header.originatorId = "CDS"

    action_identifiers: ActionIdentifiers = ActionIdentifiers()
    action_identifiers.blueprintName = "sample-cba"
    action_identifiers.blueprintVersion = "1.0.0"
    action_identifiers.actionName = "SampleScript"

    with patch.object(shutil, "rmtree", return_value=None) as mock_rmtree:
        request: BlueprintRemoveInput = BlueprintRemoveInput(commonHeader=header, actionIdentifiers=action_identifiers)
        output: BlueprintManagementOutput = grpc_stub.removeBlueprint(request)
    assert output.status.code == 200
    assert output.status.message == "success"
