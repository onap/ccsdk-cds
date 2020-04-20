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
from pympler import asizeof

CDS_IS_SUCCESSFUL_KEY = "cds_is_successful"
ERR_MSG_KEY = "err_msg"
RESULTS_KEY = "results"
RESULTS_LOG_KEY = "results_log"
TRUNC_MSG_LEN = 3 * 1024 * 1024

def get_blueprint_id(request):
  blueprint_name = request.identifiers.blueprintName
  blueprint_version = request.identifiers.blueprintVersion
  return blueprint_name + '/' + blueprint_version

# Create a response for grpc. Fills in the timestamp as well as removes cds_is_successful element
def build_grpc_response(request_id, response):
  if response[CDS_IS_SUCCESSFUL_KEY]:
    status = CommandExecutor_pb2.SUCCESS
    payload = json.dumps(response[RESULTS_KEY])
  else:
    status = CommandExecutor_pb2.FAILURE
    # truncate error message if too heavy
    if asizeof.asizeof(response[ERR_MSG_KEY]) > TRUNC_MSG_LEN:
      response[ERR_MSG_KEY] = "{} [...]. Check command executor logs for more information.".format(response[ERR_MSG_KEY][:TRUNC_MSG_LEN])
    payload = json.dumps(response[ERR_MSG_KEY])

  # truncate cmd-exec logs if too heavy
  response[RESULTS_LOG_KEY] = truncate_cmd_exec_logs(response[RESULTS_LOG_KEY])

  timestamp = Timestamp()
  timestamp.GetCurrentTime()

  return CommandExecutor_pb2.ExecutionOutput(requestId=request_id,
                                             response=response[RESULTS_LOG_KEY],
                                             status=status,
                                             payload=payload,
                                             timestamp=timestamp)

# build a ret data structure
def build_ret_data(cds_is_successful, results={}, results_log=[], error=None):
  ret_data = {
            CDS_IS_SUCCESSFUL_KEY: cds_is_successful,
            RESULTS_KEY: results,
            RESULTS_LOG_KEY: results_log
         }
  if error:
    ret_data[ERR_MSG_KEY] = error
  return ret_data

def truncate_cmd_exec_logs(logs):
    truncated_logs = []
    truncated_logs_memsize = 0
    for log in logs:
        truncated_logs_memsize += asizeof.asizeof(log)
        if truncated_logs_memsize > TRUNC_MSG_LEN:
            truncated_logs.append("Execution logs exceeds the maximum size allowed. Check command executor logs to view the execute-command-logs.")
            break
        truncated_logs.append(log)
    return truncated_logs