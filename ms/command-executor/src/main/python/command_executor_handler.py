#
# Copyright (C) 2019 Bell Canada.
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

import os
import subprocess
import venv
from builtins import Exception
from subprocess import CalledProcessError
import logging


def prepare_env(request, results):
    for package in request.packages:
        if not install(package, results):
            return False
    return True


def execute_command(request):
    return os.popen(request.command).read()


def install(package, results):
    command = ["pip", "install", package]
    try:
        results.append(subprocess.check_output(command, stderr=subprocess.STDOUT).decode())
        return True
    except CalledProcessError as e:
        results.append(e.output.decode())
        return False


def virtual_env(blueprint_name, blueprint_version):
    blueprint_id = blueprint_name + '/' + blueprint_version
    venv_home = '/opt/app/onap/blueprints/deploy/' + blueprint_id
    try:
        venv.create(venv_home, system_site_packages=True, with_pip=True)
    except Exception as err:
        logging.error("Failed to provision Python Virtual Environment for Blueprint: {}. Error: {}", blueprint_id, err)
