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
import email.parser


def get_blueprint_id(request):
  blueprint_name = request.identifiers.blueprintName
  blueprint_version = request.identifiers.blueprintVersion
  return blueprint_name + '/' + blueprint_version

def get_blueprint_timeout(request):
  return request.timeOut

# Create a response for grpc. Fills in the timestamp as well as removes cds_is_successful element
def build_grpc_response(request, log_results, payload_return, is_success=False):
  if is_success:
    status = CommandExecutor_pb2.SUCCESS
  else:
    status = CommandExecutor_pb2.FAILURE

  timestamp = Timestamp()
  timestamp.GetCurrentTime()

  if "cds_is_successful" in payload_return:
    payload_return.pop('cds_is_successful')
  payload_str = json.dumps(payload_return)
  return CommandExecutor_pb2.ExecutionOutput(requestId=request.requestId,
                                             response=log_results,
                                             status=status,
                                             payload=payload_str,
                                             timestamp=timestamp)

# build a return data structure which may contain an error message
def build_ret_data(cds_is_successful, err_msg):
  ret_data = {"cds_is_successful": cds_is_successful }
  if err_msg != "":
    ret_data["err_msg"] = err_msg
  return ret_data
<<<<<<< HEAD   (eab2ad single /enrichandupload endpoint.)
=======

# Truncate execution logs to make sure gRPC response doesn't exceed the gRPC buffer capacity
def truncate_execution_output(execution_output):
  sum_truncated_chars = 0
  if execution_output.ByteSize() > RESPONSE_MAX_SIZE:
    while execution_output.ByteSize() > RESPONSE_MAX_SIZE:
        removed_item = execution_output.response.pop()
        sum_truncated_chars += len(removed_item)
    execution_output.response.append("[...] TRUNCATED CHARS : {}".format(sum_truncated_chars))
  return execution_output


# Read temp file 'outputfile' into results_log and split out the returned payload into payload_result
def parse_cmd_exec_output(outputfile, logger, payload_result, results_log):
  payload_section = []
  is_payload_section = False
  outputfile.seek(0)
  while True:
    output = outputfile.readline()
    if output == '':
      break
    if output.startswith('BEGIN_EXTRA_PAYLOAD'):
      is_payload_section = True
      output = outputfile.readline()
    if output.startswith('END_EXTRA_PAYLOAD'):
      is_payload_section = False
      output = ''
      payload = '\n'.join(payload_section)
      msg = email.parser.Parser().parsestr(payload)
      for part in msg.get_payload():
        payload_result.update(json.loads(part.get_payload()))
    if output and not is_payload_section:
      logger.info(output.strip())
      results_log.append(output.strip())
    else:
      payload_section.append(output.strip())

>>>>>>> CHANGE (4ad951 cmd-exec server-side timeout.)
