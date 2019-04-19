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
from builtins import Exception, open, dict
from subprocess import CalledProcessError, PIPE

import logging
import os
import subprocess
import virtualenv
import venv
import utils
import proto.CommandExecutor_pb2 as CommandExecutor_pb2


class CommandExecutorHandler():

    def __init__(self, request):
        self.request = request
        self.logger = logging.getLogger(self.__class__.__name__)
        self.blueprint_id = utils.get_blueprint_id(request)
        self.venv_home = '/Users/adetalhouet/onap/master/cds/ms/deploy/' + self.blueprint_id
        self.installed = self.venv_home + '/.installed'

    def is_installed(self):
        if os.path.exists(self.installed):
            return True
        else:
            return False

    def prepare_env(self, request, results):
        if not self.is_installed():
            self.create_venv()
            if not self.activate_venv():
                return False

            f = open(self.installed, "w+")
            if not self.install_packages(request, CommandExecutor_pb2.pip, f, results):
                return False
            f.write("\r\n")
            results.append("\n")
            if not self.install_packages(request, CommandExecutor_pb2.ansible_galaxy, f, results):
                return False
            f.close()
        else:
            f = open(self.installed, "r")
            results.append(f.read())
            f.close()

        # deactivate_venv(blueprint_id)
        return True

    def execute_command(self, request, results):
        # if not self.activate_venv():
        #     return False

        try:
            results.append(os.popen(request.command).read())
        except Exception as e:
            self.logger.info("{} - Failed to execute command. Error: {}".format(self.blueprint_id, e))
            results.append(e)
            return False

        # deactivate_venv(blueprint_id)
        return True

    def install_packages(self, request, type, f, results):
        for package in request.packages:
            if package.type == type:
                f.write("Installed %s packages:\r\n" % CommandExecutor_pb2.PackageType.Name(type))
                for python_package in package.package:
                    f.write("   %s\r\n" % python_package)
                    if package.type == CommandExecutor_pb2.pip:
                        success = self.install_python_packages(python_package, results)
                    else:
                        success = self.install_ansible_packages(python_package, results)
                    if not success:
                        f.close()
                        os.remove(self.installed)
                        return False
        return True

    def install_python_packages(self, package, results):
        self.logger.info(
            "{} - Install Python package({}) in Python Virtual Environment".format(self.blueprint_id, package))
        command = ["pip", "install", package]

        env = dict(os.environ)
        if "https_proxy" in os.environ:
            env['https_proxy'] = os.environ['https_proxy']

        try:
            results.append(subprocess.run(command, check=True, stdout=PIPE, stderr=PIPE, env=env).stdout.decode())
            results.append("\n")
            return True
        except CalledProcessError as e:
            results.append(e.stderr.decode())
            return False

    def install_ansible_packages(self, package, results):
        self.logger.info(
            "{} - Install Ansible Role package({}) in Python Virtual Environment".format(self.blueprint_id, package))
        command = ["ansible-galaxy", "install", package, "-p", self.venv_home + "/Scripts/ansible/roles"]

        env = dict(os.environ)
        if "http_proxy" in os.environ:
            # ansible galaxy uses https_proxy environment variable, but requires it to be set with http proxy value.
            env['https_proxy'] = os.environ['http_proxy']

        try:
            results.append(subprocess.run(command, check=True, stdout=PIPE, stderr=PIPE, env=env).stdout.decode())
            results.append("\n")
            return True
        except CalledProcessError as e:
            results.append(e.stderr.decode())
            return False

    def create_venv(self):
        self.logger.info("{} - Create Python Virtual Environment".format(self.blueprint_id))
        try:
            bin_dir = self.venv_home + "/bin"
            # venv doesn't populate the activate_this.py script, hence we use from virtualenv
            venv.create(self.venv_home, with_pip=True, system_site_packages=True)
            virtualenv.writefile(os.path.join(bin_dir, "activate_this.py"), virtualenv.ACTIVATE_THIS)
        except Exception as err:
            self.logger.info(
                "{} - Failed to provision Python Virtual Environment. Error: {}".format(self.blueprint_id, err))

    def activate_venv(self):
        self.logger.info("{} - Activate Python Virtual Environment".format(self.blueprint_id))

        path = "%s/bin/activate_this.py" % self.venv_home
        try:
            exec (open(path).read(), {'__file__': path})
            return True
        except Exception as err:
            self.logger.info(
                "{} - Failed to activate Python Virtual Environment. Error: {}".format(self.blueprint_id, err))
            return False

    def deactivate_venv(self):
        self.logger.info("{} - Deactivate Python Virtual Environment".format(self.blueprint_id))
        command = ["deactivate"]
        try:
            subprocess.run(command, check=True)
        except Exception as err:
            self.logger.info(
                "{} - Failed to deactivate Python Virtual Environment. Error: {}".format(self.blueprint_id, err))
