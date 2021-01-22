#!/usr/bin/python
#
#  Copyright (C) 2019 Bell Canada.
#  Modifications Copyright © 2018-2019 AT&T Intellectual Property.
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

import logging
import os
import time
from builtins import KeyboardInterrupt
from concurrent import futures
from pathlib import Path, PurePath

import grpc
from manager.servicer import ArtifactManagerServicer
from proto.BlueprintManagement_pb2_grpc import add_BlueprintManagementServiceServicer_to_server

from blueprints_grpc import BlueprintProcessing_pb2_grpc, ScriptExecutorConfiguration
from blueprints_grpc.blueprint_processing_server import BlueprintProcessingServer
from blueprints_grpc.request_header_validator_interceptor import RequestHeaderValidatorInterceptor

logger = logging.getLogger("Server")

_ONE_DAY_IN_SECONDS = 60 * 60 * 24


def serve(configuration: ScriptExecutorConfiguration):
    port = configuration.script_executor_property('port')
    authType = configuration.script_executor_property('authType')
    maxWorkers = configuration.script_executor_property('maxWorkers')

    if authType == 'tls-auth':
        cert_chain_file = configuration.script_executor_property('certChain')
        private_key_file = configuration.script_executor_property('privateKey')
        logger.info("Setting GRPC server TLS authentication, cert file(%s) private key file(%s)", cert_chain_file,
                    private_key_file)
        # read in key and certificate
        with open(cert_chain_file, 'rb') as f:
            certificate_chain = f.read()
        with open(private_key_file, 'rb') as f:
            private_key = f.read()

        # create server credentials
        server_credentials = grpc.ssl_server_credentials(((private_key, certificate_chain),))

        # create server
        server = grpc.server(futures.ThreadPoolExecutor(max_workers=int(maxWorkers)))
        BlueprintProcessing_pb2_grpc.add_BlueprintProcessingServiceServicer_to_server(
            BlueprintProcessingServer(configuration), server
        )
        add_BlueprintManagementServiceServicer_to_server(ArtifactManagerServicer(), server)

        # add secure port using credentials
        server.add_secure_port('[::]:' + port, server_credentials)
        server.start()
    else:
        logger.info("Setting GRPC server base authentication")
        basic_auth = configuration.script_executor_property('token')
        header_validator = RequestHeaderValidatorInterceptor(
            'authorization', basic_auth, grpc.StatusCode.UNAUTHENTICATED,
            'Access denied!')
        # create server with token authentication interceptors
        server = grpc.server(futures.ThreadPoolExecutor(max_workers=int(maxWorkers)),
                             interceptors=(header_validator,))
        BlueprintProcessing_pb2_grpc.add_BlueprintProcessingServiceServicer_to_server(
            BlueprintProcessingServer(configuration), server
        )
        add_BlueprintManagementServiceServicer_to_server(ArtifactManagerServicer(), server)

        server.add_insecure_port('[::]:' + port)
        server.start()

    logger.info("Command Executor Server started on %s" % port)

    try:
        while True:
            time.sleep(_ONE_DAY_IN_SECONDS)
    except KeyboardInterrupt:
        server.stop(0)


if __name__ == '__main__':
    default_configuration_file = str(PurePath(Path().absolute(), "../../configuration.ini"))
    supplied_configuration_file = os.environ.get("CONFIGURATION")
    config_file = str(os.path.expanduser(Path(supplied_configuration_file or default_configuration_file)))

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
