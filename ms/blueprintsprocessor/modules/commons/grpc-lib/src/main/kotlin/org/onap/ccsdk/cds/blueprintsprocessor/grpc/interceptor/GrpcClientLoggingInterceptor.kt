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

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.ForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.service.GrpcLoggerService
import org.onap.ccsdk.cds.controllerblueprints.core.logger

class GrpcClientLoggingInterceptor : ClientInterceptor {

    val log = logger(GrpcClientLoggingInterceptor::class)

    val loggingService = GrpcLoggerService()

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        channel: Channel
    ): ClientCall<ReqT, RespT> {

        return object : ForwardingClientCall
        .SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(method, callOptions)) {

            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                val listener =
                    object : ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                        override fun onMessage(message: RespT) {
                            loggingService.grpcInvoking(headers)
                            super.onMessage(message)
                        }
                    }
                super.start(listener, headers)
            }
        }
    }
}
