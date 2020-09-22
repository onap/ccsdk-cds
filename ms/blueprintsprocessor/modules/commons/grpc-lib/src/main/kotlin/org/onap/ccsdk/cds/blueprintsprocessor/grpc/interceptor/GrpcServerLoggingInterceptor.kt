/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.grpc.interceptor

import io.grpc.ForwardingServerCall
import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.service.GrpcLoggerService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintDownloadInput
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintRemoveInput
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintUploadInput
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput
import org.slf4j.MDC

class GrpcServerLoggingInterceptor : ServerInterceptor {
    val log = logger(GrpcServerLoggingInterceptor::class)
    val loggingService = GrpcLoggerService()

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        requestHeaders: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ):
        ServerCall.Listener<ReqT> {

            val forwardingServerCall = object : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                override fun sendHeaders(responseHeaders: Metadata) {
                    loggingService.grpResponding(requestHeaders, responseHeaders)
                    super.sendHeaders(responseHeaders)
                }
            }

            return object :
                ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                    next.startCall(forwardingServerCall, requestHeaders)
                ) {

                override fun onMessage(message: ReqT) {
                    /** Get the requestId, SubRequestId and Originator Id and set in MDS context
                     *  If you are using other GRPC services, Implement own Logging Interceptors to get tracing.
                     * */
                    when (message) {
                        is ExecutionServiceInput -> {
                            val commonHeader = message.commonHeader
                                ?: throw BluePrintProcessorException("missing common header in request")
                            loggingService.grpcRequesting(call, commonHeader, next)
                        }
                        is BluePrintUploadInput -> {
                            val commonHeader = message.commonHeader
                                ?: throw BluePrintProcessorException("missing common header in request")
                            loggingService.grpcRequesting(call, commonHeader, next)
                        }
                        is BluePrintDownloadInput -> {
                            val commonHeader = message.commonHeader
                                ?: throw BluePrintProcessorException("missing common header in request")
                            loggingService.grpcRequesting(call, commonHeader, next)
                        }
                        is BluePrintRemoveInput -> {
                            val commonHeader = message.commonHeader
                                ?: throw BluePrintProcessorException("missing common header in request")
                            loggingService.grpcRequesting(call, commonHeader, next)
                        }
                        else -> {
                            loggingService.grpcRequesting(call, requestHeaders, next)
                        }
                    }
                    super.onMessage(message)
                }

                override fun onComplete() {
                    MDC.clear()
                    super.onComplete()
                }

                override fun onCancel() {
                    MDC.clear()
                    super.onCancel()
                }
            }
        }
}
