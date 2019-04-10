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

from concurrent import futures
import time
import logging

import grpc

import proto.CommandExecutor_pb2 as CommandExecutor_pb2
import proto.CommandExecutor_pb2_grpc as CommandExecutor_pb2_grpc

from request_header_validator_interceptor import RequestHeaderValidatorInterceptor
import command_executor_handler

_ONE_DAY_IN_SECONDS = 60 * 60 * 24


class CommandExecutorServer(CommandExecutor_pb2_grpc.CommandExecutorServiceServicer):

    def prepareEnv(self, request, context):
        try:
            command_executor_handler.prepare_env(request)
            return build_response(request)
        except Exception as err:
            logging.error("Python Exception in the script {}", err)
            return build_response(request, False)

    def executeCommand(self, request, context):
        try:
            output = command_executor_handler.execute_command(request)
            print(output)
            return build_response(request)
        except Exception as err:
            logging.error("Python Exception in the script {}", err)
            return build_response(request, False)


def build_response(request, is_success = True):
    if is_success:
        response = "SUCCESS"
    else:
        response = "FAILURE"
    return CommandExecutor_pb2.ExecutionOutput(requestId=request.requestId, response=response)


def serve():
    header_validator = RequestHeaderValidatorInterceptor(
        'Authorization', 'Basic Y2NzZGthcHBzOmNjc2RrYXBwcw==', grpc.StatusCode.UNAUTHENTICATED,
        'Access denied!')

    server = grpc.server(
        futures.ThreadPoolExecutor(max_workers=10),
        interceptors=(header_validator,))
    CommandExecutor_pb2_grpc.add_CommandExecutorServiceServicer_to_server(
        CommandExecutorServer(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    print("Command Executor Server started on port 50051")
    try:
        while True:
            time.sleep(_ONE_DAY_IN_SECONDS)
    except KeyboardInterrupt:
        server.stop(0)


if __name__ == '__main__':
    logging.basicConfig()
    serve()
