"""Copyright 2020 Deutsche Telekom.

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

import json
from dataclasses import dataclass, field
from enum import Enum, unique
from logging import Logger, getLogger
from os import getenv
from types import TracebackType
from typing import Any, Dict, Generator, Optional, Type

from google.protobuf import json_format

from proto.BlueprintProcessing_pb2 import ExecutionServiceInput, ExecutionServiceOutput

from .grpc import Client as GrpcClient
from .http import Client as HttpClient


@unique
class WorkflowMode(Enum):
    """Workflow mode enumerator.

    Workflow can be executed in two modes: synchronously and asynchronously.
    This enumerator stores valid values to set the mode: SYNC for synchronously mode and ASYNC for asynchronously.
    """

    SYNC = "sync"
    ASYNC = "async"


class WorkflowExecution:
    """Wokflow execution class.

    Describes workflow to call. Set blueprint name and version and workflow name to execute.
    Workflow inputs are optional, by default set to empty directory.
    Workflow mode is also optional. It is set by default to call workflow synchronously.
    """

    def __init__(
        self,
        blueprint_name: str,
        blueprint_version: str,
        workflow_name: str,
        workflow_inputs: Dict[str, Any] = None,
        workflow_mode: WorkflowMode = WorkflowMode.SYNC,
    ) -> None:
        """Initialize workflow execution.

        Get all needed information to execute workflow.

        Args:
            blueprint_name (str): Blueprint name to execute workflow from.
            blueprint_version (str): Blueprint version.
            workflow_name (str): Name of the workflow to execute
            workflow_inputs (Dict[str, Any], optional): Key-value workflow inputs. Defaults to None.
            workflow_mode (WorkflowMode, optional): Workflow execution mode. It can be run synchronously or
                asynchronously. Defaults to WorkflowMode.SYNC.
        """
        self.blueprint_name: str = blueprint_name
        self.blueprint_version: str = blueprint_version
        self.workflow_name: str = workflow_name
        if workflow_inputs is None:
            workflow_inputs = {}
        self.workflow_inputs: Dict[str, Any] = workflow_inputs
        self.workflow_mode: WorkflowMode = workflow_mode

    @property
    def message(self) -> ExecutionServiceInput:
        """Workflow execution protobuf message.

        This message is going to be sent to gRPC server to execute workflow.

        Returns:
            ExecutionServiceInput: Properly filled protobuf message.
        """
        execution_msg: ExecutionServiceInput = ExecutionServiceInput()
        execution_msg.actionIdentifiers.mode = self.workflow_mode.value
        execution_msg.actionIdentifiers.blueprintName = self.blueprint_name
        execution_msg.actionIdentifiers.blueprintVersion = self.blueprint_version
        execution_msg.actionIdentifiers.actionName = self.workflow_name
        execution_msg.payload.update({f"{self.workflow_name}-request": self.workflow_inputs})
        return execution_msg


class WorkflowExecutionResult:
    """Result of workflow execution.

    Store both workflow data and the result returns by server.
    """

    def __init__(self, workflow_execution: WorkflowExecution, execution_output: ExecutionServiceOutput) -> None:
        """Initialize workflow execution result object.

        Stores workflow execution data and execution result.

        Args:
            workflow_execution (WorkflowExecution): WorkflowExecution object which was used to call request.
            execution_output (ExecutionServiceOutput): gRPC server response.
        """
        self.workflow_execution: WorkflowExecution = workflow_execution
        self.execution_output: ExecutionServiceOutput = execution_output

    @property
    def blueprint_name(self) -> str:
        """Name of blueprint used to call workflow.

        This value is taken from server response not request (should be the same).

        Returns:
            str: Blueprint name
        """
        return self.execution_output.actionIdentifiers.blueprintName

    @property
    def blueprint_version(self) -> str:
        """Blueprint version.

        This value is taken from server response not request (should be the same).

        Returns:
            str: Blueprint version
        """
        return self.execution_output.actionIdentifiers.blueprintVersion

    @property
    def workflow_name(self) -> str:
        """Workflow name.

        This value is taken from server response not request (should be the same).

        Returns:
            str: Workflow name
        """
        return self.execution_output.actionIdentifiers.actionName

    @property
    def has_error(self) -> bool:
        """Returns bool if request returns error or not.

        Returns:
            bool: True if response has status code different than 200
        """
        return self.execution_output.status.code != 200

    @property
    def error_message(self) -> str:
        """Error message.

        This property is available only if response has error. Otherwise AttributeError will be raised.

        Raises:
            AttributeError: Response has 200 response code and hasn't error message.

        Returns:
            str: Error message returned by server
        """
        if self.has_error:
            return self.execution_output.status.errorMessage
        raise AttributeError("Execution does not finish with error")

    @property
    def payload(self) -> dict:
        """Response payload.

        Payload retured by the server is migrated to Python dict.

        Returns:
            dict: Response's payload.
        """
        return json_format.MessageToDict(self.execution_output.payload)


@dataclass
class Template:
    """Template dataclass.

    Store resolved template data.
    It keeps also ResourceResolution object to call `store_template` method.
    """

    resource_resolution: "ResourceResolution" = field(repr=False)
    blueprint_name: str
    blueprint_version: str
    artifact_name: str = None
    result: str = None
    resolution_key: str = None
    resource_type: str = None
    resource_id: str = None

    def store(self) -> None:
        """Store template using blueprintprocessor HTTP API.

        It uses ResourceResolution `store_template` method.
        """
        self.resource_resolution.store_template(
            blueprint_name=self.blueprint_name,
            blueprint_version=self.blueprint_version,
            artifact_name=self.artifact_name,
            result=self.result,
            resolution_key=self.resolution_key,
            resource_type=self.resource_type,
            resource_id=self.resource_id,
        )


class ResourceResolution:
    """Resource resolution class.

    Helper class to connect to blueprintprocessor's gRPC server, send request to execute workflow and parse responses.
    Blueprint with workflow must be deployed before workflow request.
    It's possible to create both secre or unsecure connection (without SSL/TLS).
    """

    def __init__(
        self,
        *,
        server_address: str = "127.0.0.1",
        # GRPC client configuration
        grpc_server_port: int = 9111,
        use_ssl: bool = False,
        root_certificates: bytes = None,
        private_key: bytes = None,
        certificate_chain: bytes = None,
        # Authentication header configuration for GRPC client
        use_header_auth: bool = False,
        header_auth_token: str = None,
        # HTTP client configuration
        http_server_port: int = 8080,
        http_auth_user: str = None,
        http_auth_pass: str = None,
        http_use_ssl: bool = True,
    ) -> None:
        """Resource resolution object initialization.

        Args:
            server_address (str, optional): gRPC server address. Defaults to "127.0.0.1".
            grpc_server_port (int, optional): gRPC server address port. Defaults to 9111.
            use_ssl (bool, optional): Boolean flag to determine if secure channel should be created or not.
                Defaults to False.
            root_certificates (bytes, optional): The PEM-encoded root certificates. None if it shouldn't be used.
                Defaults to None.
            private_key (bytes, optional): The PEM-encoded private key as a byte string, or None if no private key
                should be used. Defaults to None.
            certificate_chain (bytes, optional): The PEM-encoded certificate chain as a byte string to use or or None if
                no certificate chain should be used. Defaults to None.
            use_header_auth (bool, optional): Boolean flag to determine if authorization headed shoud be added for
                every call or not. Defaults to False.
            header_auth_token (str, optional): Authorization token value. Defaults to None.
                If no value is provided "AUTH_TOKEN" environment variable will be used.
            http_server_port (int, optional): HTTP server address port. Defaults to 8080.
            http_auth_user (str, optional): Username used for HTTP requests authorization. Defaults to None.
                If no value is provided "API_USERNAME" environment variable will be used.
            http_auth_pass (str, optional): Password used for HTTP requests authorization. Defaults to None.
                If no value is provided "API_PASSWORD" environment variable will be used.
            http_use_ssl (bool, optional): Determines if secure connection should be used for HTTP requests.
                Defaults to False.
        """
        # Logger
        self.logger: Logger = getLogger(__name__)
        # GrpcClient settings
        self.grpc_client_server_address: str = server_address
        self.grpc_client_server_port: str = grpc_server_port
        self.grpc_client_use_ssl: bool = use_ssl
        self.grpc_client_root_certificates: bytes = root_certificates
        self.grpc_client_private_key: bytes = private_key
        self.grpc_client_certificate_chain: bytes = certificate_chain
        self.grpc_client_use_header_auth: bool = use_header_auth
        self.grpc_client_header_auth_token: str = header_auth_token or getenv("AUTH_TOKEN")
        self.grpc_client: GrpcClient = None
        # HttpClient settings
        self.http_client: HttpClient = HttpClient(
            server_address,
            server_port=http_server_port,
            auth_user=http_auth_user or getenv("API_USERNAME"),
            auth_pass=http_auth_pass or getenv("API_PASSWORD"),
            use_ssl=http_use_ssl,
        )

    def __enter__(self) -> "ResourceResolution":
        """Enter ResourceResolution instance context.

        GrpcClient connection is created.
        """
        self.grpc_client = GrpcClient(
            server_address=f"{self.grpc_client_server_address}:{self.grpc_client_server_port}",
            use_ssl=self.grpc_client_use_ssl,
            root_certificates=self.grpc_client_root_certificates,
            private_key=self.grpc_client_private_key,
            certificate_chain=self.grpc_client_certificate_chain,
            use_header_auth=self.grpc_client_use_header_auth,
            header_auth_token=self.grpc_client_header_auth_token,
        )
        return self

    def __exit__(
        self,
        unused_exc_type: Optional[Type[BaseException]],
        unused_exc_value: Optional[BaseException],
        unused_traceback: Optional[TracebackType],
    ) -> None:
        """Exit ResourceResolution instance context.

        GrpcClient connection is closed.
        """
        self.grpc_client.close()

    def execute_workflows(self, *workflows: WorkflowExecution) -> Generator[WorkflowExecutionResult, None, None]:
        """Execute provided workflows.

        Workflows are going to be execured using one gRPC API call. Depends of implementation that may has
        some consequences. In some cases if any request fails all requests after that won't be called.

        Responses and zipped with workflows and WorkflowExecutionResult object is initialized and yielded.

        Raises:
            AttributeError: Raises if client object is not created. It occurs only if you not uses context manager.
                Then user have to create client instance for ResourceResolution object by himself calling:
                ```
                resource_resoulution.client = GrpcClient(
                    server_address=f"{resource_resoulution.client_server_address}:{resource_resoulution.client_server_port}",
                    use_ssl=resource_resoulution.client_use_ssl,
                    root_certificates=resource_resoulution.client_root_certificates,
                    private_key=resource_resoulution.client_private_key,
                    certificate_chain=resource_resoulution.client_certificate_chain,
                    use_header_auth=resource_resoulution.client_use_header_auth,
                    header_auth_token=resource_resoulution.client_header_auth_token,
                )
                ```
                Remeber also to close client connection.

        Returns:
            Generator[WorkflowExecutionResult, None, None]: WorkflowExecutionResult object
                with both WorkflowExection object and server response for it's request.
        """
        self.logger.debug("Execute workflows")
        if not self.grpc_client:
            raise AttributeError("gRPC client not connected")

        for response, workflow in zip(
            self.grpc_client.process((workflow.message for workflow in workflows)), workflows
        ):
            yield WorkflowExecutionResult(workflow, response)

    def _check_template_resolve_params(
        self, resolution_key: str = None, resource_type: str = None, resource_id: str = None
    ):
        """Check template API request parameters.

        It's possible to store/retrieve templates using pair of artifact name and resolution key OR
        resource type and resource id. This method checks if valid combination of parameters were used.

        Args:
            resolution_key (str, optional): resolutionKey HTTP request parameter value. Defaults to None.
            resource_type (str, optional): resourceType HTTP request parameter value. Defaults to None.
            resource_id (str, optional): resourceId HTTP request parameter value. Defaults to None.

        Raises:
            AttributeError: Invalid combination of parametes used
        """
        if not any([resolution_key, all([resource_type, resource_id])]):
            raise AttributeError(
                "To store/retrieve template resolution_key and artifact_name or both resource_type and resource_id have to be provided"
            )

    def store_template(
        self,
        blueprint_name: str,
        blueprint_version: str,
        result: str,
        artifact_name: str,
        resolution_key: str = None,
        resource_type: str = None,
        resource_id: str = None,
    ) -> None:
        """Store template using blueprintprocessor HTTP API.

        Prepare and send a request to store resolved template using blueprint name, blueprint version
        and pair of artifact name and resolution key OR resource type and resource id.

        Method returns Template dataclass, which stores all template data and can be used to update
        it's result.

        Args:
            blueprint_name (str): Blueprint name
            blueprint_version (str): Blueprint version
            result (str): Template result
            artifact_name (str): Artifact name
            resolution_key (str, optional): Resolution key. Defaults to None.
            resource_type (str, optional): Resource type. Defaults to None.
            resource_id (str, optional): Resource ID. Defaults to None.
        """
        self.logger.debug("Store template")
        self._check_template_resolve_params(resolution_key, resource_type, resource_id)
        base_endpoint: str = f"template/{blueprint_name}/{blueprint_version}"
        if resolution_key and artifact_name:
            endpoint: str = f"{base_endpoint}/{artifact_name}/{resolution_key}"
        else:
            endpoint: str = f"{base_endpoint}/{resource_type}/{resource_id}"
        response = self.http_client.send_request(
            "POST", endpoint, headers={"Content-Type": "application/json"}, data=json.dumps({"result": result})
        )

    def retrieve_template(
        self,
        blueprint_name: str,
        blueprint_version: str,
        artifact_name: str,
        resolution_key: str = None,
        resource_type: str = None,
        resource_id: str = None,
    ) -> Template:
        """Get stored template using blueprintprocessor's HTTP API.

        Prepare and send a request to retrieve resolved template using blueprint name, blueprint version
        and pair of artifact name and resolution key OR resource type and resource id.

        Args:
            blueprint_name (str): Blueprint name
            blueprint_version (str): Blueprint version
            artifact_name (str): Artifact name
            resolution_key (str, optional): Resolution key. Defaults to None.
            resource_type (str, optional): Resource type. Defaults to None.
            resource_id (str, optional): Resource ID. Defaults to None.
        """
        self.logger.debug("Retrieve template")
        self._check_template_resolve_params(resolution_key, resource_type, resource_id)
        params: dict = {"bpName": blueprint_name, "bpVersion": blueprint_version, "artifactName": artifact_name}
        if resolution_key:
            params.update({"resolutionKey": resolution_key})
        else:
            params.update({"resourceType": resource_type, "resourceId": resource_id})
        response = self.http_client.send_request(
            "GET", "template", headers={"Accept": "application/json"}, params=params
        )
        return Template(
            resource_resolution=self,
            blueprint_name=blueprint_name,
            blueprint_version=blueprint_version,
            artifact_name=artifact_name,
            resolution_key=resolution_key,
            resource_type=resource_type,
            resource_id=resource_id,
            result=response.json()["result"],
        )
