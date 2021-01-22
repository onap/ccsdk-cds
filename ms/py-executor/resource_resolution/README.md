# Resource resolution GRPC client

##  How to use examples

### Insecure channel

```
from proto.BlueprintCommon_pb2_grpc import ActionIdentifiers, CommonHeader
from proto.BlueprintProcessing_pb2_grpc import ExecutionServiceInput
from resource_resolution.grpc.client import Client as ResourceResolutionClient


def generate_messages():
    commonHeader = CommonHeader()
    commonHeader.requestId = "1234"
    commonHeader.subRequestId = "1234-1"
    commonHeader.originatorId = "CDS"

    actionIdentifiers = ActionIdentifiers()
    actionIdentifiers.blueprintName = "sample-cba"
    actionIdentifiers.blueprintVersion = "1.0.0"
    actionIdentifiers.actionName = "SampleScript"

    input = ExecutionServiceInput(commonHeader=commonHeader, actionIdentifiers=actionIdentifiers)

    commonHeader2 = CommonHeader()
    commonHeader2.requestId = "1235"
    commonHeader2.subRequestId = "1234-2"
    commonHeader2.originatorId = "CDS"

    input2 = ExecutionServiceInput(commonHeader=commonHeader2, actionIdentifiers=actionIdentifiers)

    yield from [input, input2]


if __name__ == "__main__":
    with ResourceResolutionClient("localhost:50052") as client:
        for response in client.process(generate_messages()):
            print(response)

```

### Secure channel

```
from proto.BlueprintCommon_pb2_grpc import ActionIdentifiers, CommonHeader
from proto.BlueprintProcessing_pb2_grpc import ExecutionServiceInput
from resource_resolution.grpc.client import Client as ResourceResolutionClient


def generate_messages():
    commonHeader = CommonHeader()
    commonHeader.requestId = "1234"
    commonHeader.subRequestId = "1234-1"
    commonHeader.originatorId = "CDS"

    actionIdentifiers = ActionIdentifiers()
    actionIdentifiers.blueprintName = "sample-cba"
    actionIdentifiers.blueprintVersion = "1.0.0"
    actionIdentifiers.actionName = "SampleScript"

    input = ExecutionServiceInput(commonHeader=commonHeader, actionIdentifiers=actionIdentifiers)

    commonHeader2 = CommonHeader()
    commonHeader2.requestId = "1235"
    commonHeader2.subRequestId = "1234-2"
    commonHeader2.originatorId = "CDS"

    input2 = ExecutionServiceInput(commonHeader=commonHeader2, actionIdentifiers=actionIdentifiers)

    yield from [input, input2]


if __name__ == "__main__":
    with open("certs/py-executor/py-executor-chain.pem", "rb") as f:
        with ResourceResolutionClient("localhost:50052", use_ssl=True, root_certificates=f.read()) as client:
            for response in client.process(generate_messages()):
                print(response)

```

### Authorizarion header

```
from proto.BlueprintCommon_pb2 import ActionIdentifiers, CommonHeader
from proto.BlueprintProcessing_pb2 import ExecutionServiceInput
from resource_resolution.grpc.client import Client as ResourceResolutionClient


def generate_messages():
    commonHeader = CommonHeader()
    commonHeader.requestId = "1234"
    commonHeader.subRequestId = "1234-1"
    commonHeader.originatorId = "CDS"

    actionIdentifiers = ActionIdentifiers()
    actionIdentifiers.blueprintName = "sample-cba"
    actionIdentifiers.blueprintVersion = "1.0.0"
    actionIdentifiers.actionName = "SampleScript"

    input = ExecutionServiceInput(commonHeader=commonHeader, actionIdentifiers=actionIdentifiers)

    commonHeader2 = CommonHeader()
    commonHeader2.requestId = "1235"
    commonHeader2.subRequestId = "1234-2"
    commonHeader2.originatorId = "CDS"

    input2 = ExecutionServiceInput(commonHeader=commonHeader2, actionIdentifiers=actionIdentifiers)

    yield from [input, input2]


if __name__ == "__main__":
    with ResourceResolutionClient("127.0.0.1:9111", use_header_auth=True, header_auth_token="Token test") as client:
        for response in client.process(generate_messages()):
            print(response)

```

# ResourceResoulution helper class

## How to use examples

### GRPC insecure channel

```
from resource_resolution.resource_resolution import ResourceResolution, WorkflowExecution, WorkflowExecutionResult


if __name__ == "__main__":
    with ResourceResolution(use_header_auth=True, header_auth_token="Basic token") as rr:
        for response in rr.execute_workflows(  # type: WorkflowExecutionResult
            WorkflowExecution(
                blueprint_name="blueprintName",
                blueprint_version="1.0",
                workflow_name="resource-assignment"
            )
        ):
            if response.has_error:
                print(response.error_message)
            else:
                print(response.payload)
```

### HTTP retrieve/store template

```
from resource_resolution.resource_resolution import ResourceResolution

if __name__ == "__main__":
    # If you want to use only HTTP you don't have to use context manager
    r = ResourceResolution(
        http_server_port=8081,
        http_auth_user="ccsdkapps",
        http_auth_pass="ccsdkapps",
        http_use_tls=False
    )
    r.store_template(
        blueprint_name="blueprintName",
        blueprint_version="1.0.0", 
        artifact_name="test",
        resolution_key="test", 
        result="test")
    template = r.retrieve_template(
        blueprint_name="blueprintName",
        blueprint_version="1.0.0", 
        artifact_name="test",
        resolution_key="test",
    )
    assert template.result == "test"
    template.result = "another value"
    template.store()
    another_template = r.retrieve_template(
        blueprint_name="blueprintName",
        blueprint_version="1.0.0", 
        artifact_name="test",
        resolution_key="test",
    )
    assert another_template.result == "another_value"
```