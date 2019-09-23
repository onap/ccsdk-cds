# -*- coding: utf-8 -*-

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

# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: BluePrintProcessing.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from google.protobuf import struct_pb2 as google_dot_protobuf_dot_struct__pb2
from blueprints_grpc.proto import BluePrintCommon_pb2 as BluePrintCommon__pb2

DESCRIPTOR = _descriptor.FileDescriptor(
  name='BluePrintProcessing.proto',
  package='org.onap.ccsdk.cds.controllerblueprints.processing.api',
  syntax='proto3',
  serialized_options=_b('P\001'),
  serialized_pb=_b('\n\x19\x42luePrintProcessing.proto\x12\x36org.onap.ccsdk.cds.controllerblueprints.processing.api\x1a\x1cgoogle/protobuf/struct.proto\x1a\x15\x42luePrintCommon.proto\"\xfb\x01\n\x15\x45xecutionServiceInput\x12V\n\x0c\x63ommonHeader\x18\x01 \x01(\x0b\x32@.org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader\x12`\n\x11\x61\x63tionIdentifiers\x18\x02 \x01(\x0b\x32\x45.org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers\x12(\n\x07payload\x18\x03 \x01(\x0b\x32\x17.google.protobuf.Struct\"\xc8\x02\n\x16\x45xecutionServiceOutput\x12V\n\x0c\x63ommonHeader\x18\x01 \x01(\x0b\x32@.org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader\x12`\n\x11\x61\x63tionIdentifiers\x18\x02 \x01(\x0b\x32\x45.org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers\x12J\n\x06status\x18\x03 \x01(\x0b\x32:.org.onap.ccsdk.cds.controllerblueprints.common.api.Status\x12(\n\x07payload\x18\x04 \x01(\x0b\x32\x17.google.protobuf.Struct2\xcb\x01\n\x1a\x42luePrintProcessingService\x12\xac\x01\n\x07process\x12M.org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput\x1aN.org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput(\x01\x30\x01\x42\x02P\x01\x62\x06proto3')
  ,
  dependencies=[google_dot_protobuf_dot_struct__pb2.DESCRIPTOR,BluePrintCommon__pb2.DESCRIPTOR,])




_EXECUTIONSERVICEINPUT = _descriptor.Descriptor(
  name='ExecutionServiceInput',
  full_name='org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='commonHeader', full_name='org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput.commonHeader', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='actionIdentifiers', full_name='org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput.actionIdentifiers', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='payload', full_name='org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput.payload', index=2,
      number=3, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=139,
  serialized_end=390,
)


_EXECUTIONSERVICEOUTPUT = _descriptor.Descriptor(
  name='ExecutionServiceOutput',
  full_name='org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='commonHeader', full_name='org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput.commonHeader', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='actionIdentifiers', full_name='org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput.actionIdentifiers', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='status', full_name='org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput.status', index=2,
      number=3, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='payload', full_name='org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput.payload', index=3,
      number=4, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=393,
  serialized_end=721,
)

_EXECUTIONSERVICEINPUT.fields_by_name['commonHeader'].message_type = BluePrintCommon__pb2._COMMONHEADER
_EXECUTIONSERVICEINPUT.fields_by_name['actionIdentifiers'].message_type = BluePrintCommon__pb2._ACTIONIDENTIFIERS
_EXECUTIONSERVICEINPUT.fields_by_name['payload'].message_type = google_dot_protobuf_dot_struct__pb2._STRUCT
_EXECUTIONSERVICEOUTPUT.fields_by_name['commonHeader'].message_type = BluePrintCommon__pb2._COMMONHEADER
_EXECUTIONSERVICEOUTPUT.fields_by_name['actionIdentifiers'].message_type = BluePrintCommon__pb2._ACTIONIDENTIFIERS
_EXECUTIONSERVICEOUTPUT.fields_by_name['status'].message_type = BluePrintCommon__pb2._STATUS
_EXECUTIONSERVICEOUTPUT.fields_by_name['payload'].message_type = google_dot_protobuf_dot_struct__pb2._STRUCT
DESCRIPTOR.message_types_by_name['ExecutionServiceInput'] = _EXECUTIONSERVICEINPUT
DESCRIPTOR.message_types_by_name['ExecutionServiceOutput'] = _EXECUTIONSERVICEOUTPUT
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

ExecutionServiceInput = _reflection.GeneratedProtocolMessageType('ExecutionServiceInput', (_message.Message,), {
  'DESCRIPTOR' : _EXECUTIONSERVICEINPUT,
  '__module__' : 'BluePrintProcessing_pb2'
  # @@protoc_insertion_point(class_scope:org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput)
  })
_sym_db.RegisterMessage(ExecutionServiceInput)

ExecutionServiceOutput = _reflection.GeneratedProtocolMessageType('ExecutionServiceOutput', (_message.Message,), {
  'DESCRIPTOR' : _EXECUTIONSERVICEOUTPUT,
  '__module__' : 'BluePrintProcessing_pb2'
  # @@protoc_insertion_point(class_scope:org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput)
  })
_sym_db.RegisterMessage(ExecutionServiceOutput)


DESCRIPTOR._options = None

_BLUEPRINTPROCESSINGSERVICE = _descriptor.ServiceDescriptor(
  name='BluePrintProcessingService',
  full_name='org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingService',
  file=DESCRIPTOR,
  index=0,
  serialized_options=None,
  serialized_start=724,
  serialized_end=927,
  methods=[
  _descriptor.MethodDescriptor(
    name='process',
    full_name='org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingService.process',
    index=0,
    containing_service=None,
    input_type=_EXECUTIONSERVICEINPUT,
    output_type=_EXECUTIONSERVICEOUTPUT,
    serialized_options=None,
  ),
])
_sym_db.RegisterServiceDescriptor(_BLUEPRINTPROCESSINGSERVICE)

DESCRIPTOR.services_by_name['BluePrintProcessingService'] = _BLUEPRINTPROCESSINGSERVICE

# @@protoc_insertion_point(module_scope)
