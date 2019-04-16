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
import os
from builtins import KeyboardInterrupt, dict
from concurrent import futures
import logging
from logging.config import dictConfig
import time
import sys

import grpc

import proto.CommandExecutor_pb2_grpc as CommandExecutor_pb2_grpc

from request_header_validator_interceptor import RequestHeaderValidatorInterceptor
from command_executor_server import CommandExecutorServer

logger = logging.getLogger("Server")

_ONE_DAY_IN_SECONDS = 60 * 60 * 24


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

    logger.info("Command Executor Server started on %s" % port)

    try:
        while True:
            time.sleep(_ONE_DAY_IN_SECONDS)
    except KeyboardInterrupt:
        server.stop(0)


if __name__ == '__main__':
    logging.basicConfig(filename='/opt/app/onap/logs/application.log',level=logging.DEBUG, format='%(asctime)s | %(message)s')
    serve()
