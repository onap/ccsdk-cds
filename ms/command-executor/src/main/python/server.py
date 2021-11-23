
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
from builtins import KeyboardInterrupt
from concurrent import futures
import logging
import time
import sys
import utils

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
        futures.ThreadPoolExecutor(max_workers=15),
        interceptors=(header_validator,))

    CommandExecutor_pb2_grpc.add_CommandExecutorServiceServicer_to_server(
        CommandExecutorServer(), server)

    server.add_insecure_port('[::]:' + port)
    server.start()

    logger.info("Command Executor Server started on %s" % port, extra=utils.getExtraLogData())

    try:
        while True:
            time.sleep(_ONE_DAY_IN_SECONDS)
    except KeyboardInterrupt:
        server.stop(0)


if __name__ == '__main__':
    logging_formater = '%(asctime)s|%(request_id)s|%(subrequest_id)s|%(originator_id)s|%(threadName)s|%(name)s|%(levelname)s|%(message)s'
    logging.basicConfig(level=logging.INFO, format=logging_formater)
    serve()
