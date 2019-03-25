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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import reactor.core.publisher.Mono;

@Configuration
public class AuthenticationManager implements ReactiveAuthenticationManager {

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        try {
            return Mono.just(authenticationProvider.authenticate(authentication));
        } catch (AuthenticationException e) {
            return Mono.error(e);
        }
    }
}