#! /usr/bin/python

from concurrent import futures
import time
import logging

import grpc

import CommandExecutor_pb2
import CommandExecutor_pb2_grpc
from request_header_validator_interceptor import RequestHeaderValidatorInterceptor

_ONE_DAY_IN_SECONDS = 60 * 60 * 24


class CommandExecutorServer(CommandExecutor_pb2_grpc.CommandExecutorServiceServicer):

    def prepareEnv(self, request, context):
        return ""

    def executeCommand(self, request, context):
        return ""

    def build_response(self, request):
        CommandExecutor_pb2.ExecutionOutput(requestId=request.requestId, response="SUCCESS")


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
