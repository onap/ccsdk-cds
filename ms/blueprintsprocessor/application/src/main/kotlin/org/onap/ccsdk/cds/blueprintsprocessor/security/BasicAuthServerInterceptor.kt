/*
 * Copyright (C) 2019 Bell Canada.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.ccsdk.cds.blueprintsprocessor.security

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Base64

@Component
class BasicAuthServerInterceptor(private val authenticationManager: AuthenticationManager) :
    ServerInterceptor {

    private val log = logger(BasicAuthServerInterceptor::class)

    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val authHeader = headers.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER))

        if (authHeader.isNullOrEmpty()) {
            throw Status.UNAUTHENTICATED.withDescription("Missing required authentication")
                .asRuntimeException()
        }

        try {
            val tokens = decodeBasicAuth(authHeader)
            val username = tokens[0]

            log.info("Basic Authentication Authorization header found for user: {}", username)

            val authRequest = UsernamePasswordAuthenticationToken(username, tokens[1])
            val authResult = authenticationManager.authenticate(authRequest).block()

            log.info("Authentication success: {}", authResult)

            SecurityContextHolder.getContext().authentication = authResult
        } catch (e: AuthenticationException) {
            SecurityContextHolder.clearContext()

            log.info("Authentication request failed: {}", e.message)

            throw Status.UNAUTHENTICATED.withDescription(e.message).withCause(e).asRuntimeException()
        }

        return next.startCall(call, headers)
    }

    private fun decodeBasicAuth(authHeader: String): Array<String> {
        val basicAuth: String
        try {
            basicAuth = String(
                Base64.getDecoder().decode(authHeader.substring(6).toByteArray(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8
            )
        } catch (e: IllegalArgumentException) {
            throw BadCredentialsException("Failed to decode basic authentication token")
        } catch (e: IndexOutOfBoundsException) {
            throw BadCredentialsException("Failed to decode basic authentication token")
        }

        val delim = basicAuth.indexOf(':')
        if (delim == -1) {
            throw BadCredentialsException("Failed to decode basic authentication token")
        }

        return arrayOf(basicAuth.substring(0, delim), basicAuth.substring(delim + 1))
    }
}
