#  Copyright © 2018-2019 AT&T Intellectual Property.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

from builtins import KeyboardInterrupt
from concurrent import futures
import logging
import time
import grpc
from pathlib import Path, PurePath
from blueprints_grpc import BluePrintProcessing_pb2_grpc
from blueprints_grpc.request_header_validator_interceptor import RequestHeaderValidatorInterceptor
from blueprints_grpc.blueprint_processing_server import BluePrintProcessingServer
from blueprints_grpc import ScriptExecutorConfiguration

logger = logging.getLogger("Server")

_ONE_DAY_IN_SECONDS = 60 * 60 * 24


def serve(configuration: ScriptExecutorConfiguration):
    port = configuration.script_executor_property('port')
    basic_auth = configuration.script_executor_property('auth')
    maxWorkers = configuration.script_executor_property('maxWorkers')

    header_validator = RequestHeaderValidatorInterceptor(
        'authorization', basic_auth, grpc.StatusCode.UNAUTHENTICATED,
        'Access denied!')

    server = grpc.server(futures.ThreadPoolExecutor(max_workers=int(maxWorkers)),
                         interceptors=(header_validator,))

    BluePrintProcessing_pb2_grpc.add_BluePrintProcessingServiceServicer_to_server(
        BluePrintProcessingServer(configuration), server)

    server.add_insecure_port('[::]:' + port)
    server.start()

    logger.info("Command Executor Server started on %s" % port)

    try:
        while True:
            time.sleep(_ONE_DAY_IN_SECONDS)
    except KeyboardInterrupt:
        server.stop(0)


if __name__ == '__main__':
    config_file = str(PurePath(Path().absolute())) + '/configuration.ini'
    configuration = ScriptExecutorConfiguration(config_file)
    logging_formater = '%(asctime)s - %(name)s - %(threadName)s - %(levelname)s - %(message)s'
    logging.basicConfig(filename=configuration.script_executor_property('logFile'),
                        level=logging.DEBUG,
                        format=logging_formater)
    console = logging.StreamHandler()
    console.setLevel(logging.INFO)
    formatter = logging.Formatter(logging_formater)
    console.setFormatter(formatter)
    logging.getLogger('').addHandler(console)
    serve(configuration)
