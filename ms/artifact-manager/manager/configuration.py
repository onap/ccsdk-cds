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
import logging
import os
from configparser import ConfigParser, SectionProxy
from distutils.util import strtobool
from logging import Logger
from pathlib import Path, PurePath
from typing import NoReturn

from onaplogging import monkey
from onaplogging.mdcformatter import MDCFormatter  # noqa

monkey.patch_loggingYaml()


DEFAUL_CONFIGURATION_FILE: str = str(PurePath(Path().absolute(), "../configuration.ini"))
SUPPLIED_CONFIGURATION_FILE: str = os.environ.get("CONFIGURATION")
CONFIGURATION_FILE: str = str(os.path.expanduser(Path(SUPPLIED_CONFIGURATION_FILE or DEFAUL_CONFIGURATION_FILE)))


class ArtifactManagerConfiguration:
    """ServerConfiguration class loads configuration from config INI files."""

    def __init__(self, config_file_path: str) -> NoReturn:
        """Initialize configuration class instance.

        Configuration is loaded from file provided as a parameter. Environment variables are loaded also.
        Logger for object is created with the name same as the class name.
        :param config_file_path: Path to configuration file.
        """
        self.config_file_path = config_file_path
        self.config = ConfigParser(os.environ)
        self.config.read(config_file_path, encoding="utf-8")

    @property
    def configuration_directory(self) -> str:
        """Get directory path to a directory with configuration ini file.

        This is used to handle relative file paths in config file.
        """
        return os.path.dirname(self.config_file_path)

    def get_section(self, section_name: str) -> SectionProxy:
        """Get the section from configuration file.

        :param section_name: Name of the section to get
        :raises: KeyError
        :return: SectionProxy object for given section name
        """
        return self.config[section_name]

    def __getitem__(self, key: str) -> SectionProxy:
        """Get the section from configuration file.

        This method just calls the get_section method but allows us to use it as key lookup

        :param section_name: Name of the section to get
        :raises: KeyError
        :return: SectionProxy object for given section name
        """
        return self.get_section(key)

    def get_property(self, section_name: str, property_name: str) -> str:
        """Get the property value from *section_name* section.

        :param section_name: Name of the section config file section on which property is set
        :param property_name: Name of the property to get
        :raises: configparser.NoOptionError
        :return: String value of the property
        """
        return self.config.get(section_name, property_name)

    def artifact_manager_property(self, property_name: str) -> str:
        """Get the property value from *artifactManagerServer* section.

        :param property_name: Name of the property to get
        :raises: configparser.NoOptionError
        :return: String value of the property
        """
        return self.config.get("artifactManagerServer", property_name)


config = ArtifactManagerConfiguration(CONFIGURATION_FILE)


def prepare_logger(log_file_path: str, development_mode: bool, config: ArtifactManagerConfiguration) -> callable:
    """Base MDC logger configuration.

    Level depends on the *development_mode* flag: DEBUG if development_mode is set or INFO otherwise.
    Console handler is created from MDC settings from onappylog library.

    :param log_file_path: Path to the log file, where logs are going to be saved.
    :param development_mode: Boolean type value which means if logger should be setup in development mode or not
    :param config: Configuration class so we can fetch app settings (paths) to logger.
    :return: callable
    """
    logging_level: int = logging.DEBUG if development_mode else logging.INFO
    logging.basicConfig(filename=log_file_path, level=logging_level)
    logging.config.yamlConfig(
        filepath=Path(config.configuration_directory, config["artifactManagerServer"]["logConfig"])
    )

    console: logging.StreamHandler = logging.StreamHandler()
    console.setLevel(logging_level)
    formatter: logging.Formatter = MDCFormatter(
        fmt="%(asctime)s:[%(name)s] %(created)f %(module)s %(funcName)s %(pathname)s %(process)d %(levelno)s :[ %(threadName)s  %(thread)d]: [%(mdc)s]: [%(filename)s]-[%(lineno)d] [%(levelname)s]:%(message)s",  # noqa
        mdcfmt="{RequestID} {InvocationID} {ServiceName} {PartnerName} {BeginTimestamp} {EndTimestamp} {ElapsedTime} {StatusCode} {TargetEntity} {TargetServiceName} {Server}",  # noqa
        # Important: We cannot use %f here because this datetime format is used by time library, not datetime.
        datefmt="%Y-%m-%dT%H:%M:%S%z",
    )
    console.setFormatter(formatter)

    def get_logger(name: str) -> Logger:
        """Get a new logger with predefined MDC handler."""
        logger: Logger = logging.getLogger(name)
        logger.addHandler(console)
        return logger

    return get_logger


get_logger = prepare_logger(
    config.artifact_manager_property("logFile"),
    strtobool(config["artifactManagerServer"].get("debug", "false")),
    config,
)
