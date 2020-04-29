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
from unittest.mock import patch, MagicMock

from google.protobuf import json_format
from pytest import raises

from resource_resolution.resource_resolution import (
    ExecutionServiceInput,
    ExecutionServiceOutput,
    ResourceResolution,
    Template,
    WorkflowExecution,
    WorkflowExecutionResult,
    WorkflowMode,
)


def test_workflow_execution_class():
    """Workflow execution class tests.

    - Test initialization and default values
    - Test request message formatting
    """
    # Without inputs
    workflow_execution: WorkflowExecution = WorkflowExecution("test blueprint", "test version", "test workflow")
    assert workflow_execution.blueprint_name == "test blueprint"
    assert workflow_execution.blueprint_version == "test version"
    assert workflow_execution.workflow_name == "test workflow"
    assert workflow_execution.workflow_inputs == {}
    assert workflow_execution.workflow_mode == WorkflowMode.SYNC

    msg: ExecutionServiceInput = workflow_execution.message
    msg_dict: dict = json_format.MessageToDict(msg)
    assert msg_dict["actionIdentifiers"]["blueprintName"] == "test blueprint"
    assert msg_dict["actionIdentifiers"]["blueprintVersion"] == "test version"
    assert msg_dict["actionIdentifiers"]["actionName"] == "test workflow"
    assert msg_dict["actionIdentifiers"]["mode"] == "sync"
    assert list(msg_dict["payload"].keys())[0] == "test workflow-request"
    assert msg_dict["payload"]["test workflow-request"] == {}

    # With inputs
    workflow_execution: WorkflowExecution = WorkflowExecution(
        "test blueprint2",
        "test version2",
        "test workflow2",
        workflow_inputs={"test": "test"},
        workflow_mode=WorkflowMode.ASYNC,
    )
    assert workflow_execution.blueprint_name == "test blueprint2"
    assert workflow_execution.blueprint_version == "test version2"
    assert workflow_execution.workflow_name == "test workflow2"
    assert workflow_execution.workflow_inputs == {"test": "test"}
    assert workflow_execution.workflow_mode == WorkflowMode.ASYNC

    msg: ExecutionServiceInput = workflow_execution.message
    msg_dict: dict = json_format.MessageToDict(msg)
    assert msg_dict["actionIdentifiers"]["blueprintName"] == "test blueprint2"
    assert msg_dict["actionIdentifiers"]["blueprintVersion"] == "test version2"
    assert msg_dict["actionIdentifiers"]["actionName"] == "test workflow2"
    assert msg_dict["actionIdentifiers"]["mode"] == "async"
    assert list(msg_dict["payload"].keys())[0] == "test workflow2-request"
    assert msg_dict["payload"]["test workflow2-request"] == {"test": "test"}


def test_workflow_execution_result_class():
    """Workflow execution result class tests.

    - Test initizalization and default values
    - Test `has_error` property
    - Test `error_message` property
    - Test payload formatting
    """
    workflow_execution: WorkflowExecution = WorkflowExecution("test blueprint", "test version", "test workflow")
    execution_output: ExecutionServiceOutput = ExecutionServiceOutput()
    execution_output.actionIdentifiers.blueprintName = "test blueprint"
    execution_output.actionIdentifiers.blueprintVersion = "test version"
    execution_output.actionIdentifiers.actionName = "test workflow"
    execution_output.status.code = 200

    execution_result: WorkflowExecutionResult = WorkflowExecutionResult(workflow_execution, execution_output)
    assert not execution_result.has_error
    with raises(AttributeError):
        execution_result.error_message
    assert execution_result.payload == {}
    assert execution_result.blueprint_name == "test blueprint"
    assert execution_result.blueprint_version == "test version"
    assert execution_result.workflow_name == "test workflow"

    execution_output.payload.update({"test_key": "test_value"})
    execution_result: WorkflowExecutionResult = WorkflowExecutionResult(workflow_execution, execution_output)
    assert execution_result.payload == {"test_key": "test_value"}

    execution_output.status.code = 500
    assert execution_result.has_error
    assert execution_result.error_message == ""


def test_resource_resolution_check_resolve_params():
    """Check values of potentially HTTP parameters."""
    rr = ResourceResolution()
    with raises(AttributeError):
        rr._check_template_resolve_params()
        rr._check_template_resolve_params(resource_type="test")
        rr._check_template_resolve_params(resource_id="test")
    rr._check_template_resolve_params(resolution_key="test")
    rr._check_template_resolve_params(resource_type="test", resource_id="test")


def test_store_template():
    """Test store_template method.

    Checks if http_client send_request method is called with valid parameters.
    """
    rr = ResourceResolution(server_address="127.0.0.1", http_server_port=8080)
    rr.http_client = MagicMock()
    rr.store_template(
        blueprint_name="test_blueprint_name",
        blueprint_version="test_blueprint_version",
        artifact_name="test_artifact_name",
        resolution_key="test_resolution_key",
        result="test_result",
    )
    rr.http_client.send_request.assert_called_once_with(
        "POST",
        "template/test_blueprint_name/test_blueprint_version/test_artifact_name/test_resolution_key",
        data=json.dumps({"result": "test_result"}),
        headers={"Content-Type": "application/json"},
    )


def test_retrieve_template():
    """Test retrieve_template method.

    Checks if http_client send_request method is called with valid parameters.
    """
    rr = ResourceResolution(server_address="127.0.0.1", http_server_port=8080)
    rr.http_client = MagicMock()
    rr.retrieve_template(
        blueprint_name="test_blueprint_name",
        blueprint_version="test_blueprint_version",
        artifact_name="test_artifact_name",
        resolution_key="test_resolution_key",
    )
    rr.http_client.send_request.assert_called_once_with(
        "GET",
        "template",
        params={
            "bpName": "test_blueprint_name",
            "bpVersion": "test_blueprint_version",
            "artifactName": "test_artifact_name",
            "resolutionKey": "test_resolution_key",
        },
        headers={"Accept": "application/json"},
    )
