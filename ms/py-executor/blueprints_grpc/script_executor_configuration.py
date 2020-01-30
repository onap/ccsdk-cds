#!/usr/bin/python
#
#  Copyright © 2018-2019 AT&T Intellectual Property.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import os
import logging
import configparser
from pathlib import Path, PurePath


class ScriptExecutorConfiguration:

    def __init__(self, file_path: str):
        self.logger = logging.getLogger(self.__class__.__name__)
        self.logger.info('loading configuration file : {}'.format(file_path))
        self.config = configparser.ConfigParser(os.environ)
        self.config.read(file_path, encoding='utf-8')

    def get_section(self, section_name: str):
        return self.config[section_name]

    def get_property(self, section_name: str, property_name: str):
        return self.config.get(section_name, property_name)

    def script_executor_property(self, property_name: str):
        return self.config.get('scriptExecutor', property_name)

    def blueprints_processor(self, property_name: str):
        return self.config.get('blueprintsprocessor', property_name)


if __name__ == '__main__':
    default_configuration_file = str(PurePath(Path().absolute(), "../../configuration.ini"))
    supplied_configuration_file = os.environ.get('CONFIGURATION')
    config_file = str(os.path.expanduser(Path(supplied_configuration_file or default_configuration_file)))
    scriptExecutorConfiguration = ScriptExecutorConfiguration(config_file)
    blueprintDeployPath = scriptExecutorConfiguration.get_property('blueprintsprocessor', 'blueprintDeployPath')
    print(blueprintDeployPath)
