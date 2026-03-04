#
# Copyright (C) 2026 Deutsche Telekom.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

"""
Tests for command_executor_handler.py — CommandExecutorHandler methods
covering blueprint upload, env preparation, command execution, venv
creation, and package installation.
"""

import io
import json
import os
import tempfile
import zipfile
from subprocess import CalledProcessError, PIPE
from types import SimpleNamespace
from unittest.mock import patch, MagicMock, mock_open, PropertyMock

import pytest

import proto.CommandExecutor_pb2 as CommandExecutor_pb2
import utils


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_identifiers(name="test-bp", version="1.0.0", uuid="uuid-1"):
    return SimpleNamespace(
        blueprintName=name,
        blueprintVersion=version,
        blueprintUUID=uuid,
    )


def _make_request(name="test-bp", version="1.0.0", uuid="uuid-1",
                  timeout=30, request_id="req-1", sub_request_id="sub-1",
                  originator_id="orig-1", correlation_id="corr-1",
                  command="", properties=None, packages=None,
                  archive_type="CBA_ZIP", bin_data=b""):
    return SimpleNamespace(
        identifiers=_make_identifiers(name, version, uuid),
        timeOut=timeout,
        requestId=request_id,
        subRequestId=sub_request_id,
        originatorId=originator_id,
        correlationId=correlation_id,
        command=command,
        properties=properties,
        packages=packages or [],
        archiveType=archive_type,
        binData=bin_data,
    )


def _create_valid_zip_bytes():
    """Create a valid in-memory zip file with a single entry."""
    buf = io.BytesIO()
    with zipfile.ZipFile(buf, 'w', zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("test.txt", "hello world")
    return buf.getvalue()


def _make_handler(request=None, skip_prometheus=True):
    """Create a CommandExecutorHandler with prometheus mocked out."""
    if request is None:
        request = _make_request()

    with patch("command_executor_handler.prometheus") as mock_prom, \
         patch.dict(os.environ, {}, clear=False):
        # Mock prometheus so it doesn't try to start an HTTP server
        mock_histogram = MagicMock()
        mock_counter = MagicMock()
        mock_prom.REGISTRY = MagicMock()
        mock_prom.REGISTRY._command_executor_histogram = mock_histogram
        mock_prom.REGISTRY._command_executor_counter = mock_counter
        mock_prom.REGISTRY._command_executor_prometheus_server_started = True
        mock_prom.Histogram.return_value = mock_histogram
        mock_prom.Counter.return_value = mock_counter

        from command_executor_handler import CommandExecutorHandler
        handler = CommandExecutorHandler(request)

    return handler


# ===================================================================
# __init__ and basic attribute tests
# ===================================================================

class TestHandlerInit:

    def test_basic_attributes(self):
        req = _make_request(name="bp-init", version="2.0.0", uuid="u-init",
                            timeout=60, request_id="r-init", sub_request_id="s-init")
        handler = _make_handler(req)
        assert handler.blueprint_name == "bp-init"
        assert handler.blueprint_version == "2.0.0"
        assert handler.uuid == "u-init"
        assert handler.execution_timeout == 60
        assert handler.request_id == "r-init"
        assert handler.sub_request_id == "s-init"

    def test_blueprint_dir_path(self):
        req = _make_request(name="my-bp", version="1.0.0", uuid="u1")
        handler = _make_handler(req)
        expected = "/opt/app/onap/blueprints/deploy/my-bp/1.0.0/u1"
        assert handler.blueprint_dir == expected

    def test_blueprint_name_version_uuid(self):
        req = _make_request(name="n", version="v", uuid="u")
        handler = _make_handler(req)
        assert handler.blueprint_name_version_uuid == "n/v/u"

    def test_blueprint_name_version_legacy(self):
        req = _make_request(name="n", version="v")
        handler = _make_handler(req)
        assert handler.blueprint_name_version == "n/v"


# ===================================================================
# is_installed / blueprint_dir_exists / blueprint_tosca_meta_file_exists
# ===================================================================

class TestFileChecks:

    def test_is_installed_true(self):
        handler = _make_handler()
        with patch("os.path.exists", return_value=True):
            assert handler.is_installed() is True

    def test_is_installed_false(self):
        handler = _make_handler()
        with patch("os.path.exists", return_value=False):
            assert handler.is_installed() is False

    def test_blueprint_dir_exists(self):
        handler = _make_handler()
        with patch("os.path.exists", return_value=True):
            assert handler.blueprint_dir_exists() is True

    def test_blueprint_tosca_meta_file_exists(self):
        handler = _make_handler()
        with patch("os.path.exists", return_value=False):
            assert handler.blueprint_tosca_meta_file_exists() is False


# ===================================================================
# is_valid_archive_type
# ===================================================================

class TestIsValidArchiveType:

    def test_cba_zip(self):
        handler = _make_handler()
        assert handler.is_valid_archive_type("CBA_ZIP") is True

    def test_cba_gzip(self):
        handler = _make_handler()
        assert handler.is_valid_archive_type("CBA_GZIP") is True

    def test_invalid_type(self):
        handler = _make_handler()
        assert handler.is_valid_archive_type("TAR") is False

    def test_empty_string(self):
        handler = _make_handler()
        assert handler.is_valid_archive_type("") is False


# ===================================================================
# err_exit
# ===================================================================

class TestErrExit:

    def test_returns_failure_data(self):
        handler = _make_handler()
        result = handler.err_exit("something broke")
        assert result[utils.CDS_IS_SUCCESSFUL_KEY] is False
        assert "something broke" in result[utils.ERR_MSG_KEY]


# ===================================================================
# uploadBlueprint
# ===================================================================

class TestUploadBlueprint:

    def test_invalid_archive_type(self):
        req = _make_request(archive_type="UNKNOWN")
        handler = _make_handler(req)
        upload_req = SimpleNamespace(archiveType="UNKNOWN", binData=b"",
                                     **{'__str__': lambda s: "req"})
        # Use request object with the right fields
        result = handler.uploadBlueprint(
            SimpleNamespace(archiveType="UNKNOWN", binData=b"")
        )
        assert result.status == CommandExecutor_pb2.FAILURE

    def test_successful_zip_upload(self):
        zip_bytes = _create_valid_zip_bytes()
        req = _make_request(archive_type="CBA_ZIP", bin_data=zip_bytes)
        handler = _make_handler(req)

        with patch("os.makedirs") as mock_mkdirs:
            upload_req = SimpleNamespace(archiveType="CBA_ZIP", binData=zip_bytes)
            # Mock ZipFile to avoid writing to filesystem
            with patch("command_executor_handler.ZipFile") as mock_zf:
                mock_zf_instance = MagicMock()
                mock_zf.return_value.__enter__ = MagicMock(return_value=mock_zf_instance)
                mock_zf.return_value.__exit__ = MagicMock(return_value=False)
                result = handler.uploadBlueprint(upload_req)

        assert result.status == CommandExecutor_pb2.SUCCESS

    def test_makedirs_failure(self):
        req = _make_request()
        handler = _make_handler(req)

        with patch("os.makedirs", side_effect=OSError("permission denied")):
            upload_req = SimpleNamespace(archiveType="CBA_ZIP", binData=b"data")
            result = handler.uploadBlueprint(upload_req)

        assert result.status == CommandExecutor_pb2.FAILURE

    def test_gzip_returns_failure_todo(self):
        """CBA_GZIP is recognized but not yet implemented, should return FAILURE."""
        req = _make_request()
        handler = _make_handler(req)

        with patch("os.makedirs"):
            upload_req = SimpleNamespace(archiveType="CBA_GZIP", binData=b"data")
            result = handler.uploadBlueprint(upload_req)

        assert result.status == CommandExecutor_pb2.FAILURE


# ===================================================================
# prepare_env
# ===================================================================

class TestPrepareEnv:

    def test_blueprint_dir_not_found(self):
        req = _make_request()
        handler = _make_handler(req)

        with patch.object(handler, 'blueprint_dir_exists', return_value=False):
            result = handler.prepare_env(req)

        assert result[utils.CDS_IS_SUCCESSFUL_KEY] is False
        assert result.get(utils.REUPLOAD_CBA_KEY) is True

    def test_tosca_meta_not_found(self):
        req = _make_request()
        handler = _make_handler(req)

        with patch.object(handler, 'blueprint_dir_exists', return_value=True), \
             patch.object(handler, 'blueprint_tosca_meta_file_exists', return_value=False):
            result = handler.prepare_env(req)

        assert result[utils.CDS_IS_SUCCESSFUL_KEY] is False
        assert result.get(utils.REUPLOAD_CBA_KEY) is True

    def test_already_installed_reads_file(self):
        req = _make_request()
        handler = _make_handler(req)

        with patch.object(handler, 'blueprint_dir_exists', return_value=True), \
             patch.object(handler, 'blueprint_tosca_meta_file_exists', return_value=True), \
             patch.object(handler, 'is_installed', return_value=True), \
             patch("command_executor_handler.open", mock_open(read_data="previously installed packages")):
            result = handler.prepare_env(req)

        assert result[utils.CDS_IS_SUCCESSFUL_KEY] is True
        assert "previously installed packages" in result[utils.RESULTS_LOG_KEY][0]

    def test_already_installed_read_failure(self):
        req = _make_request()
        handler = _make_handler(req)

        with patch.object(handler, 'blueprint_dir_exists', return_value=True), \
             patch.object(handler, 'blueprint_tosca_meta_file_exists', return_value=True), \
             patch.object(handler, 'is_installed', return_value=True), \
             patch("command_executor_handler.open", side_effect=IOError("read error")):
            result = handler.prepare_env(req)

        assert result[utils.CDS_IS_SUCCESSFUL_KEY] is False

    def test_create_venv_failure(self):
        req = _make_request()
        handler = _make_handler(req)

        with patch.object(handler, 'blueprint_dir_exists', return_value=True), \
             patch.object(handler, 'blueprint_tosca_meta_file_exists', return_value=True), \
             patch.object(handler, 'is_installed', return_value=False), \
             patch.object(handler, 'create_venv', return_value=utils.build_ret_data(False, error="venv error")):
            result = handler.prepare_env(req)

        assert result[utils.CDS_IS_SUCCESSFUL_KEY] is False

    def test_pip_upgrade_failure(self):
        req = _make_request()
        handler = _make_handler(req)

        with patch.object(handler, 'blueprint_dir_exists', return_value=True), \
             patch.object(handler, 'blueprint_tosca_meta_file_exists', return_value=True), \
             patch.object(handler, 'is_installed', return_value=False), \
             patch.object(handler, 'create_venv', return_value=utils.build_ret_data(True)), \
             patch.object(handler, 'upgrade_pip', return_value=False):
            result = handler.prepare_env(req)

        assert result[utils.CDS_IS_SUCCESSFUL_KEY] is False


# ===================================================================
# execute_command
# ===================================================================

class TestExecuteCommand:

    def test_successful_execution(self):
        req = _make_request(
            command="/opt/app/onap/blueprints/deploy/test-bp/1.0.0/uuid-1/Scripts/python/test.py arg1",
            timeout=30,
        )
        handler = _make_handler(req)

        mock_completed = MagicMock()
        mock_completed.returncode = 0

        with patch.object(handler, 'is_installed', return_value=True), \
             patch("os.utime"), \
             patch("subprocess.run", return_value=mock_completed), \
             patch("tempfile.TemporaryFile", return_value=tempfile.TemporaryFile(mode="w+")):
            result = handler.execute_command(req)

        assert result[utils.CDS_IS_SUCCESSFUL_KEY] is True

    def test_failed_execution_nonzero_rc(self):
        req = _make_request(
            command="python /opt/app/onap/blueprints/deploy/test-bp/1.0.0/uuid-1/Scripts/python/test.py",
            timeout=30,
        )
        handler = _make_handler(req)

        mock_completed = MagicMock()
        mock_completed.returncode = 1

        with patch.object(handler, 'is_installed', return_value=True), \
             patch("os.utime"), \
             patch("subprocess.run", return_value=mock_completed), \
             patch("tempfile.TemporaryFile", return_value=tempfile.TemporaryFile(mode="w+")):
            result = handler.execute_command(req)

        assert result[utils.CDS_IS_SUCCESSFUL_KEY] is False

    def test_timeout_execution(self):
        from subprocess import TimeoutExpired

        req = _make_request(
            command="python /opt/app/onap/blueprints/deploy/test-bp/1.0.0/uuid-1/Scripts/python/test.py",
            timeout=5,
        )
        handler = _make_handler(req)

        with patch.object(handler, 'is_installed', return_value=True), \
             patch("os.utime"), \
             patch("subprocess.run", side_effect=TimeoutExpired("cmd", 5)), \
             patch("tempfile.TemporaryFile", return_value=tempfile.TemporaryFile(mode="w+")):
            result = handler.execute_command(req)

        assert result[utils.CDS_IS_SUCCESSFUL_KEY] is False

    def test_general_exception(self):
        req = _make_request(
            command="python /opt/app/onap/blueprints/deploy/test-bp/1.0.0/uuid-1/Scripts/python/test.py",
            timeout=30,
        )
        handler = _make_handler(req)

        with patch.object(handler, 'is_installed', return_value=True), \
             patch("os.utime", side_effect=Exception("unexpected")):
            result = handler.execute_command(req)

        assert result[utils.CDS_IS_SUCCESSFUL_KEY] is False

    def test_sr7_compatibility_path_rewrite(self):
        """If request.command uses name/version but not UUID, UUID should be inserted."""
        req = _make_request(
            name="my-bp", version="1.0.0", uuid="u-1",
            command="python /opt/app/onap/blueprints/deploy/my-bp/1.0.0/Scripts/python/test.py",
            timeout=30,
        )
        handler = _make_handler(req)

        mock_completed = MagicMock()
        mock_completed.returncode = 0
        captured_cmd = []

        def capture_run(cmd, **kwargs):
            captured_cmd.append(cmd)
            return mock_completed

        with patch.object(handler, 'is_installed', return_value=True), \
             patch("os.utime"), \
             patch("subprocess.run", side_effect=capture_run), \
             patch("tempfile.TemporaryFile", return_value=tempfile.TemporaryFile(mode="w+")):
            handler.execute_command(req)

        # The command should have been rewritten to include the UUID
        assert "my-bp/1.0.0/u-1" in captured_cmd[0]

    def test_ansible_playbook_adds_interpreter(self):
        """ansible-playbook commands should get ansible_python_interpreter set."""
        req = _make_request(
            command="ansible-playbook /opt/app/onap/blueprints/deploy/test-bp/1.0.0/uuid-1/playbook.yml",
            timeout=30,
        )
        handler = _make_handler(req)

        mock_completed = MagicMock()
        mock_completed.returncode = 0
        captured_cmd = []

        def capture_run(cmd, **kwargs):
            captured_cmd.append(cmd)
            return mock_completed

        with patch.object(handler, 'is_installed', return_value=True), \
             patch("os.utime"), \
             patch("subprocess.run", side_effect=capture_run), \
             patch("tempfile.TemporaryFile", return_value=tempfile.TemporaryFile(mode="w+")):
            handler.execute_command(req)

        assert "ansible_python_interpreter=" in captured_cmd[0]


# ===================================================================
# create_venv
# ===================================================================

class TestCreateVenv:

    def test_successful_creation(self):
        handler = _make_handler()
        with patch("venv.create") as mock_create:
            result = handler.create_venv()

        assert result[utils.CDS_IS_SUCCESSFUL_KEY] is True
        mock_create.assert_called_once()

    def test_creation_failure(self):
        handler = _make_handler()
        with patch("venv.create", side_effect=Exception("venv failed")):
            result = handler.create_venv()

        assert result[utils.CDS_IS_SUCCESSFUL_KEY] is False
        assert "venv failed" in str(result.get(utils.ERR_MSG_KEY, ""))

    def test_system_site_packages_disabled(self):
        handler = _make_handler()
        with patch("venv.create") as mock_create, \
             patch.dict(os.environ, {"CREATE_VENV_DISABLE_SITE_PACKAGES": "1"}):
            handler.create_venv()

        call_kwargs = mock_create.call_args
        # system_site_packages should be False when env var is set
        assert call_kwargs[1].get("system_site_packages") is False or \
               (len(call_kwargs[0]) > 0 and call_kwargs[1].get("system_site_packages") is False)

    def test_system_site_packages_enabled_by_default(self):
        handler = _make_handler()
        env = os.environ.copy()
        env.pop("CREATE_VENV_DISABLE_SITE_PACKAGES", None)
        with patch("venv.create") as mock_create, \
             patch.dict(os.environ, env, clear=True):
            handler.create_venv()

        call_kwargs = mock_create.call_args
        assert call_kwargs[1].get("system_site_packages") is True


# ===================================================================
# upgrade_pip
# ===================================================================

class TestUpgradePip:

    def test_successful_upgrade(self):
        handler = _make_handler()
        results = []

        mock_result = MagicMock()
        mock_result.stdout = b"Successfully installed pip-23.0"

        with patch("subprocess.run", return_value=mock_result):
            success = handler.upgrade_pip(results)

        assert success is True
        assert "Successfully installed pip-23.0" in results[0]

    def test_failed_upgrade(self):
        handler = _make_handler()
        results = []

        with patch("subprocess.run", side_effect=CalledProcessError(
                1, "pip", stderr=b"pip upgrade failed")):
            success = handler.upgrade_pip(results)

        assert success is False
        assert "pip upgrade failed" in results[0]


# ===================================================================
# install_python_packages
# ===================================================================

class TestInstallPythonPackages:

    def test_successful_install(self):
        handler = _make_handler()
        results = []

        mock_result = MagicMock()
        mock_result.stdout = b"Successfully installed package-1.0"

        with patch("subprocess.run", return_value=mock_result):
            success = handler.install_python_packages("some-package", results)

        assert success is True

    def test_failed_install(self):
        handler = _make_handler()
        results = []

        with patch("subprocess.run", side_effect=CalledProcessError(
                1, "pip install", stderr=b"No matching distribution")):
            success = handler.install_python_packages("bad-package", results)

        assert success is False
        assert "No matching distribution" in results[0]

    def test_requirements_txt_uses_full_path(self):
        handler = _make_handler()
        results = []
        captured_cmd = []

        mock_result = MagicMock()
        mock_result.stdout = b"ok"

        def capture_run(cmd, **kwargs):
            captured_cmd.append(cmd)
            return mock_result

        with patch("subprocess.run", side_effect=capture_run):
            handler.install_python_packages("requirements.txt", results)

        # Should use the full path to pip and requirements.txt
        assert any("bin/pip" in str(c) for c in captured_cmd)
        assert any("requirements.txt" in str(c) for c in captured_cmd)

    def test_utility_package_uses_cp(self):
        handler = _make_handler()
        results = []
        captured_cmd = []

        mock_result = MagicMock()
        mock_result.stdout = b"ok"

        def capture_run(cmd, **kwargs):
            captured_cmd.append(cmd)
            return mock_result

        with patch("subprocess.run", side_effect=capture_run):
            handler.install_python_packages("UTILITY", results)

        assert captured_cmd[0][0] == "cp"

    def test_pip_install_user_flag(self):
        handler = _make_handler()
        results = []
        captured_cmd = []

        mock_result = MagicMock()
        mock_result.stdout = b"ok"

        def capture_run(cmd, **kwargs):
            captured_cmd.append(cmd)
            return mock_result

        with patch("subprocess.run", side_effect=capture_run), \
             patch.dict(os.environ, {"PIP_INSTALL_USER_FLAG": "1"}):
            handler.install_python_packages("some-package", results)

        assert "--user" in captured_cmd[0]


# ===================================================================
# install_ansible_packages
# ===================================================================

class TestInstallAnsiblePackages:

    def test_successful_install(self):
        handler = _make_handler()
        results = []

        mock_result = MagicMock()
        mock_result.stdout = b"Role installed successfully"

        with patch("subprocess.run", return_value=mock_result):
            success = handler.install_ansible_packages("my-role", results)

        assert success is True

    def test_failed_install(self):
        handler = _make_handler()
        results = []

        with patch("subprocess.run", side_effect=CalledProcessError(
                1, "ansible-galaxy", stderr=b"Role not found")):
            success = handler.install_ansible_packages("bad-role", results)

        assert success is False
        assert "Role not found" in results[0]

    def test_uses_ansible_galaxy_command(self):
        handler = _make_handler()
        results = []
        captured_cmd = []

        mock_result = MagicMock()
        mock_result.stdout = b"ok"

        def capture_run(cmd, **kwargs):
            captured_cmd.append(cmd)
            return mock_result

        with patch("subprocess.run", side_effect=capture_run):
            handler.install_ansible_packages("some-role", results)

        assert captured_cmd[0][0] == "ansible-galaxy"
        assert "install" in captured_cmd[0]
        assert "some-role" in captured_cmd[0]

    def test_http_proxy_is_passed(self):
        handler = _make_handler()
        results = []
        captured_env = []

        mock_result = MagicMock()
        mock_result.stdout = b"ok"

        def capture_run(cmd, **kwargs):
            captured_env.append(kwargs.get("env", {}))
            return mock_result

        with patch("subprocess.run", side_effect=capture_run), \
             patch.dict(os.environ, {"http_proxy": "http://proxy:8080"}):
            handler.install_ansible_packages("role", results)

        assert captured_env[0].get("https_proxy") == "http://proxy:8080"
