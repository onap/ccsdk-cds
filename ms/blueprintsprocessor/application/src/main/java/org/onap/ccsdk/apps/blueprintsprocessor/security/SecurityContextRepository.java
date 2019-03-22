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
package org.onap.ccsdk.apps.blueprintsprocessor.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange swe, SecurityContext sc) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange swe) {
        ServerHttpRequest request = swe.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Basic")) {
            String[] tokens = decodeBasicAuth(authHeader);
            String username = tokens[0];
            String password = tokens[1];
            Authentication auth = new UsernamePasswordAuthenticationToken(username, password);
            return this.authenticationManager.authenticate(auth).map(SecurityContextImpl::new);
        } else {
            return Mono.empty();
        }
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