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

package org.onap.ccsdk.cds.blueprintsprocessor.grpc.service

import io.grpc.ManagedChannel
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.netty.GrpcSslContexts
import io.grpc.netty.NettyChannelBuilder
import io.netty.handler.ssl.SslContext
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TLSAuthGrpcClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.interceptor.GrpcClientLoggingInterceptor
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile

class TLSAuthGrpcClientService(private val tlsAuthGrpcClientProperties: TLSAuthGrpcClientProperties) :
    BlueprintGrpcClientService {

    override suspend fun channel(): ManagedChannel {

        val target =
            if (tlsAuthGrpcClientProperties.port == -1) tlsAuthGrpcClientProperties.host
            else "${tlsAuthGrpcClientProperties.host}:${tlsAuthGrpcClientProperties.port}"

        return NettyChannelBuilder
            .forTarget(target)
            .nameResolverFactory(DnsNameResolverProvider())
            .intercept(GrpcClientLoggingInterceptor())
            .sslContext(sslContext())
            .build()
    }

    fun sslContext(): SslContext {
        val builder = GrpcSslContexts.forClient()
        if (tlsAuthGrpcClientProperties.trustCertCollection != null) {
            builder.trustManager(normalizedFile(tlsAuthGrpcClientProperties.trustCertCollection!!))
        }
        if (tlsAuthGrpcClientProperties.clientCertChain != null &&
            tlsAuthGrpcClientProperties.clientPrivateKey != null
        ) {
            builder.keyManager(
                normalizedFile(tlsAuthGrpcClientProperties.clientCertChain!!),
                normalizedFile(tlsAuthGrpcClientProperties.clientPrivateKey!!)
            )
        }
        return builder.build()
    }
}
