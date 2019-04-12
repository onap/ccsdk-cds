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
import virtualenv
import venv
from builtins import Exception, open, dict
from subprocess import CalledProcessError, PIPE

import utils


class CommandExecutorHandler:

    def __init__(self, request):
        self.blueprint_id = utils.get_blueprint_id(request)
        self.venv_home = '/opt/app/onap/blueprints/deploy/' + self.blueprint_id

    def prepare_env(self, request, results):
        self.create_venv()
        if not self.activate_venv():
            return False

        for package in request.packages:
            if not self.install(package, results):
                return False

        # deactivate_venv(blueprint_id)
        return True

    def execute_command(self, request, results):
        if not self.activate_venv():
            return False

        try:
            results.append(os.popen(request.command).read())
        except Exception as e:
            print("{} - Failed to execute command. Error: {}".format(self.blueprint_id, e))
            results.append(e)
            return False

        # deactivate_venv(blueprint_id)
        return True

    def install(self, package, results):
        print("{} - Install package({}) in Python Virtual Environment".format(self.blueprint_id, package))
        command = ["pip", "install", package]

        env = dict(os.environ)
        # env['https_proxy'] = "https://fastweb.int.bell.ca:8083"

        try:
            results.append(subprocess.run(command, check=True, stdout=PIPE, stderr=PIPE, env=env).stdout.decode())
            return True
        except CalledProcessError as e:
            results.append(e.stderr.decode())
            return False

    def create_venv(self):
        print("{} - Create Python Virtual Environment".format(self.blueprint_id))
        try:
            bin_dir = self.venv_home + "/bin"
            # venv doesn't populate the activate_this.py script, hence we use from virtualenv
            venv.create(self.venv_home, with_pip=True, system_site_packages=True)
            virtualenv.writefile(os.path.join(bin_dir, "activate_this.py"), virtualenv.ACTIVATE_THIS)
        except Exception as err:
            print("{} - Failed to provision Python Virtual Environment. Error: {}".format(self.blueprint_id, err))

    def activate_venv(self):
        print("{} - Activate Python Virtual Environment".format(self.blueprint_id))

        path = "%s/bin/activate_this.py" % self.venv_home
        try:
            exec (open(path).read(), {'__file__': path})
            return True
        except Exception as err:
            print("{} - Failed to activate Python Virtual Environment. Error: {}".format(self.blueprint_id, err))
            return False

    def deactivate_venv(self):
        print("{} - Deactivate Python Virtual Environment".format(self.blueprint_id))
        command = ["deactivate"]
        try:
            subprocess.run(command, check=True)
        except Exception as err:
            print("{} - Failed to deactivate Python Virtual Environment. Error: {}".format(self.blueprint_id, err))
