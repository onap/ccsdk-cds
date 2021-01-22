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

import io.grpc.netty.GrpcSslContexts
import io.grpc.netty.NettyServerBuilder
import io.netty.handler.ssl.ClientAuth
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TLSAuthGrpcServerProperties
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile

class TLSAuthGrpcServerService(private val tlsAuthGrpcServerProperties: TLSAuthGrpcServerProperties) :
    BlueprintGrpcServerService {

    override fun serverBuilder(): NettyServerBuilder {
        return NettyServerBuilder
            .forPort(tlsAuthGrpcServerProperties.port)
            .sslContext(sslContext())
    }

    fun sslContext(): SslContext {
        val sslClientContextBuilder = SslContextBuilder
            .forServer(
                normalizedFile(tlsAuthGrpcServerProperties.certChain),
                normalizedFile(tlsAuthGrpcServerProperties.privateKey)
            )

        tlsAuthGrpcServerProperties.trustCertCollection?.let { trustCertFile ->
            sslClientContextBuilder.trustManager(normalizedFile(trustCertFile))
            sslClientContextBuilder.clientAuth(ClientAuth.REQUIRE)
        }
        return GrpcSslContexts.configure(sslClientContextBuilder).build()
    }
}
