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

from enum import Enum, unique
from logging import Logger, getLogger
from types import TracebackType
from typing import Any, Dict, Generator, Optional, Type

from google.protobuf import json_format

from proto.BluePrintProcessing_pb2 import ExecutionServiceInput, ExecutionServiceOutput

from .client import Client


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
        server_port: int = "9111",
        use_ssl: bool = False,
        root_certificates: bytes = None,
        private_key: bytes = None,
        certificate_chain: bytes = None,
        # Authentication header configuration
        use_header_auth: bool = False,
        header_auth_token: str = None,
    ) -> None:
        """Resource resolution object initialization.

        Args:
            server_address (str, optional): gRPC server address. Defaults to "127.0.0.1".
            server_port (int, optional): gRPC server address port. Defaults to "9111".
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
        """
        # Logger
        self.logger: Logger = getLogger(__name__)
        # Client settings
        self.client_server_address: str = server_address
        self.client_server_port: str = server_port
        self.client_use_ssl: bool = use_ssl
        self.client_root_certificates: bytes = root_certificates
        self.client_private_key: bytes = private_key
        self.client_certificate_chain: bytes = certificate_chain
        self.client_use_header_auth: bool = use_header_auth
        self.client_header_auth_token: str = header_auth_token
        self.client: Client = None

    def __enter__(self) -> "ResourceResolution":
        """Enter ResourceResolution instance context.

        Client connection is created.
        """
        self.client = Client(
            server_address=f"{self.client_server_address}:{self.client_server_port}",
            use_ssl=self.client_use_ssl,
            root_certificates=self.client_root_certificates,
            private_key=self.client_private_key,
            certificate_chain=self.client_certificate_chain,
            use_header_auth=self.client_use_header_auth,
            header_auth_token=self.client_header_auth_token,
        )
        return self

    def __exit__(
        self,
        unused_exc_type: Optional[Type[BaseException]],
        unused_exc_value: Optional[BaseException],
        unused_traceback: Optional[TracebackType],
    ) -> None:
        """Exit ResourceResolution instance context.

        Client connection is closed.
        """
        self.client.close()

    def execute_workflows(self, *workflows: WorkflowExecution) -> Generator[WorkflowExecutionResult, None, None]:
        """Execute provided workflows.

        Workflows are going to be execured using one gRPC API call. Depends of implementation that may has
        some consequences. In some cases if any request fails all requests after that won't be called.

        Responses and zipped with workflows and WorkflowExecutionResult object is initialized and yielded.

        Raises:
            AttributeError: Raises if client object is not created. It occurs only if you not uses context manager.
                Then user have to create client instance for ResourceResolution object by himself calling:
                ```
                resource_resoulution.client = Client(
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
        if not self.client:
            raise AttributeError("gRPC client not connected")

        for response, workflow in zip(self.client.process((workflow.message for workflow in workflows)), workflows):
            yield WorkflowExecutionResult(workflow, response)
