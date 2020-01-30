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
from google.protobuf.timestamp_pb2 import Timestamp

import proto.CommandExecutor_pb2 as CommandExecutor_pb2
import json


def get_blueprint_id(request):
  blueprint_name = request.identifiers.blueprintName
  blueprint_version = request.identifiers.blueprintVersion
  return blueprint_name + '/' + blueprint_version

# Create a response for grpc. Fills in the timestamp as well as removes cds_is_successful element
def build_grpc_response(request, log_results, payload_return, is_success=False):
  if is_success:
    status = CommandExecutor_pb2.SUCCESS
  else:
    status = CommandExecutor_pb2.FAILURE

  timestamp = Timestamp()
  timestamp.GetCurrentTime()

  if payload_return["cds_is_successful"]:
    payload_return.pop('cds_is_successful')
  payload_str = json.dumps(payload_return)
  return CommandExecutor_pb2.ExecutionOutput(requestId=request.requestId,
                                             response=log_results,
                                             status=status,
                                             payload=payload_str,
                                             timestamp=timestamp)

# build a return data structure which contains an error msg
def build_ret_data(cds_is_successful, err_msg):
  ret_data = {"cds_is_successful": cds_is_successful, "err_msg": err_msg}
  return ret_data
