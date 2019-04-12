#
# Copyright (C) 2019 Bell Canada.
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

import proto.CommandExecutor_pb2 as CommandExecutor_pb2


def get_blueprint_id(request):
    blueprint_name = request.identifiers.blueprintName
    blueprint_version = request.identifiers.blueprintVersion
    return blueprint_name + '/' + blueprint_version


def build_response(request, is_success=True):
    if is_success:
        response = "SUCCESS"
    else:
        response = "FAILURE"
    return CommandExecutor_pb2.ExecutionOutput(requestId=request.identifiers.requestId, response=response)
