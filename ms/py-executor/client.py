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

import grpc
from proto.BlueprintCommon_pb2 import ActionIdentifiers, CommonHeader
from proto.BlueprintProcessing_pb2 import ExecutionServiceInput
from proto.BlueprintProcessing_pb2_grpc import BlueprintProcessingServiceStub


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

    inputs = [input, input2]
    for input in inputs:
        print(input)
        yield input


if __name__ == '__main__':
    with open('py-executor-chain.pem', 'rb') as f:
        creds = grpc.ssl_channel_credentials(f.read())
    channel = grpc.secure_channel('localhost:50052', creds)
    stub = BlueprintProcessingServiceStub(channel)

    messages = generate_messages()
    responses = stub.process(messages)
    for response in responses:
        print(response)
