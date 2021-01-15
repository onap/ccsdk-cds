#
# Copyright (C) 2019 - 2020 Bell Canada.
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
import sys
import re
import subprocess
import virtualenv
import venv
import utils
import proto.CommandExecutor_pb2 as CommandExecutor_pb2
from zipfile import ZipFile
import io

REQUIREMENTS_TXT = "requirements.txt"


class CommandExecutorHandler():
    BLUEPRINTS_DEPLOY_DIR = '/opt/app/onap/blueprints/deploy/'
    TOSCA_META_FILE = 'TOSCA-Metadata/TOSCA.meta'

    def __init__(self, request):
        self.request = request
        self.logger = logging.getLogger(self.__class__.__name__)
        self.blueprint_name = utils.get_blueprint_name(request)
        self.blueprint_version = utils.get_blueprint_version(request)
        self.uuid = utils.get_blueprint_uuid(request)
        self.request_id = utils.get_blueprint_requestid(request)
        self.sub_request_id = utils.get_blueprint_subRequestId(request)
        self.blueprint_name_version_uuid = utils.blueprint_name_version_uuid(request)
        self.execution_timeout = utils.get_blueprint_timeout(request)
        # onap/blueprints/deploy will be ephemeral now
        self.blueprint_dir = self.BLUEPRINTS_DEPLOY_DIR + self.blueprint_name_version_uuid
        self.blueprint_tosca_meta_file = self.blueprint_dir + '/' + self.TOSCA_META_FILE
        self.extra = utils.getExtraLogData(request)
        self.installed = self.blueprint_dir + '/.installed'

    def is_installed(self):
        return os.path.exists(self.installed)

    def blueprint_dir_exists(self):
        return os.path.exists(self.blueprint_dir)

    # used to validate if the blueprint actually had a chace of getting uploaded
    def blueprint_tosca_meta_file_exists(self):
        return os.path.exists(self.blueprint_tosca_meta_file)

    def err_exit(self, msg):
        self.logger.error(msg, extra=self.extra)
        return utils.build_ret_data(False, error=msg)
    
    def is_valid_archive_type(self, archiveType):
        return archiveType=="CBA_ZIP" or archiveType=="CBA_GZIP"

    # Handle uploading blueprint request
    # accept UploadBlueprintInput (CommandExecutor.proto) struct
    # create dir blueprintName/BlueprintVersion/BlueprintUUID, and extract binData as either ZIP file or GZIP
    # based on archiveType field...
    def uploadBlueprint(self, request):
        archive_type = request.archiveType
        compressed_cba_stream = io.BytesIO(request.binData)
        
        if not self.is_valid_archive_type(archive_type):
            return utils.build_grpc_blueprint_upload_response(self.request_id, self.sub_request_id, False, ["Archive type {} is not valid.".format(archive_type)])
        
        # create the BP dir self.blueprint_dir
        try:
            os.makedirs(name=self.blueprint_dir, mode=0o755, exist_ok=True)
        except OSError as ex:
            err_msg = "Failed to create blueprint dir: {} exception message: {}".format(self.blueprint_dir, ex.strerror)
            self.logger.error(err_msg, extra=self.extra)
            return utils.build_grpc_blueprint_upload_response(self.request_id, self.sub_request_id, False, [err_msg])
        if archive_type=="CBA_ZIP":
            self.logger.info("Extracting ZIP data to dir {}".format(self.blueprint_dir), extra=self.extra)
            try:
                with ZipFile(compressed_cba_stream,'r') as zipfile:
                    zipfile.extractall(self.blueprint_dir)                    
                self.logger.info("Done extracting ZIP data to dir {}".format(self.blueprint_dir), extra=self.extra)
            except (IOError, zipfile.error) as e:
                err_msg = "Error extracting ZIP data to dir {} exception: {}".format(self.blueprint_dir, e)
                self.logger.error(err_msg, extra=self.extra)
                return utils.build_grpc_blueprint_upload_response(self.request_id, self.sub_request_id, False, [err_msg])
        # TODO with an actual test gzip cba...
        elif archive_type=="CBA_GZIP":
            self.logger.error("CBA_GZIP TODO", extra=self.extra)
            return utils.build_grpc_blueprint_upload_response(self.request_id, self.sub_request_id, False, ["Error extracting GZIP data to {} GZIP todo!".format(self.blueprint_dir)])
        # Finally, everything is ok! 
        return utils.build_grpc_blueprint_upload_response(self.request_id, self.sub_request_id, True, [])

    def prepare_env(self, request):
        results_log = []

        # validate that the blueprint name in the request exists, if not, notify the caller
        if not self.blueprint_dir_exists():
            err_msg = "CBA directory {} not found on cmd-exec. CBA will be uploaded by BP proc.".format(self.blueprint_name_version_uuid)
            self.logger.info(err_msg, extra=self.extra)
            return utils.build_ret_data(False, results_log=results_log, error=err_msg, reupload_cba=True)
        if not self.blueprint_tosca_meta_file_exists():
            err_msg = "CBA directory {} exists on cmd-exec, but TOSCA meta file is not found!!! Returning (null) as UUID. CBA will be uploaded by BP proc.".format(self.blueprint_name_version_uuid)
            self.logger.info(err_msg, extra=self.extra)
            return utils.build_ret_data(False, results_log=results_log, error=err_msg, reupload_cba=True)
        self.logger.info("CBA directory {} exists on cmd-exec.".format(self.blueprint_name_version_uuid), extra=self.extra)

        if not self.is_installed():
            create_venv_status = self.create_venv()
            if not create_venv_status[utils.CDS_IS_SUCCESSFUL_KEY]:
                return self.err_exit("ERROR: failed to prepare environment for request {} due to error in creating virtual Python env. Original error {}".format(self.blueprint_name_version_uuid, create_venv_status[utils.ERR_MSG_KEY]))

            activate_venv_status = self.activate_venv()
            if not activate_venv_status[utils.CDS_IS_SUCCESSFUL_KEY]:
                return self.err_exit("ERROR: failed to prepare environment for request {} due Python venv_activation. Original error {}".format(self.blueprint_name_version_uuid, activate_venv_status[utils.ERR_MSG_KEY]))
            try:
                with open(self.installed, "w+") as f:
                    if not self.install_packages(request, CommandExecutor_pb2.pip, f, results_log):
                        err_msg = "ERROR: failed to prepare environment for request {} during pip package install.".format(self.blueprint_name_version_uuid)
                        return utils.build_ret_data(False, results_log=results_log, error=err_msg)
                    f.write("\r\n") # TODO: is \r needed?
                    results_log.append("\n")
                    if not self.install_packages(request, CommandExecutor_pb2.ansible_galaxy, f, results_log):
                        err_msg = "ERROR: failed to prepare environment for request {} during Ansible install.".format(self.blueprint_name_version_uuid)
                        return utils.build_ret_data(False, results_log=results_log, error=err_msg)
            except Exception as ex:
                err_msg = "ERROR: failed to prepare environment for request {} during installing packages. Exception: {}".format(self.blueprint_name_version_uuid, ex)
                self.logger.error(err_msg, extra=self.extra)
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
                    err_msg = "{} - Failed to execute command during venv creation. Original error: {}".format(self.blueprint_name_version_uuid, create_venv_status[utils.ERR_MSG_KEY])
                    return utils.build_ret_data(False, error=err_msg)
            activate_response = self.activate_venv()
            if not activate_response[utils.CDS_IS_SUCCESSFUL_KEY]:
                orig_error = activate_response[utils.ERR_MSG_KEY]
                err_msg = "{} - Failed to execute command during environment activation. Original error: {}".format(self.blueprint_name_version_uuid, orig_error)
                return utils.build_ret_data(False, error=err_msg)
            # touch blueprint dir to indicate this CBA was used recently
            os.utime(self.blueprint_dir)

            cmd = "cd " + self.blueprint_dir

            ### if properties are defined we add them to the command
            properties = ""
            if request.properties is not None and len(request.properties) > 0:
                properties = " " + re.escape(MessageToJson(request.properties))

            ### TODO: replace with os.environ['VIRTUAL_ENV']?
            if "ansible-playbook" in request.command:
                cmd = cmd + "; " + request.command + " -e 'ansible_python_interpreter=" + self.blueprint_dir + "/bin/python'"
            else:
                cmd = cmd + "; " + request.command + properties

            ### extract the original header request into sys-env variables
            ### OriginatorID
            originator_id = request.originatorId
            ### CorrelationID
            correlation_id = request.correlationId
            request_id_map = {'CDS_REQUEST_ID':self.request_id, 'CDS_SUBREQUEST_ID':self.sub_request_id, 'CDS_ORIGINATOR_ID': originator_id, 'CDS_CORRELATION_ID': correlation_id}
            updated_env =  { **os.environ, **request_id_map }
            self.logger.info("Running blueprint {} with timeout: {}".format(self.blueprint_name_version_uuid, self.execution_timeout), extra=self.extra)

            with tempfile.TemporaryFile(mode="w+") as tmp:
                try:
                    completed_subprocess = subprocess.run(cmd, stdout=tmp, stderr=subprocess.STDOUT, shell=True,
                                                env=updated_env, timeout=self.execution_timeout)
                except TimeoutExpired:
                    timeout_err_msg = "Running command {} failed due to timeout of {} seconds.".format(self.blueprint_name_version_uuid, self.execution_timeout)
                    self.logger.error(timeout_err_msg, extra=self.extra)
                    utils.parse_cmd_exec_output(tmp, self.logger, result, results_log, self.extra)
                    return utils.build_ret_data(False, results_log=results_log, error=timeout_err_msg)

                utils.parse_cmd_exec_output(tmp, self.logger, result, results_log, self.extra)
                rc = completed_subprocess.returncode
        except Exception as e:
            err_msg = "{} - Failed to execute command. Error: {}".format(self.blueprint_name_version_uuid, e)
            result.update(utils.build_ret_data(False, results_log=results_log, error=err_msg))
            return result

        # deactivate_venv(blueprint_id)
        #Since return code is only used to check if it's zero (success), we can just return success flag instead.
        self.logger.debug("python return_code : {}".format(rc), extra=self.extra)
        is_execution_successful = rc == 0
        result.update(utils.build_ret_data(is_execution_successful, results_log=results_log))
        return result

    def install_packages(self, request, type, f, results):
        success = self.install_python_packages('UTILITY', results)
        if not success:
            self.logger.error("Error installing 'UTILITY (cds_utils) package to CBA python environment!!!", extra=self.extra)
            return False

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
            "{} - Install Python package({}) in Python Virtual Environment".format(self.blueprint_name_version_uuid, package), extra=self.extra)

        if REQUIREMENTS_TXT == package:
            command = ["pip", "install", "--user", "-r", self.blueprint_dir + "/Environments/" + REQUIREMENTS_TXT]
        elif package == 'UTILITY':
            py_ver_maj = sys.version_info.major
            py_ver_min = sys.version_info.minor
            command = ["cp", "-r", "./cds_utils", "{}/lib/python{}.{}/site-packages/".format(self.blueprint_dir, py_ver_maj,py_ver_min)]
        else:
            command = ["pip", "install", "--user", package]

        env = dict(os.environ)
        if "https_proxy" in os.environ:
            env['https_proxy'] = os.environ['https_proxy']
            self.logger.info("Using https_proxy: {}".format(env['https_proxy']), extra=self.extra)
        try:
            results.append(subprocess.run(command, check=True, stdout=PIPE, stderr=PIPE, env=env).stdout.decode())
            results.append("\n")
            self.logger.info("install_python_packages {} succeeded".format(package), extra=self.extra)
            return True
        except CalledProcessError as e:
            results.append(e.stderr.decode())
            self.logger.error("install_python_packages {} failed".format(package), extra=self.extra)
            return False

    def install_ansible_packages(self, package, results):
        self.logger.info(
            "{} - Install Ansible Role package({}) in Python Virtual Environment".format(self.blueprint_name_version_uuid, package), extra=self.extra)
        command = ["ansible-galaxy", "install", package, "-p", self.blueprint_dir + "/Scripts/ansible/roles"]

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
        self.logger.info("{} - Create Python Virtual Environment".format(self.blueprint_name_version_uuid), extra=self.extra)
        try:
            bin_dir = self.blueprint_dir + "/bin"
            # venv doesn't populate the activate_this.py script, hence we use from virtualenv
            venv.create(self.blueprint_dir, with_pip=True, system_site_packages=True)
            virtualenv.writefile(os.path.join(bin_dir, "activate_this.py"), virtualenv.ACTIVATE_THIS)
            self.logger.info("{} - Creation of Python Virtual Environment finished.".format(self.blueprint_name_version_uuid), extra=self.extra)
            return utils.build_ret_data(True)
        except Exception as err:
            err_msg = "{} - Failed to provision Python Virtual Environment. Error: {}".format(self.blueprint_name_version_uuid, err)
            self.logger.info(err_msg, extra=self.extra)
            return utils.build_ret_data(False, error=err_msg)

    # return map cds_is_successful and err_msg. Status is True on success. err_msg may existence doesn't necessarily indicate fatal condition.
    # the 'status' should be set to False to indicate error.
    def activate_venv(self):
        self.logger.info("{} - Activate Python Virtual Environment".format(self.blueprint_name_version_uuid), extra=self.extra)

        # Fix: The python generated activate_this.py script concatenates the env bin dir to PATH on every call
        #      eventually this process PATH variable was so big (128Kb) that no child process could be spawn
        #      This script will remove all duplicates; while keeping the order of the PATH folders
        fixpathenvvar = "os.environ['PATH']=os.pathsep.join(list(dict.fromkeys(os.environ['PATH'].split(':'))))"

        path = "%s/bin/activate_this.py" % self.blueprint_dir
        try:
            with open(path) as activate_this_script:
                exec (activate_this_script.read(), {'__file__': path})
            exec (fixpathenvvar)
            self.logger.info("Running with PATH : {}".format(os.environ['PATH']), extra=self.extra)
            return utils.build_ret_data(True)
        except Exception as err:
            err_msg ="{} - Failed to activate Python Virtual Environment. Error: {}".format(self.blueprint_name_version_uuid, err)
            self.logger.info( err_msg, extra=self.extra)
            return utils.build_ret_data(False, error=err_msg)

    def deactivate_venv(self):
        self.logger.info("{} - Deactivate Python Virtual Environment".format(self.blueprint_name_version_uuid), extra=self.extra)
        command = ["deactivate"]
        try:
            subprocess.run(command, check=True)
        except Exception as err:
            self.logger.info(
                "{} - Failed to deactivate Python Virtual Environment. Error: {}".format(self.blueprint_name_version_uuid, err), extra=self.extra)


