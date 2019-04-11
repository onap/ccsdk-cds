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
import logging
import sys

import grpc

import proto.CommandExecutor_pb2 as CommandExecutor_pb2
import proto.CommandExecutor_pb2_grpc as CommandExecutor_pb2_grpc

from request_header_validator_interceptor import RequestHeaderValidatorInterceptor
import command_executor_handler

_ONE_DAY_IN_SECONDS = 60 * 60 * 24


class CommandExecutorServer(CommandExecutor_pb2_grpc.CommandExecutorServiceServicer):

    def prepareEnv(self, request, context):
        blueprint_id = request.identifiers.blueprintName + ':' + request.identifiers.blueprintVersion
        results = []
        if not command_executor_handler.prepare_env(request, results):
            logging.error("{} - Failed to prepare python environment", blueprint_id)
            return build_response(request, False)
        logging.info("{} - Package installation logs {}", blueprint_id, results)
        return build_response(request)

    def executeCommand(self, request, context):
        output = command_executor_handler.execute_command(request)
        print(output)
        return build_response(request)


def build_response(request, is_success=True):
    if is_success:
        response = "SUCCESS"
    else:
        response = "FAILURE"
    return CommandExecutor_pb2.ExecutionOutput(requestId=request.requestId, response=response)


def serve():
    port = sys.argv[1]
    basic_auth = sys.argv[2]

    header_validator = RequestHeaderValidatorInterceptor(
        'Authorization', basic_auth, grpc.StatusCode.UNAUTHENTICATED,
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
    logging.basicConfig()
    serve()
