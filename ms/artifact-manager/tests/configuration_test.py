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
from configparser import NoOptionError
from pathlib import Path, PurePath
from typing import NoReturn

from pytest import raises

from manager.configuration import ArtifactManagerConfiguration


TEST_CONFIGURATION_FILE_PATH = str(
    PurePath(Path(__file__).parent.absolute(), "configuration-test.ini")
)


def test_server_configuration_configuration_file_path() -> NoReturn:
    """Test ArtifactManagerConfiguration class.

    Test checks if configuration file is loaded properly and returns valid values.
    If invalid section or option is provided it should raises KeyError or configparser.NoOptionError exceptions.
    :return: NoReturn
    """
    configuration: ArtifactManagerConfiguration = ArtifactManagerConfiguration(
        TEST_CONFIGURATION_FILE_PATH
    )
    assert configuration.get_section("testSection")
    with raises(KeyError):
        configuration.get_section("invalidSection")
    assert configuration.get_property("testSection", "testValue") == "123"
    with raises(NoOptionError):
        configuration.get_property("testSection", "invalidValue")
    assert configuration.artifact_manager_property("artifactManagerValue") == "123"
    with raises(NoOptionError):
        configuration.artifact_manager_property("invalidValue")
