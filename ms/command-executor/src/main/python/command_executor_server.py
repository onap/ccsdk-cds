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
from builtins import KeyboardInterrupt
from concurrent import futures
import time
import sys

import grpc

import proto.CommandExecutor_pb2_grpc as CommandExecutor_pb2_grpc

from request_header_validator_interceptor import RequestHeaderValidatorInterceptor
from command_executor_handler import CommandExecutorHandler
import utils

_ONE_DAY_IN_SECONDS = 60 * 60 * 24


class CommandExecutorServer(CommandExecutor_pb2_grpc.CommandExecutorServiceServicer):

    def prepareEnv(self, request, context):
        blueprint_id = utils.get_blueprint_id(request)
        print("{} - Received prepareEnv request".format(blueprint_id))
        print (request)

        results = []
        handler = CommandExecutorHandler(request)
        if not handler.prepare_env(request, results):
            print("{} - Failed to prepare python environment. {}".format(blueprint_id, results))
            return utils.build_response(request, False)
        print("{} - Package installation logs {}".format(blueprint_id, results))
        return utils.build_response(request)

    def executeCommand(self, request, context):
        blueprint_id = utils.get_blueprint_id(request)
        print("{} - Received executeCommand request".format(blueprint_id))
        print(request)

        results = []
        handler = CommandExecutorHandler(request)
        if not handler.execute_command(request, results):
            print("{} - Failed to executeCommand. {}".format(blueprint_id, results))
            return utils.build_response(request, False)
        print("{} - Execute command logs: {}".format(blueprint_id, results))
        return utils.build_response(request)


def serve():
    port = sys.argv[1]
    basic_auth = sys.argv[2] + ' ' + sys.argv[3]

    header_validator = RequestHeaderValidatorInterceptor(
        'authorization', basic_auth, grpc.StatusCode.UNAUTHENTICATED,
        'Access denied!')

    server = grpc.server(
        futures.ThreadPoolExecutor(max_workers=10),
        interceptors=(header_validator,))

    CommandExecutor_pb2_grpc.add_CommandExecutorServiceServicer_to_server(
        CommandExecutorServer(), server)

    server.add_insecure_port('[::]:' + port)
    server.start()

    print("Command Executor Server started on %s" % port)

    try:
        while True:
            time.sleep(_ONE_DAY_IN_SECONDS)
    except KeyboardInterrupt:
        server.stop(0)


if __name__ == '__main__':
    serve()
