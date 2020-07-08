#!/usr/bin/python

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
import logging
import os, sys
import proto.CommandExecutor_pb2_grpc as CommandExecutor_pb2_grpc

from command_executor_handler import CommandExecutorHandler
import utils

class CommandExecutorServer(CommandExecutor_pb2_grpc.CommandExecutorServiceServicer):

    def __init__(self):
        self.logger = logging.getLogger(self.__class__.__name__)

    def prepareEnv(self, request, context):
        blueprint_id = utils.get_blueprint_id(request)
        self.logger.info("{} - Received prepareEnv request".format(blueprint_id))
        self.logger.info(request)

        handler = CommandExecutorHandler(request)
        prepare_env_response = handler.prepare_env(request)
        if prepare_env_response[utils.CDS_IS_SUCCESSFUL_KEY]:
            self.logger.info("{} - Package installation logs {}".format(blueprint_id, prepare_env_response[utils.RESULTS_LOG_KEY]))
        else:
            self.logger.info("{} - Failed to prepare python environment. {}".format(blueprint_id, prepare_env_response[utils.ERR_MSG_KEY]))
        self.logger.info("Prepare Env Response returned : %s" % prepare_env_response)
        return utils.build_grpc_response(request.requestId, prepare_env_response)

    def executeCommand(self, request, context):
        blueprint_id = utils.get_blueprint_id(request)
        self.logger.info("{} - Received executeCommand request".format(blueprint_id))
        if os.environ.get('CE_DEBUG','false') == "true":
            self.logger.info(request)

        handler = CommandExecutorHandler(request)
        exec_cmd_response = handler.execute_command(request)
        if exec_cmd_response[utils.CDS_IS_SUCCESSFUL_KEY]:
            self.logger.info("{} - Execution finished successfully.".format(blueprint_id))
        else:
            self.logger.info("{} - Failed to executeCommand. {}".format(blueprint_id, exec_cmd_response[utils.RESULTS_LOG_KEY]))

        ret = utils.build_grpc_response(request.requestId, exec_cmd_response)
        self.logger.info("Payload returned : {}".format(exec_cmd_response))

        return ret