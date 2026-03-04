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
Tests for blueprints_grpc/script_executor_configuration.py —
INI-based configuration reader for the script executor.
"""

import os
import pytest

from blueprints_grpc.script_executor_configuration import ScriptExecutorConfiguration


# ---------------------------------------------------------------------------
# ScriptExecutorConfiguration tests
# ---------------------------------------------------------------------------

class TestScriptExecutorConfiguration:

    @pytest.fixture
    def config_file(self, tmp_path):
        """Create a minimal config file and return its path."""
        p = tmp_path / "test-config.ini"
        p.write_text(
            "[scriptExecutor]\n"
            "port=50052\n"
            "authType=basic-auth\n"
            "token=my-secret-token\n"
            "logFile=app.log\n"
            "maxWorkers=10\n"
            "\n"
            "[blueprintsprocessor]\n"
            "blueprintDeployPath=/opt/blueprints/deploy\n"
            "blueprintArchivePath=/opt/blueprints/archive\n"
        )
        return str(p)

    def test_init_reads_file(self, config_file):
        config = ScriptExecutorConfiguration(config_file)
        assert config.config is not None

    def test_get_section(self, config_file):
        config = ScriptExecutorConfiguration(config_file)
        section = config.get_section("scriptExecutor")
        assert section["port"] == "50052"
        assert section["authtype"] == "basic-auth"

    def test_get_property(self, config_file):
        config = ScriptExecutorConfiguration(config_file)
        assert config.get_property("scriptExecutor", "port") == "50052"
        assert config.get_property("blueprintsprocessor", "blueprintDeployPath") == "/opt/blueprints/deploy"

    def test_script_executor_property(self, config_file):
        config = ScriptExecutorConfiguration(config_file)
        assert config.script_executor_property("port") == "50052"
        assert config.script_executor_property("authType") == "basic-auth"
        assert config.script_executor_property("token") == "my-secret-token"
        assert config.script_executor_property("maxWorkers") == "10"

    def test_blueprints_processor(self, config_file):
        config = ScriptExecutorConfiguration(config_file)
        assert config.blueprints_processor("blueprintDeployPath") == "/opt/blueprints/deploy"
        assert config.blueprints_processor("blueprintArchivePath") == "/opt/blueprints/archive"

    def test_missing_section_raises(self, config_file):
        config = ScriptExecutorConfiguration(config_file)
        with pytest.raises(KeyError):
            config.get_section("nonexistent")

    def test_missing_property_raises(self, config_file):
        config = ScriptExecutorConfiguration(config_file)
        from configparser import NoOptionError
        with pytest.raises(NoOptionError):
            config.get_property("scriptExecutor", "nonexistent_key")

    def test_env_vars_interpolated(self, tmp_path, monkeypatch):
        """ConfigParser with os.environ should interpolate env vars."""
        monkeypatch.setenv("TEST_DEPLOY_PATH", "/custom/path")
        p = tmp_path / "env-config.ini"
        p.write_text(
            "[blueprintsprocessor]\n"
            "blueprintDeployPath=%(TEST_DEPLOY_PATH)s/blueprints\n"
            "[scriptExecutor]\nport=50052\n"
        )
        config = ScriptExecutorConfiguration(str(p))
        assert config.blueprints_processor("blueprintDeployPath") == "/custom/path/blueprints"

    def test_nonexistent_file_results_in_empty_config(self, tmp_path):
        """ConfigParser.read silently ignores missing files."""
        config = ScriptExecutorConfiguration(str(tmp_path / "does-not-exist.ini"))
        # Config is initialized but has no sections
        assert config.config.sections() == []
