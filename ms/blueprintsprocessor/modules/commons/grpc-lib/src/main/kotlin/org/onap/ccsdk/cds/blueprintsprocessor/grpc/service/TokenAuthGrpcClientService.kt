/*
 *  Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.grpc.service

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.ManagedChannel
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.netty.NettyChannelBuilder
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TokenAuthGrpcClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.interceptor.GrpcClientLoggingInterceptor

class TokenAuthGrpcClientService(private val tokenAuthGrpcClientProperties: TokenAuthGrpcClientProperties) :
    BlueprintGrpcClientService {

    override suspend fun channel(): ManagedChannel {
        val target =
            if (tokenAuthGrpcClientProperties.port == -1) tokenAuthGrpcClientProperties.host
            else "${tokenAuthGrpcClientProperties.host}:${tokenAuthGrpcClientProperties.port}"

        val managedChannel = NettyChannelBuilder
            .forTarget(target)
            .nameResolverFactory(DnsNameResolverProvider())
            .intercept(GrpcClientLoggingInterceptor())
            .intercept(TokenAuthClientInterceptor(tokenAuthGrpcClientProperties)).usePlaintext().build()
        return managedChannel
    }
}

class TokenAuthClientInterceptor(private val tokenAuthGrpcClientProperties: TokenAuthGrpcClientProperties) :
    ClientInterceptor {

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        channel: Channel
    ): ClientCall<ReqT, RespT> {

        val authHeader = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)

        return object : ForwardingClientCall
        .SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(method, callOptions)) {

            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                headers.put(authHeader, tokenAuthGrpcClientProperties.token)
                super.start(responseListener, headers)
            }
        }
    }
}
