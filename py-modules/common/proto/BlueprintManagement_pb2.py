# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: BlueprintManagement.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf.internal import enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from google.protobuf import struct_pb2 as google_dot_protobuf_dot_struct__pb2
import proto.BlueprintCommon_pb2 as BlueprintCommon__pb2


DESCRIPTOR = _descriptor.FileDescriptor(
  name='BlueprintManagement.proto',
  package='org.onap.ccsdk.cds.controllerblueprints.management.api',
  syntax='proto3',
  serialized_options=_b('P\001'),
  serialized_pb=_b('\n\x19\x42lueprintManagement.proto\x12\x36org.onap.ccsdk.cds.controllerblueprints.management.api\x1a\x1cgoogle/protobuf/struct.proto\x1a\x15\x42lueprintCommon.proto\"\xd3\x02\n\x14\x42lueprintUploadInput\x12V\n\x0c\x63ommonHeader\x18\x01 \x01(\x0b\x32@.org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader\x12T\n\tfileChunk\x18\x02 \x01(\x0b\x32\x41.org.onap.ccsdk.cds.controllerblueprints.management.api.FileChunk\x12`\n\x11\x61\x63tionIdentifiers\x18\x03 \x01(\x0b\x32\x45.org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers\x12+\n\nproperties\x18\x04 \x01(\x0b\x32\x17.google.protobuf.Struct\"\xff\x01\n\x16\x42lueprintDownloadInput\x12V\n\x0c\x63ommonHeader\x18\x01 \x01(\x0b\x32@.org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader\x12`\n\x11\x61\x63tionIdentifiers\x18\x02 \x01(\x0b\x32\x45.org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers\x12+\n\nproperties\x18\x03 \x01(\x0b\x32\x17.google.protobuf.Struct\"\xfd\x01\n\x14\x42lueprintRemoveInput\x12V\n\x0c\x63ommonHeader\x18\x01 \x01(\x0b\x32@.org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader\x12`\n\x11\x61\x63tionIdentifiers\x18\x02 \x01(\x0b\x32\x45.org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers\x12+\n\nproperties\x18\x03 \x01(\x0b\x32\x17.google.protobuf.Struct\"\xb9\x01\n\x17\x42lueprintBootstrapInput\x12V\n\x0c\x63ommonHeader\x18\x01 \x01(\x0b\x32@.org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader\x12\x0f\n\x07loadCBA\x18\x02 \x01(\x08\x12\x15\n\rloadModelType\x18\x03 \x01(\x08\x12\x1e\n\x16loadResourceDictionary\x18\x04 \x01(\x08\"\xc2\x02\n\x19\x42lueprintManagementOutput\x12V\n\x0c\x63ommonHeader\x18\x01 \x01(\x0b\x32@.org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader\x12T\n\tfileChunk\x18\x02 \x01(\x0b\x32\x41.org.onap.ccsdk.cds.controllerblueprints.management.api.FileChunk\x12J\n\x06status\x18\x03 \x01(\x0b\x32:.org.onap.ccsdk.cds.controllerblueprints.common.api.Status\x12+\n\nproperties\x18\x04 \x01(\x0b\x32\x17.google.protobuf.Struct\"\x1a\n\tFileChunk\x12\r\n\x05\x63hunk\x18\x01 \x01(\x0c*4\n\x0e\x44ownloadAction\x12\n\n\x06SEARCH\x10\x00\x12\x0b\n\x07STARTER\x10\x01\x12\t\n\x05\x43LONE\x10\x02*@\n\x0cUploadAction\x12\t\n\x05\x44RAFT\x10\x00\x12\n\n\x06\x45NRICH\x10\x01\x12\x0c\n\x08VALIDATE\x10\x02\x12\x0b\n\x07PUBLISH\x10\x03*\x1b\n\x0cRemoveAction\x12\x0b\n\x07\x44\x45\x46\x41ULT\x10\x00\x32\xfa\x05\n\x1a\x42lueprintManagementService\x12\xb6\x01\n\x11\x64ownloadBlueprint\x12N.org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintDownloadInput\x1aQ.org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementOutput\x12\xb2\x01\n\x0fuploadBlueprint\x12L.org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintUploadInput\x1aQ.org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementOutput\x12\xb2\x01\n\x0fremoveBlueprint\x12L.org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintRemoveInput\x1aQ.org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementOutput\x12\xb8\x01\n\x12\x62ootstrapBlueprint\x12O.org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintBootstrapInput\x1aQ.org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementOutputB\x02P\x01\x62\x06proto3')
  ,
  dependencies=[google_dot_protobuf_dot_struct__pb2.DESCRIPTOR,BlueprintCommon__pb2.DESCRIPTOR,])

_DOWNLOADACTION = _descriptor.EnumDescriptor(
  name='DownloadAction',
  full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.DownloadAction',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='SEARCH', index=0, number=0,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='STARTER', index=1, number=1,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='CLONE', index=2, number=2,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=1535,
  serialized_end=1587,
)
_sym_db.RegisterEnumDescriptor(_DOWNLOADACTION)

DownloadAction = enum_type_wrapper.EnumTypeWrapper(_DOWNLOADACTION)
_UPLOADACTION = _descriptor.EnumDescriptor(
  name='UploadAction',
  full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.UploadAction',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='DRAFT', index=0, number=0,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='ENRICH', index=1, number=1,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='VALIDATE', index=2, number=2,
      serialized_options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='PUBLISH', index=3, number=3,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=1589,
  serialized_end=1653,
)
_sym_db.RegisterEnumDescriptor(_UPLOADACTION)

UploadAction = enum_type_wrapper.EnumTypeWrapper(_UPLOADACTION)
_REMOVEACTION = _descriptor.EnumDescriptor(
  name='RemoveAction',
  full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.RemoveAction',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='DEFAULT', index=0, number=0,
      serialized_options=None,
      type=None),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=1655,
  serialized_end=1682,
)
_sym_db.RegisterEnumDescriptor(_REMOVEACTION)

RemoveAction = enum_type_wrapper.EnumTypeWrapper(_REMOVEACTION)
SEARCH = 0
STARTER = 1
CLONE = 2
DRAFT = 0
ENRICH = 1
VALIDATE = 2
PUBLISH = 3
DEFAULT = 0



_BLUEPRINTUPLOADINPUT = _descriptor.Descriptor(
  name='BlueprintUploadInput',
  full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintUploadInput',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='commonHeader', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintUploadInput.commonHeader', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='fileChunk', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintUploadInput.fileChunk', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='actionIdentifiers', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintUploadInput.actionIdentifiers', index=2,
      number=3, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='properties', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintUploadInput.properties', index=3,
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
  serialized_start=139,
  serialized_end=478,
)


_BLUEPRINTDOWNLOADINPUT = _descriptor.Descriptor(
  name='BlueprintDownloadInput',
  full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintDownloadInput',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='commonHeader', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintDownloadInput.commonHeader', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='actionIdentifiers', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintDownloadInput.actionIdentifiers', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='properties', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintDownloadInput.properties', index=2,
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
  serialized_start=481,
  serialized_end=736,
)


_BLUEPRINTREMOVEINPUT = _descriptor.Descriptor(
  name='BlueprintRemoveInput',
  full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintRemoveInput',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='commonHeader', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintRemoveInput.commonHeader', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='actionIdentifiers', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintRemoveInput.actionIdentifiers', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='properties', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintRemoveInput.properties', index=2,
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
  serialized_start=739,
  serialized_end=992,
)


_BLUEPRINTBOOTSTRAPINPUT = _descriptor.Descriptor(
  name='BlueprintBootstrapInput',
  full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintBootstrapInput',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='commonHeader', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintBootstrapInput.commonHeader', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='loadCBA', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintBootstrapInput.loadCBA', index=1,
      number=2, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='loadModelType', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintBootstrapInput.loadModelType', index=2,
      number=3, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='loadResourceDictionary', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintBootstrapInput.loadResourceDictionary', index=3,
      number=4, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
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
  serialized_start=995,
  serialized_end=1180,
)


_BLUEPRINTMANAGEMENTOUTPUT = _descriptor.Descriptor(
  name='BlueprintManagementOutput',
  full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementOutput',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='commonHeader', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementOutput.commonHeader', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='fileChunk', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementOutput.fileChunk', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='status', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementOutput.status', index=2,
      number=3, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='properties', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementOutput.properties', index=3,
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
  serialized_start=1183,
  serialized_end=1505,
)


_FILECHUNK = _descriptor.Descriptor(
  name='FileChunk',
  full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.FileChunk',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='chunk', full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.FileChunk.chunk', index=0,
      number=1, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value=_b(""),
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
  serialized_start=1507,
  serialized_end=1533,
)

_BLUEPRINTUPLOADINPUT.fields_by_name['commonHeader'].message_type = BlueprintCommon__pb2._COMMONHEADER
_BLUEPRINTUPLOADINPUT.fields_by_name['fileChunk'].message_type = _FILECHUNK
_BLUEPRINTUPLOADINPUT.fields_by_name['actionIdentifiers'].message_type = BlueprintCommon__pb2._ACTIONIDENTIFIERS
_BLUEPRINTUPLOADINPUT.fields_by_name['properties'].message_type = google_dot_protobuf_dot_struct__pb2._STRUCT
_BLUEPRINTDOWNLOADINPUT.fields_by_name['commonHeader'].message_type = BlueprintCommon__pb2._COMMONHEADER
_BLUEPRINTDOWNLOADINPUT.fields_by_name['actionIdentifiers'].message_type = BlueprintCommon__pb2._ACTIONIDENTIFIERS
_BLUEPRINTDOWNLOADINPUT.fields_by_name['properties'].message_type = google_dot_protobuf_dot_struct__pb2._STRUCT
_BLUEPRINTREMOVEINPUT.fields_by_name['commonHeader'].message_type = BlueprintCommon__pb2._COMMONHEADER
_BLUEPRINTREMOVEINPUT.fields_by_name['actionIdentifiers'].message_type = BlueprintCommon__pb2._ACTIONIDENTIFIERS
_BLUEPRINTREMOVEINPUT.fields_by_name['properties'].message_type = google_dot_protobuf_dot_struct__pb2._STRUCT
_BLUEPRINTBOOTSTRAPINPUT.fields_by_name['commonHeader'].message_type = BlueprintCommon__pb2._COMMONHEADER
_BLUEPRINTMANAGEMENTOUTPUT.fields_by_name['commonHeader'].message_type = BlueprintCommon__pb2._COMMONHEADER
_BLUEPRINTMANAGEMENTOUTPUT.fields_by_name['fileChunk'].message_type = _FILECHUNK
_BLUEPRINTMANAGEMENTOUTPUT.fields_by_name['status'].message_type = BlueprintCommon__pb2._STATUS
_BLUEPRINTMANAGEMENTOUTPUT.fields_by_name['properties'].message_type = google_dot_protobuf_dot_struct__pb2._STRUCT
DESCRIPTOR.message_types_by_name['BlueprintUploadInput'] = _BLUEPRINTUPLOADINPUT
DESCRIPTOR.message_types_by_name['BlueprintDownloadInput'] = _BLUEPRINTDOWNLOADINPUT
DESCRIPTOR.message_types_by_name['BlueprintRemoveInput'] = _BLUEPRINTREMOVEINPUT
DESCRIPTOR.message_types_by_name['BlueprintBootstrapInput'] = _BLUEPRINTBOOTSTRAPINPUT
DESCRIPTOR.message_types_by_name['BlueprintManagementOutput'] = _BLUEPRINTMANAGEMENTOUTPUT
DESCRIPTOR.message_types_by_name['FileChunk'] = _FILECHUNK
DESCRIPTOR.enum_types_by_name['DownloadAction'] = _DOWNLOADACTION
DESCRIPTOR.enum_types_by_name['UploadAction'] = _UPLOADACTION
DESCRIPTOR.enum_types_by_name['RemoveAction'] = _REMOVEACTION
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

BlueprintUploadInput = _reflection.GeneratedProtocolMessageType('BlueprintUploadInput', (_message.Message,), dict(
  DESCRIPTOR = _BLUEPRINTUPLOADINPUT,
  __module__ = 'BlueprintManagement_pb2'
  # @@protoc_insertion_point(class_scope:org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintUploadInput)
  ))
_sym_db.RegisterMessage(BlueprintUploadInput)

BlueprintDownloadInput = _reflection.GeneratedProtocolMessageType('BlueprintDownloadInput', (_message.Message,), dict(
  DESCRIPTOR = _BLUEPRINTDOWNLOADINPUT,
  __module__ = 'BlueprintManagement_pb2'
  # @@protoc_insertion_point(class_scope:org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintDownloadInput)
  ))
_sym_db.RegisterMessage(BlueprintDownloadInput)

BlueprintRemoveInput = _reflection.GeneratedProtocolMessageType('BlueprintRemoveInput', (_message.Message,), dict(
  DESCRIPTOR = _BLUEPRINTREMOVEINPUT,
  __module__ = 'BlueprintManagement_pb2'
  # @@protoc_insertion_point(class_scope:org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintRemoveInput)
  ))
_sym_db.RegisterMessage(BlueprintRemoveInput)

BlueprintBootstrapInput = _reflection.GeneratedProtocolMessageType('BlueprintBootstrapInput', (_message.Message,), dict(
  DESCRIPTOR = _BLUEPRINTBOOTSTRAPINPUT,
  __module__ = 'BlueprintManagement_pb2'
  # @@protoc_insertion_point(class_scope:org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintBootstrapInput)
  ))
_sym_db.RegisterMessage(BlueprintBootstrapInput)

BlueprintManagementOutput = _reflection.GeneratedProtocolMessageType('BlueprintManagementOutput', (_message.Message,), dict(
  DESCRIPTOR = _BLUEPRINTMANAGEMENTOUTPUT,
  __module__ = 'BlueprintManagement_pb2'
  # @@protoc_insertion_point(class_scope:org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementOutput)
  ))
_sym_db.RegisterMessage(BlueprintManagementOutput)

FileChunk = _reflection.GeneratedProtocolMessageType('FileChunk', (_message.Message,), dict(
  DESCRIPTOR = _FILECHUNK,
  __module__ = 'BlueprintManagement_pb2'
  # @@protoc_insertion_point(class_scope:org.onap.ccsdk.cds.controllerblueprints.management.api.FileChunk)
  ))
_sym_db.RegisterMessage(FileChunk)


DESCRIPTOR._options = None

_BLUEPRINTMANAGEMENTSERVICE = _descriptor.ServiceDescriptor(
  name='BlueprintManagementService',
  full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementService',
  file=DESCRIPTOR,
  index=0,
  serialized_options=None,
  serialized_start=1685,
  serialized_end=2447,
  methods=[
  _descriptor.MethodDescriptor(
    name='downloadBlueprint',
    full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementService.downloadBlueprint',
    index=0,
    containing_service=None,
    input_type=_BLUEPRINTDOWNLOADINPUT,
    output_type=_BLUEPRINTMANAGEMENTOUTPUT,
    serialized_options=None,
  ),
  _descriptor.MethodDescriptor(
    name='uploadBlueprint',
    full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementService.uploadBlueprint',
    index=1,
    containing_service=None,
    input_type=_BLUEPRINTUPLOADINPUT,
    output_type=_BLUEPRINTMANAGEMENTOUTPUT,
    serialized_options=None,
  ),
  _descriptor.MethodDescriptor(
    name='removeBlueprint',
    full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementService.removeBlueprint',
    index=2,
    containing_service=None,
    input_type=_BLUEPRINTREMOVEINPUT,
    output_type=_BLUEPRINTMANAGEMENTOUTPUT,
    serialized_options=None,
  ),
  _descriptor.MethodDescriptor(
    name='bootstrapBlueprint',
    full_name='org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementService.bootstrapBlueprint',
    index=3,
    containing_service=None,
    input_type=_BLUEPRINTBOOTSTRAPINPUT,
    output_type=_BLUEPRINTMANAGEMENTOUTPUT,
    serialized_options=None,
  ),
])
_sym_db.RegisterServiceDescriptor(_BLUEPRINTMANAGEMENTSERVICE)

DESCRIPTOR.services_by_name['BlueprintManagementService'] = _BLUEPRINTMANAGEMENTSERVICE

# @@protoc_insertion_point(module_scope)
