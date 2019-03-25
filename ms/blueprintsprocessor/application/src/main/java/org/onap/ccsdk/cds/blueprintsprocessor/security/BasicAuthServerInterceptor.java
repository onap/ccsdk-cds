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
package org.onap.ccsdk.cds.blueprintsprocessor.security;

import com.google.common.base.Strings;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class BasicAuthServerInterceptor implements ServerInterceptor {

    private static Logger log = LoggerFactory.getLogger(BasicAuthServerInterceptor.class);

    @Autowired
    private AuthenticationManager authenticationManager;


    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next) {
        String authHeader = headers.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));

        if (Strings.isNullOrEmpty(authHeader)) {
            throw Status.UNAUTHENTICATED.withDescription("Missing required authentication").asRuntimeException();

        }

        try {
            String[] tokens = decodeBasicAuth(authHeader);
            String username = tokens[0];

            log.info("Basic Authentication Authorization header found for user: {}", username);

            Authentication authRequest = new UsernamePasswordAuthenticationToken(username, tokens[1]);
            Authentication authResult = authenticationManager.authenticate(authRequest).block();

            log.info("Authentication success: {}", authResult);

            SecurityContextHolder.getContext().setAuthentication(authResult);

        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();

            log.info("Authentication request failed: {}", e.getMessage());

            throw Status.UNAUTHENTICATED.withDescription(e.getMessage()).withCause(e).asRuntimeException();
        }

        return next.startCall(call, headers);
    }

    private String[] decodeBasicAuth(String authHeader) {
        String basicAuth;
        try {
            basicAuth = new String(Base64.getDecoder().decode(authHeader.substring(6).getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new BadCredentialsException("Failed to decode basic authentication token");
        }

        int delim = basicAuth.indexOf(':');
        if (delim == -1) {
            throw new BadCredentialsException("Failed to decode basic authentication token");
        }

        return new String[]{basicAuth.substring(0, delim), basicAuth.substring(delim + 1)};
    }
}