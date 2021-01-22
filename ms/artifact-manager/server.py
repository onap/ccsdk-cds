"""Copyright 2019 Deutsche Telekom.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

from concurrent.futures import ThreadPoolExecutor

import click
from grpc import server as grpc_server
from manager.configuration import config
from manager.servicer import ArtifactManagerServicer
from proto.BlueprintManagement_pb2_grpc import add_BlueprintManagementServiceServicer_to_server


@click.command()
def run_server():
    """Run Artifact Manager gRPC server.

    Values like 'maxWorkers' and 'port' must be specified in a config file in .ini format.

    Config file path is specified by 'CONFIGURATION' environment variable.

    """
    max_workers: int = int(config["artifactManagerServer"]["maxWorkers"])
    server: grpc_server = grpc_server(ThreadPoolExecutor(max_workers=max_workers))

    add_BlueprintManagementServiceServicer_to_server(ArtifactManagerServicer(), server)
    port_number: int = int(config["artifactManagerServer"]["port"])
    server.add_insecure_port(f"[::]:{port_number}")

    click.echo(f"Server starts on port {port_number} using {max_workers} workers.")
    server.start()
    server.wait_for_termination()


if __name__ == "__main__":
    run_server()
