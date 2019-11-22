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

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.util.Base64

@Component
class SecurityContextRepository(private val authenticationManager: AuthenticationManager) :
    ServerSecurityContextRepository {

    override fun save(swe: ServerWebExchange, sc: SecurityContext): Mono<Void> {
        throw UnsupportedOperationException("Not supported.")
    }

    override fun load(swe: ServerWebExchange): Mono<SecurityContext> {
        val request = swe.request
        val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (authHeader != null && authHeader.startsWith("Basic")) {
            val tokens = decodeBasicAuth(authHeader)
            val username = tokens[0]
            val password = tokens[1]
            val auth = UsernamePasswordAuthenticationToken(username, password)
            return this.authenticationManager!!.authenticate(auth)
                .map { SecurityContextImpl(it) }
        } else {
            return Mono.empty()
        }
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
