"""Copyright 2019 Deutsche Telekom.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""
import socket
from datetime import datetime, timezone
from functools import wraps
from logging import Logger
from typing import NoReturn, Union

from grpc import ServicerContext
from manager.configuration import get_logger
from manager.errors import ArtifactManagerError, InvalidRequestError
from manager.utils import Repository, RepositoryStrategy
from onaplogging.mdcContext import MDC
from proto.BlueprintManagement_pb2 import (
    BlueprintDownloadInput,
    BlueprintManagementOutput,
    BlueprintRemoveInput,
    BlueprintUploadInput,
)
from proto.BlueprintManagement_pb2_grpc import BlueprintManagementServiceServicer

MDC_DATETIME_FORMAT = r"%Y-%m-%dT%H:%M:%S.%f%z"
COMMON_HEADER_DATETIME_FORMAT = r"%Y-%m-%dT%H:%M:%S.%fZ"


def fill_common_header(func):
    """Decorator to fill handler's output values which is the same type for each handler.

    It copies commonHeader from request object and set timestamp value.

    :param func: Handler function
    :return: _handler decorator callable object
    """

    @wraps(func)
    def _decorator(
        servicer: "ArtifactManagerServicer",
        request: Union[BlueprintDownloadInput, BlueprintRemoveInput, BlueprintUploadInput],
        context: ServicerContext,
    ) -> BlueprintManagementOutput:

        if not all([request.actionIdentifiers.blueprintName, request.actionIdentifiers.blueprintVersion]):
            raise InvalidRequestError("Request has to have set both Blueprint name and version")
        output: BlueprintManagementOutput = func(servicer, request, context)
        # Set same values for every handler
        output.commonHeader.CopyFrom(request.commonHeader)
        output.commonHeader.timestamp = datetime.utcnow().strftime(COMMON_HEADER_DATETIME_FORMAT)
        return output

    return _decorator


def translate_exception_to_response(func):
    """Decorator that translates Artifact Manager exceptions into proper responses.

    :param func: Handler function
    :return: _handler decorator callable object
    """

    @wraps(func)
    def _handler(
        servicer: "ArtifactManagerServicer",
        request: Union[BlueprintDownloadInput, BlueprintRemoveInput, BlueprintUploadInput],
        context: ServicerContext,
    ) -> BlueprintManagementOutput:
        try:
            output: BlueprintManagementOutput = func(servicer, request, context)
            output.status.code = 200
            output.status.message = "success"
        except ArtifactManagerError as error:
            # If ArtifactManagerError is raises one of defined error occurs.
            # Every ArtifactManagerError based exception has status_code paramenter
            # which has to be set in output. Use also exception's message to
            # set errorMessage of the output.
            output: BlueprintManagementOutput = BlueprintManagementOutput()
            output.status.code = error.status_code
            output.status.message = "failure"
            output.status.errorMessage = str(error.message)

            servicer.fill_MDC_timestamps()
            servicer.logger.error(
                "Error while processing the message - blueprintName={} blueprintVersion={}".format(
                    request.actionIdentifiers.blueprintName, request.actionIdentifiers.blueprintVersion
                ),
                extra={"mdc": MDC.result()},
            )
            MDC.clear()
        return output

    return _handler


def prepare_logging_context(func):
    """Decorator that prepares MDC logging context for logs inside the handler.

    :param func: Handler function
    :return: _handler decorator callable object
    """

    @wraps(func)
    def _decorator(
        servicer: "ArtifactManagerServicer",
        request: Union[BlueprintDownloadInput, BlueprintRemoveInput, BlueprintUploadInput],
        context: ServicerContext,
    ) -> BlueprintManagementOutput:
        MDC.put("RequestID", request.commonHeader.requestId)
        MDC.put("InvocationID", request.commonHeader.subRequestId)
        MDC.put("ServiceName", servicer.__class__.__name__)
        MDC.put("PartnerName", request.commonHeader.originatorId)
        started_at = datetime.utcnow().replace(tzinfo=timezone.utc)
        MDC.put("BeginTimestamp", started_at.strftime(MDC_DATETIME_FORMAT))

        # Adding processing_started_at to the servicer so later we'll have the data to calculate elapsed time.
        servicer.processing_started_at = started_at

        MDC.put("TargetEntity", "py-executor")
        MDC.put("TargetServiceName", func.__name__)
        MDC.put("Server", socket.getfqdn())

        output: BlueprintManagementOutput = func(servicer, request, context)
        MDC.clear()
        return output

    return _decorator


class ArtifactManagerServicer(BlueprintManagementServiceServicer):
    """ArtifactManagerServer class.

    Implements methods defined in proto files to manage artifacts repository.
    These methods are: download, upload and remove.
    """

    processing_started_at = None

    def __init__(self) -> NoReturn:
        """Instance of ArtifactManagerServer class initialization.

        Create logger for class using class name and set configuration property.
        """
        self.logger: Logger = get_logger(self.__class__.__name__)
        self.repository: Repository = RepositoryStrategy.get_reporitory()

    def fill_MDC_timestamps(self, status_code: int = 200) -> NoReturn:
        """Add MDC context timestamps "in place".

        :param status_code: int with expected response status. Default: 200 (success)
        """
        now = datetime.utcnow().replace(tzinfo=timezone.utc)
        MDC.put("EndTimestamp", now.strftime(MDC_DATETIME_FORMAT))

        # Elapsed time measured in miliseconds
        MDC.put("ElapsedTime", (now - self.processing_started_at).total_seconds() * 1000)

        MDC.put("StatusCode", status_code)

    @prepare_logging_context
    @translate_exception_to_response
    @fill_common_header
    def downloadBlueprint(self, request: BlueprintDownloadInput, context: ServicerContext) -> BlueprintManagementOutput:
        """Download blueprint file request method.

        Currently it only logs when is called and all base class method.
        :param request: BlueprintDownloadInput
        :param context: ServicerContext
        :return: BlueprintManagementOutput
        """
        output: BlueprintManagementOutput = BlueprintManagementOutput()
        output.fileChunk.chunk = self.repository.download_blueprint(
            request.actionIdentifiers.blueprintName, request.actionIdentifiers.blueprintVersion
        )
        self.fill_MDC_timestamps()
        self.logger.info(
            "Blueprint download successfuly processed - blueprintName={} blueprintVersion={}".format(
                request.actionIdentifiers.blueprintName, request.actionIdentifiers.blueprintVersion
            ),
            extra={"mdc": MDC.result()},
        )
        return output

    @prepare_logging_context
    @translate_exception_to_response
    @fill_common_header
    def uploadBlueprint(self, request: BlueprintUploadInput, context: ServicerContext) -> BlueprintManagementOutput:
        """Upload blueprint file request method.

        Currently it only logs when is called and all base class method.
        :param request: BlueprintUploadInput
        :param context: ServicerContext
        :return: BlueprintManagementOutput
        """
        self.repository.upload_blueprint(
            request.fileChunk.chunk, request.actionIdentifiers.blueprintName, request.actionIdentifiers.blueprintVersion
        )
        self.fill_MDC_timestamps()
        self.logger.info(
            "Blueprint upload successfuly processed - blueprintName={} blueprintVersion={}".format(
                request.actionIdentifiers.blueprintName, request.actionIdentifiers.blueprintVersion
            ),
            extra={"mdc": MDC.result()},
        )
        return BlueprintManagementOutput()

    @prepare_logging_context
    @translate_exception_to_response
    @fill_common_header
    def removeBlueprint(self, request: BlueprintRemoveInput, context: ServicerContext) -> BlueprintManagementOutput:
        """Remove blueprint file request method.

        Currently it only logs when is called and all base class method.
        :param request: BlueprintRemoveInput
        :param context: ServicerContext
        :return: BlueprintManagementOutput
        """
        self.repository.remove_blueprint(
            request.actionIdentifiers.blueprintName, request.actionIdentifiers.blueprintVersion
        )
        self.fill_MDC_timestamps()
        self.logger.info(
            "Blueprint removal successfuly processed - blueprintName={} blueprintVersion={}".format(
                request.actionIdentifiers.blueprintName, request.actionIdentifiers.blueprintVersion
            ),
            extra={"mdc": MDC.result()},
        )
        return BlueprintManagementOutput()
