#!/usr/bin/python

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
import logging
import os, sys
import proto.CommandExecutor_pb2_grpc as CommandExecutor_pb2_grpc

from command_executor_handler import CommandExecutorHandler
import utils

class CommandExecutorServer(CommandExecutor_pb2_grpc.CommandExecutorServiceServicer):

    def __init__(self):
        self.logger = logging.getLogger(self.__class__.__name__)

    def uploadBlueprint(self, request, context):
        # handler for 'uploadBluleprint' call - extracts compressed cbaData to a  bpname/bpver/bpuuid dir.
        blueprint_name_version_uuid = utils.blueprint_name_version_uuid(request)
        extra = utils.getExtraLogData(request)
        self.logger.info("{} - Received uploadBlueprint request".format(blueprint_name_version_uuid), extra=extra)
        handler = CommandExecutorHandler(request)
        return handler.uploadBlueprint(request)
        
    def prepareEnv(self, request, context):
        blueprint_id = utils.blueprint_name_version_uuid(request)
        extra = utils.getExtraLogData(request)
        self.logger.info("{} - Received prepareEnv request".format(blueprint_id), extra=extra)
        self.logger.info(request, extra=extra)

        handler = CommandExecutorHandler(request)
        prepare_env_response = handler.prepare_env(request)
        if prepare_env_response[utils.CDS_IS_SUCCESSFUL_KEY]:
            self.logger.info("{} - Package installation logs {}".format(blueprint_id, prepare_env_response[utils.RESULTS_LOG_KEY]), extra=extra)
        else:
            self.logger.info("{} - Failed to prepare python environment. {}".format(blueprint_id, prepare_env_response[utils.ERR_MSG_KEY]), extra=extra)
        self.logger.info("Prepare Env Response returned : %s" % prepare_env_response, extra=extra)
        return utils.build_grpc_response(request.requestId, prepare_env_response)

    def executeCommand(self, request, context):
        blueprint_id = utils.blueprint_name_version_uuid(request)
        extra = utils.getExtraLogData(request)
        self.logger.info("{} - Received executeCommand request".format(blueprint_id), extra=extra)
        if os.environ.get('CE_DEBUG','false') == "true":
            self.logger.info(request, extra=extra)

        handler = CommandExecutorHandler(request)
        exec_cmd_response = handler.execute_command(request)
        if exec_cmd_response[utils.CDS_IS_SUCCESSFUL_KEY]:
            self.logger.info("{} - Execution finished successfully.".format(blueprint_id), extra=extra)
        else:
            self.logger.info("{} - Failed to executeCommand. {}".format(blueprint_id, exec_cmd_response[utils.RESULTS_LOG_KEY]), extra=extra)

        ret = utils.build_grpc_response(request.requestId, exec_cmd_response)
        self.logger.info("Payload returned : {}".format(exec_cmd_response), extra=extra)

        return ret
