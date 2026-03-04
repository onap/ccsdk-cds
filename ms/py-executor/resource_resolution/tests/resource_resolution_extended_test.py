#
# Copyright (C) 2026 Deutsche Telekom.
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

"""
Extended tests for resource_resolution/resource_resolution.py —
covers context manager, execute_workflows, Template.store,
store_template/retrieve_template with resource_type/resource_id,
and edge cases in _check_template_resolve_params.
"""

import json
from unittest.mock import MagicMock, patch, PropertyMock

import pytest

from proto.BluePrintProcessing_pb2 import ExecutionServiceOutput

from resource_resolution.resource_resolution import (
    ResourceResolution,
    Template,
    WorkflowExecution,
    WorkflowExecutionResult,
    WorkflowMode,
)


# ---------------------------------------------------------------------------
# ResourceResolution context manager
# ---------------------------------------------------------------------------

class TestResourceResolutionContextManager:

    @patch("resource_resolution.resource_resolution.GrpcClient")
    def test_enter_creates_grpc_client(self, MockGrpcClient):
        rr = ResourceResolution(server_address="10.0.0.1", grpc_server_port=9111)
        result = rr.__enter__()

        MockGrpcClient.assert_called_once_with(
            server_address="10.0.0.1:9111",
            use_ssl=False,
            root_certificates=None,
            private_key=None,
            certificate_chain=None,
            use_header_auth=False,
            header_auth_token=None,
        )
        assert result is rr
        assert rr.grpc_client is MockGrpcClient.return_value

    @patch("resource_resolution.resource_resolution.GrpcClient")
    def test_exit_closes_grpc_client(self, MockGrpcClient):
        rr = ResourceResolution()
        rr.__enter__()
        rr.__exit__(None, None, None)

        MockGrpcClient.return_value.close.assert_called_once()

    @patch("resource_resolution.resource_resolution.GrpcClient")
    def test_with_statement(self, MockGrpcClient):
        with ResourceResolution() as rr:
            assert rr.grpc_client is MockGrpcClient.return_value
        MockGrpcClient.return_value.close.assert_called_once()

    @patch("resource_resolution.resource_resolution.GrpcClient")
    def test_enter_with_ssl(self, MockGrpcClient):
        rr = ResourceResolution(
            server_address="secure.host",
            grpc_server_port=9999,
            use_ssl=True,
            root_certificates=b"root",
            private_key=b"key",
            certificate_chain=b"chain",
        )
        rr.__enter__()

        MockGrpcClient.assert_called_once_with(
            server_address="secure.host:9999",
            use_ssl=True,
            root_certificates=b"root",
            private_key=b"key",
            certificate_chain=b"chain",
            use_header_auth=False,
            header_auth_token=None,
        )

    @patch("resource_resolution.resource_resolution.GrpcClient")
    def test_enter_with_header_auth(self, MockGrpcClient):
        rr = ResourceResolution(
            use_header_auth=True,
            header_auth_token="Bearer abc",
        )
        rr.__enter__()

        call_kwargs = MockGrpcClient.call_args[1]
        assert call_kwargs["use_header_auth"] is True
        assert call_kwargs["header_auth_token"] == "Bearer abc"


# ---------------------------------------------------------------------------
# ResourceResolution.execute_workflows
# ---------------------------------------------------------------------------

class TestExecuteWorkflows:

    @patch("resource_resolution.resource_resolution.GrpcClient")
    def test_executes_single_workflow(self, MockGrpcClient):
        mock_response = ExecutionServiceOutput()
        mock_response.status.code = 200
        MockGrpcClient.return_value.process.return_value = iter([mock_response])

        workflow = WorkflowExecution("bp", "1.0", "wf")

        with ResourceResolution() as rr:
            results = list(rr.execute_workflows(workflow))

        assert len(results) == 1
        assert isinstance(results[0], WorkflowExecutionResult)
        assert results[0].workflow_execution is workflow
        assert results[0].execution_output is mock_response

    @patch("resource_resolution.resource_resolution.GrpcClient")
    def test_executes_multiple_workflows(self, MockGrpcClient):
        resp1 = ExecutionServiceOutput()
        resp1.status.code = 200
        resp2 = ExecutionServiceOutput()
        resp2.status.code = 500

        MockGrpcClient.return_value.process.return_value = iter([resp1, resp2])

        wf1 = WorkflowExecution("bp1", "1.0", "wf1")
        wf2 = WorkflowExecution("bp2", "2.0", "wf2")

        with ResourceResolution() as rr:
            results = list(rr.execute_workflows(wf1, wf2))

        assert len(results) == 2
        assert results[0].workflow_execution is wf1
        assert results[1].workflow_execution is wf2
        assert not results[0].has_error
        assert results[1].has_error

    def test_raises_without_client(self):
        rr = ResourceResolution()
        workflow = WorkflowExecution("bp", "1.0", "wf")

        with pytest.raises(AttributeError, match="gRPC client not connected"):
            list(rr.execute_workflows(workflow))


# ---------------------------------------------------------------------------
# _check_template_resolve_params edge cases
# ---------------------------------------------------------------------------

class TestCheckTemplateResolveParams:

    def test_resolution_key_only_is_valid(self):
        rr = ResourceResolution()
        # Should not raise
        rr._check_template_resolve_params(resolution_key="key1")

    def test_resource_type_and_id_is_valid(self):
        rr = ResourceResolution()
        rr._check_template_resolve_params(resource_type="vnf", resource_id="123")

    def test_no_params_raises(self):
        rr = ResourceResolution()
        with pytest.raises(AttributeError):
            rr._check_template_resolve_params()

    def test_resource_type_only_raises(self):
        rr = ResourceResolution()
        with pytest.raises(AttributeError):
            rr._check_template_resolve_params(resource_type="vnf")

    def test_resource_id_only_raises(self):
        rr = ResourceResolution()
        with pytest.raises(AttributeError):
            rr._check_template_resolve_params(resource_id="123")


# ---------------------------------------------------------------------------
# store_template with resource_type/resource_id
# ---------------------------------------------------------------------------

class TestStoreTemplate:

    def test_store_with_resolution_key(self):
        rr = ResourceResolution()
        rr.http_client = MagicMock()
        rr.store_template(
            blueprint_name="bp",
            blueprint_version="1.0",
            artifact_name="art",
            resolution_key="key1",
            result="template_result",
        )
        rr.http_client.send_request.assert_called_once_with(
            "POST",
            "template/bp/1.0/art/key1",
            headers={"Content-Type": "application/json"},
            data=json.dumps({"result": "template_result"}),
        )

    def test_store_with_resource_type_and_id(self):
        rr = ResourceResolution()
        rr.http_client = MagicMock()
        rr.store_template(
            blueprint_name="bp",
            blueprint_version="2.0",
            artifact_name="art",
            resource_type="vnf",
            resource_id="vnf-001",
            result="some result",
        )
        rr.http_client.send_request.assert_called_once_with(
            "POST",
            "template/bp/2.0/vnf/vnf-001",
            headers={"Content-Type": "application/json"},
            data=json.dumps({"result": "some result"}),
        )

    def test_store_raises_without_valid_params(self):
        rr = ResourceResolution()
        rr.http_client = MagicMock()
        with pytest.raises(AttributeError):
            rr.store_template(
                blueprint_name="bp",
                blueprint_version="1.0",
                artifact_name="art",
                result="data",
            )


# ---------------------------------------------------------------------------
# retrieve_template with resource_type/resource_id
# ---------------------------------------------------------------------------

class TestRetrieveTemplate:

    def test_retrieve_with_resolution_key(self):
        rr = ResourceResolution()
        rr.http_client = MagicMock()
        mock_response = MagicMock()
        mock_response.json.return_value = {"result": "template data"}
        rr.http_client.send_request.return_value = mock_response

        template = rr.retrieve_template(
            blueprint_name="bp",
            blueprint_version="1.0",
            artifact_name="art",
            resolution_key="key1",
        )

        rr.http_client.send_request.assert_called_once_with(
            "GET",
            "template",
            headers={"Accept": "application/json"},
            params={
                "bpName": "bp",
                "bpVersion": "1.0",
                "artifactName": "art",
                "resolutionKey": "key1",
            },
        )
        assert isinstance(template, Template)
        assert template.result == "template data"
        assert template.blueprint_name == "bp"
        assert template.resolution_key == "key1"

    def test_retrieve_with_resource_type_and_id(self):
        rr = ResourceResolution()
        rr.http_client = MagicMock()
        mock_response = MagicMock()
        mock_response.json.return_value = {"result": "type+id data"}
        rr.http_client.send_request.return_value = mock_response

        template = rr.retrieve_template(
            blueprint_name="bp",
            blueprint_version="2.0",
            artifact_name="art",
            resource_type="pnf",
            resource_id="pnf-99",
        )

        call_kwargs = rr.http_client.send_request.call_args
        params = call_kwargs[1]["params"]
        assert "resourceType" in params
        assert params["resourceType"] == "pnf"
        assert params["resourceId"] == "pnf-99"
        assert "resolutionKey" not in params

        assert template.resource_type == "pnf"
        assert template.resource_id == "pnf-99"


# ---------------------------------------------------------------------------
# Template.store
# ---------------------------------------------------------------------------

class TestTemplateStore:

    def test_store_calls_resource_resolution_store_template(self):
        mock_rr = MagicMock(spec=ResourceResolution)
        template = Template(
            resource_resolution=mock_rr,
            blueprint_name="bp",
            blueprint_version="1.0",
            artifact_name="art",
            result="stored result",
            resolution_key="key1",
        )

        template.store()

        mock_rr.store_template.assert_called_once_with(
            blueprint_name="bp",
            blueprint_version="1.0",
            artifact_name="art",
            result="stored result",
            resolution_key="key1",
            resource_type=None,
            resource_id=None,
        )

    def test_store_with_resource_type_id(self):
        mock_rr = MagicMock(spec=ResourceResolution)
        template = Template(
            resource_resolution=mock_rr,
            blueprint_name="bp",
            blueprint_version="2.0",
            artifact_name="art",
            result="data",
            resource_type="vnf",
            resource_id="vnf-1",
        )

        template.store()

        mock_rr.store_template.assert_called_once_with(
            blueprint_name="bp",
            blueprint_version="2.0",
            artifact_name="art",
            result="data",
            resolution_key=None,
            resource_type="vnf",
            resource_id="vnf-1",
        )


# ---------------------------------------------------------------------------
# WorkflowExecution edge cases
# ---------------------------------------------------------------------------

class TestWorkflowExecutionEdgeCases:

    def test_none_inputs_default_to_empty_dict(self):
        wf = WorkflowExecution("bp", "1.0", "wf", workflow_inputs=None)
        assert wf.workflow_inputs == {}

    def test_async_mode(self):
        wf = WorkflowExecution("bp", "1.0", "wf", workflow_mode=WorkflowMode.ASYNC)
        msg = wf.message
        assert msg.actionIdentifiers.mode == "async"

    def test_sync_mode_default(self):
        wf = WorkflowExecution("bp", "1.0", "wf")
        msg = wf.message
        assert msg.actionIdentifiers.mode == "sync"


# ---------------------------------------------------------------------------
# WorkflowExecutionResult edge cases
# ---------------------------------------------------------------------------

class TestWorkflowExecutionResultEdgeCases:

    def test_has_error_false_for_200(self):
        wf = WorkflowExecution("bp", "1.0", "wf")
        output = ExecutionServiceOutput()
        output.status.code = 200
        result = WorkflowExecutionResult(wf, output)
        assert not result.has_error

    def test_has_error_true_for_non_200(self):
        wf = WorkflowExecution("bp", "1.0", "wf")
        output = ExecutionServiceOutput()
        output.status.code = 500
        result = WorkflowExecutionResult(wf, output)
        assert result.has_error

    def test_error_message_raises_when_no_error(self):
        wf = WorkflowExecution("bp", "1.0", "wf")
        output = ExecutionServiceOutput()
        output.status.code = 200
        result = WorkflowExecutionResult(wf, output)
        with pytest.raises(AttributeError, match="Execution does not finish with error"):
            _ = result.error_message

    def test_error_message_returns_string_when_error(self):
        wf = WorkflowExecution("bp", "1.0", "wf")
        output = ExecutionServiceOutput()
        output.status.code = 500
        output.status.errorMessage = "Something broke"
        result = WorkflowExecutionResult(wf, output)
        assert result.error_message == "Something broke"

    def test_payload_returns_dict(self):
        wf = WorkflowExecution("bp", "1.0", "wf")
        output = ExecutionServiceOutput()
        output.payload.update({"data": "value"})
        result = WorkflowExecutionResult(wf, output)
        assert result.payload == {"data": "value"}

    def test_blueprint_name_from_output(self):
        wf = WorkflowExecution("bp", "1.0", "wf")
        output = ExecutionServiceOutput()
        output.actionIdentifiers.blueprintName = "server-bp"
        result = WorkflowExecutionResult(wf, output)
        assert result.blueprint_name == "server-bp"

    def test_blueprint_version_from_output(self):
        wf = WorkflowExecution("bp", "1.0", "wf")
        output = ExecutionServiceOutput()
        output.actionIdentifiers.blueprintVersion = "3.0"
        result = WorkflowExecutionResult(wf, output)
        assert result.blueprint_version == "3.0"

    def test_workflow_name_from_output(self):
        wf = WorkflowExecution("bp", "1.0", "wf")
        output = ExecutionServiceOutput()
        output.actionIdentifiers.actionName = "server-wf"
        result = WorkflowExecutionResult(wf, output)
        assert result.workflow_name == "server-wf"


# ---------------------------------------------------------------------------
# ResourceResolution init with env vars
# ---------------------------------------------------------------------------

class TestResourceResolutionInit:

    @patch.dict("os.environ", {"AUTH_TOKEN": "env-token", "API_USERNAME": "env-user", "API_PASSWORD": "env-pass"})
    def test_env_vars_used_when_no_explicit_values(self):
        rr = ResourceResolution()
        assert rr.grpc_client_header_auth_token == "env-token"
        assert rr.http_client.auth_user == "env-user"
        assert rr.http_client.auth_pass == "env-pass"

    def test_explicit_values_override_env(self):
        rr = ResourceResolution(
            header_auth_token="explicit-token",
            http_auth_user="explicit-user",
            http_auth_pass="explicit-pass",
        )
        assert rr.grpc_client_header_auth_token == "explicit-token"
        assert rr.http_client.auth_user == "explicit-user"
        assert rr.http_client.auth_pass == "explicit-pass"

    def test_default_values(self):
        rr = ResourceResolution()
        assert rr.grpc_client_server_address == "127.0.0.1"
        assert rr.grpc_client_server_port == 9111
        assert rr.grpc_client_use_ssl is False
        assert rr.grpc_client_use_header_auth is False
