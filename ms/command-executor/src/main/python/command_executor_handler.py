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
from subprocess import CalledProcessError, PIPE, TimeoutExpired
from google.protobuf.json_format import MessageToJson
import tempfile
import logging
import os
import re
import subprocess
import virtualenv
import venv
import utils
import proto.CommandExecutor_pb2 as CommandExecutor_pb2

REQUIREMENTS_TXT = "requirements.txt"


class CommandExecutorHandler():

    def __init__(self, request):
        self.request = request
        self.logger = logging.getLogger(self.__class__.__name__)
        self.blueprint_id = utils.get_blueprint_id(request)
        self.execution_timeout = utils.get_blueprint_timeout(request)
        self.venv_home = '/opt/app/onap/blueprints/deploy/' + self.blueprint_id
        self.installed = self.venv_home + '/.installed'

    def is_installed(self):
        return os.path.exists(self.installed)

    def prepare_env(self, request):
        results_log = []
        if not self.is_installed():
            create_venv_status = self.create_venv()
            if not create_venv_status[utils.CDS_IS_SUCCESSFUL_KEY]:
                err_msg = "ERROR: failed to prepare environment for request {} due to error in creating virtual Python env. Original error {}".format(self.blueprint_id, create_venv_status[utils.ERR_MSG_KEY])
                self.logger.error(err_msg)
                return utils.build_ret_data(False, error=err_msg)

            activate_venv_status = self.activate_venv()
            if not activate_venv_status[utils.CDS_IS_SUCCESSFUL_KEY]:
                err_msg = "ERROR: failed to prepare environment for request {} due Python venv_activation. Original error {}".format(self.blueprint_id, activate_venv_status[utils.ERR_MSG_KEY])
                self.logger.error(err_msg)
                return utils.build_ret_data(False, error=err_msg)
            try:
                with open(self.installed, "w+") as f:
                    if not self.install_packages(request, CommandExecutor_pb2.pip, f, results_log):
                        err_msg = "ERROR: failed to prepare environment for request {} during pip package install.".format(self.blueprint_id)
                        return utils.build_ret_data(False, results_log=results_log, error=err_msg)
                    f.write("\r\n") # TODO: is \r needed?
                    results_log.append("\n")
                    if not self.install_packages(request, CommandExecutor_pb2.ansible_galaxy, f, results_log):
                        err_msg = "ERROR: failed to prepare environment for request {} during Ansible install.".format(self.blueprint_id)
                        return utils.build_ret_data(False, results_log=results_log, error=err_msg)
            except Exception as ex:
                err_msg = "ERROR: failed to prepare environment for request {} during installing packages. Exception: {}".format(self.blueprint_id, ex)
                self.logger.error(err_msg)
                return utils.build_ret_data(False, error=err_msg)
        else:
            try:
                with open(self.installed, "r") as f:
                    results_log.append(f.read())
            except Exception as ex:
                err_msg="ERROR: failed to prepare environment during reading 'installed' file {}. Exception: {}".format(self.installed, ex)
                return utils.build_ret_data(False, error=err_msg)

        # deactivate_venv(blueprint_id)
        return utils.build_ret_data(True, results_log=results_log)

    def execute_command(self, request):
        # STDOUT/STDERR output of the process
        results_log = []
        # encoded payload returned by the process
        result = {}
        # workaround for when packages are not specified, we may not want to go through the install step
        # can just call create_venv from here.
        if not self.is_installed():
            self.create_venv()
        try:
            if not self.is_installed():
                create_venv_status = self.create_venv
                if not create_venv_status[utils.CDS_IS_SUCCESSFUL_KEY]:
                    err_msg = "{} - Failed to execute command during venv creation. Original error: {}".format(self.blueprint_id, create_venv_status[utils.ERR_MSG_KEY])
                    return utils.build_ret_data(False, error=err_msg)
            activate_response = self.activate_venv()
            if not activate_response[utils.CDS_IS_SUCCESSFUL_KEY]:
                orig_error = activate_response[utils.ERR_MSG_KEY]
                err_msg = "{} - Failed to execute command during environment activation. Original error: {}".format(self.blueprint_id, orig_error)
                return utils.build_ret_data(False, error=err_msg)

            cmd = "cd " + self.venv_home

            ### TODO: replace with os.environ['VIRTUAL_ENV']?
            if "ansible-playbook" in request.command:
                cmd = cmd + "; " + request.command + " -e 'ansible_python_interpreter=" + self.venv_home + "/bin/python'"
            else:
                cmd = cmd + "; " + request.command + " " + re.escape(MessageToJson(request.properties))

            ### extract the original header request into sys-env variables
            ### RequestID
            request_id = request.requestId
            ### Sub-requestID
            subrequest_id = request.correlationId
            request_id_map = {'CDS_REQUEST_ID':request_id, 'CDS_CORRELATION_ID':subrequest_id}
            updated_env =  { **os.environ, **request_id_map }
            self.logger.info("Running blueprint {} with timeout: {}".format(self.blueprint_id, self.execution_timeout))

            with tempfile.TemporaryFile(mode="w+") as tmp:
                try:
                    completed_subprocess = subprocess.run(cmd, stdout=tmp, stderr=subprocess.STDOUT, shell=True,
                                                env=updated_env, timeout=self.execution_timeout)
                except TimeoutExpired:
                    timeout_err_msg = "Running command {} failed due to timeout of {} seconds.".format(self.blueprint_id, self.execution_timeout)
                    self.logger.error(timeout_err_msg)
                    utils.parse_cmd_exec_output(tmp, self.logger, result, results_log)
                    return utils.build_ret_data(False, results_log=results_log, error=timeout_err_msg)

                utils.parse_cmd_exec_output(tmp, self.logger, result, results_log)
                rc = completed_subprocess.returncode
        except Exception as e:
            err_msg = "{} - Failed to execute command. Error: {}".format(self.blueprint_id, e)
            result.update(utils.build_ret_data(False, results_log=results_log, error=err_msg))
            return result

        # deactivate_venv(blueprint_id)
        #Since return code is only used to check if it's zero (success), we can just return success flag instead.
        self.logger.debug("python return_code : {}".format(rc))
        is_execution_successful = rc == 0
        result.update(utils.build_ret_data(is_execution_successful, results_log=results_log))
        return result

    def install_packages(self, request, type, f, results):
        success = self.install_python_packages('UTILITY', results)

        for package in request.packages:
            if package.type == type:
                f.write("Installed %s packages:\r\n" % CommandExecutor_pb2.PackageType.Name(type))
                for p in package.package:
                    f.write("   %s\r\n" % p)
                    if package.type == CommandExecutor_pb2.pip:
                        success = self.install_python_packages(p, results)
                    else:
                        success = self.install_ansible_packages(p, results)
                    if not success:
                        f.close()
                        os.remove(self.installed)
                        return False
        return True

    def install_python_packages(self, package, results):
        self.logger.info(
            "{} - Install Python package({}) in Python Virtual Environment".format(self.blueprint_id, package))

        if REQUIREMENTS_TXT == package:
            command = ["pip", "install", "-r", self.venv_home + "/Environments/" + REQUIREMENTS_TXT]
        elif package == 'UTILITY':
            # TODO: fix python version that is hardcoded here, may fail if python image is upgraded
            command = ["cp", "-r", "./cds_utils", self.venv_home + "/lib/python3.6/site-packages/"]
        else:
            command = ["pip", "install", package]

        env = dict(os.environ)
        if "https_proxy" in os.environ:
            env['https_proxy'] = os.environ['https_proxy']
            self.logger.info("Using https_proxy: ", env['https_proxy'])

        try:
            results.append(subprocess.run(command, check=True, stdout=PIPE, stderr=PIPE, env=env).stdout.decode())
            results.append("\n")
            self.logger.info("install_python_packages {} succeeded".format(package))
            return True
        except CalledProcessError as e:
            results.append(e.stderr.decode())
            self.logger.error("install_python_packages {} failed".format(package))
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

    # Returns a map with 'status' and 'err_msg'.
    # 'status' True indicates success.
    # 'err_msg' indicates an error occurred. The presence of err_msg may not be fatal,
    # status should be set to False for fatal errors.
    def create_venv(self):
        self.logger.info("{} - Create Python Virtual Environment".format(self.blueprint_id))
        try:
            bin_dir = self.venv_home + "/bin"
            # venv doesn't populate the activate_this.py script, hence we use from virtualenv
            venv.create(self.venv_home, with_pip=True, system_site_packages=True)
            virtualenv.writefile(os.path.join(bin_dir, "activate_this.py"), virtualenv.ACTIVATE_THIS)
            self.logger.info("{} - Creation of Python Virtual Environment finished.".format(self.blueprint_id))
            return utils.build_ret_data(True)
        except Exception as err:
            err_msg = "{} - Failed to provision Python Virtual Environment. Error: {}".format(self.blueprint_id, err)
            self.logger.info(err_msg)
            return utils.build_ret_data(False, error=err_msg)

    # return map cds_is_successful and err_msg. Status is True on success. err_msg may existence doesn't necessarily indicate fatal condition.
    # the 'status' should be set to False to indicate error.
    def activate_venv(self):
        self.logger.info("{} - Activate Python Virtual Environment".format(self.blueprint_id))

        # Fix: The python generated activate_this.py script concatenates the env bin dir to PATH on every call
        #      eventually this process PATH variable was so big (128Kb) that no child process could be spawn
        #      This script will remove all duplicates; while keeping the order of the PATH folders
        fixpathenvvar = "os.environ['PATH']=os.pathsep.join(list(dict.fromkeys(os.environ['PATH'].split(':'))))"

        path = "%s/bin/activate_this.py" % self.venv_home
        try:
            with open(path) as activate_this_script:
                exec (activate_this_script.read(), {'__file__': path})
            exec (fixpathenvvar)
            self.logger.info("Running with PATH : {}".format(os.environ['PATH']))
            return utils.build_ret_data(True)
        except Exception as err:
            err_msg ="{} - Failed to activate Python Virtual Environment. Error: {}".format(self.blueprint_id, err)
            self.logger.info( err_msg)
            return utils.build_ret_data(False, error=err_msg)

    def deactivate_venv(self):
        self.logger.info("{} - Deactivate Python Virtual Environment".format(self.blueprint_id))
        command = ["deactivate"]
        try:
            subprocess.run(command, check=True)
        except Exception as err:
            self.logger.info(
                "{} - Failed to deactivate Python Virtual Environment. Error: {}".format(self.blueprint_id, err))


