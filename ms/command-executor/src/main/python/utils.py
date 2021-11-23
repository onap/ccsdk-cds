#
# Copyright (C) 2019 - 2020 Bell Canada.
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

CDS_IS_SUCCESSFUL_KEY = "cds_is_successful"
ERR_MSG_KEY = "err_msg"
RESULTS_KEY = "results"
RESULTS_LOG_KEY = "results_log"
REUPLOAD_CBA_KEY = "reupload_cba"
RESPONSE_MAX_SIZE = 4 * 1024 * 1024  # 4Mb

# part of cba_name/version/uuid path
def blueprint_name_version_uuid(request):
  return get_blueprint_name(request) + '/' + get_blueprint_version(request) + '/' + get_blueprint_uuid(request)

# return blueprint_name and version part of the path (needed for legacy SR7 cmd-exec support
def blueprint_name_version(request):
  return get_blueprint_name(request) + '/' + get_blueprint_version(request)

def get_blueprint_name(request):
  return request.identifiers.blueprintName

def get_blueprint_version(request):
  return request.identifiers.blueprintVersion

def get_blueprint_uuid(request):
  return request.identifiers.blueprintUUID

def get_blueprint_timeout(request):
  return request.timeOut

def get_blueprint_requestid(request):
  return request.requestId

def get_blueprint_subRequestId(request):
  return request.subRequestId

# Create a response for grpc. Fills in the timestamp as well as removes cds_is_successful element
def build_grpc_response(request_id, response):
  if response[CDS_IS_SUCCESSFUL_KEY]:
    status = CommandExecutor_pb2.SUCCESS
  else:
    status = CommandExecutor_pb2.FAILURE

  response.pop(CDS_IS_SUCCESSFUL_KEY)
  logs = response.pop(RESULTS_LOG_KEY)

  errorMessage = ""
  if ERR_MSG_KEY in response:
    errorMessage = '\n'.join(response.pop(ERR_MSG_KEY))

  # Payload should only contain response data returned from the executed script and/or the error message
  payload = json.dumps(response)

  timestamp = Timestamp()
  timestamp.GetCurrentTime()

  execution_output = CommandExecutor_pb2.ExecutionOutput(requestId=request_id,
                                                         response=logs,
                                                         status=status,
                                                         payload=payload,
                                                         timestamp=timestamp,
                                                         errMsg=errorMessage)

  return truncate_execution_output(execution_output)


# return the status of validate blueprint UUID call rpc
def build_grpc_blueprint_validation_response(request_id, subrequest_id,
    cba_uuid, success=True):
  timestamp = Timestamp()
  timestamp.GetCurrentTime()
  return CommandExecutor_pb2.BlueprintValidationOutput(requestId=request_id,
                                                subRequestId=subrequest_id,
                                                status=CommandExecutor_pb2.SUCCESS if success else CommandExecutor_pb2.FAILURE,
                                                cbaUUID=cba_uuid,
                                                timestamp=timestamp)

def build_grpc_blueprint_upload_response(request_id, subrequest_id, success=True, payload=[]):
  timestamp = Timestamp()
  timestamp.GetCurrentTime()
  return CommandExecutor_pb2.UploadBlueprintOutput(requestId=request_id,
    subRequestId=subrequest_id,
    status=CommandExecutor_pb2.SUCCESS if success else CommandExecutor_pb2.FAILURE,
    timestamp=timestamp,
    payload=json.dumps(payload))

# build a ret data structure used to populate the ExecutionOutput
def build_ret_data(cds_is_successful, results_log=[], error=None, reupload_cba = False):
  ret_data = {
    CDS_IS_SUCCESSFUL_KEY: cds_is_successful,
    RESULTS_LOG_KEY: results_log
  }
  if error:
    ret_data[ERR_MSG_KEY] = error
  # CBA needs to be reuploaded case:
  if reupload_cba:
    ret_data[REUPLOAD_CBA_KEY] = True
  return ret_data


# Truncate execution logs to make sure gRPC response doesn't exceed the gRPC buffer capacity
def truncate_execution_output(execution_output):
  sum_truncated_chars = 0
  if execution_output.ByteSize() > RESPONSE_MAX_SIZE:
    while execution_output.ByteSize() > RESPONSE_MAX_SIZE:
      removed_item = execution_output.response.pop()
      sum_truncated_chars += len(removed_item)
    execution_output.response.append(
        "[...] TRUNCATED CHARS : {}".format(sum_truncated_chars))
  return execution_output


# Read temp file 'outputfile' into results_log and split out the returned payload into payload_result
def parse_cmd_exec_output(outputfile, logger, payload_result, err_msg_result, results_log,
    extra):
  payload_section = []
  ret_err_msg_section = []
  is_payload_section = False
  is_user_script_err_msg = False
  outputfile.seek(0)
  while True:
    line = outputfile.readline()
    if line == '':
      break
    # Read the user-supplied (script) return payload.
    if line.startswith('BEGIN_EXTRA_PAYLOAD'):
      is_payload_section = True
      line = outputfile.readline()
    if line.startswith('END_EXTRA_PAYLOAD'):
      is_payload_section = False
      line = ''
      payload = '\n'.join(payload_section)
      msg = email.parser.Parser().parsestr(payload)
      for part in msg.get_payload():
        payload_result.update(json.loads(part.get_payload()))

    # Read the user-supplied (script) error message string
    if line.startswith('BEGIN_EXTRA_RET_ERR_MSG'):
      is_user_script_err_msg = True
      line = outputfile.readline()
    if line.startswith('END_EXTRA_RET_ERR_MSG'):
      is_user_script_err_msg = False
      line = ''
      err_msg_result.append('\n'.join(ret_err_msg_section))
    if is_payload_section:
      payload_section.append(line.strip())
    elif is_user_script_err_msg:
      ret_err_msg_section.append(line.strip())
    elif line:
      logger.info(line.strip(), extra=extra)
      results_log.append(line.strip())


def getExtraLogData(request=None):
  extra = {'request_id': '', 'subrequest_id': '', 'originator_id': ''}
  if request is not None:
    extra = {
      'request_id': request.requestId,
      'subrequest_id': request.subRequestId,
      'originator_id': request.originatorId
    }
  return extra
