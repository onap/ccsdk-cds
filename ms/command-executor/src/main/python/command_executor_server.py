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

import proto.CommandExecutor_pb2_grpc as CommandExecutor_pb2_grpc

from command_executor_handler import CommandExecutorHandler
import utils

_ONE_DAY_IN_SECONDS = 60 * 60 * 24


class CommandExecutorServer(CommandExecutor_pb2_grpc.CommandExecutorServiceServicer):

    def __init__(self):
        self.logger = logging.getLogger(self.__class__.__name__)

    def prepareEnv(self, request, context):
        blueprint_id = utils.get_blueprint_id(request)
        self.logger.info("{} - Received prepareEnv request".format(blueprint_id))
        self.logger.info(request)

        results = []
        handler = CommandExecutorHandler(request)
        if not handler.prepare_env(request, results):
            self.logger.info("{} - Failed to prepare python environment. {}".format(blueprint_id, results))
            return utils.build_response(request, results, False)
        self.logger.info("{} - Package installation logs {}".format(blueprint_id, results))
        return utils.build_response(request, results)

    def executeCommand(self, request, context):
        blueprint_id = utils.get_blueprint_id(request)
        self.logger.info("{} - Received executeCommand request".format(blueprint_id))
        self.logger.info(request)

        results = []
        handler = CommandExecutorHandler(request)
        if not handler.execute_command(request, results):
            self.logger.info("{} - Failed to executeCommand. {}".format(blueprint_id, results))
            return utils.build_response(request, results, False)
        self.logger.info("{} - Execute command logs: {}".format(blueprint_id, results))
        return utils.build_response(request, results)
