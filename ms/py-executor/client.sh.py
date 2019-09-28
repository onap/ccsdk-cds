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

import grpc
from blueprints_grpc.proto.BluePrintProcessing_pb2_grpc import BluePrintProcessingServiceStub
from blueprints_grpc.proto.BluePrintProcessing_pb2 import ExecutionServiceInput
from blueprints_grpc.proto.BluePrintCommon_pb2 import CommonHeader, ActionIdentifiers


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
    channel = grpc.insecure_channel('localhost:50052')
    stub = BluePrintProcessingServiceStub(channel)

    messages = generate_messages()
    responses = stub.process(messages)
    for response in responses:
        print("*********************")
        print(response)
