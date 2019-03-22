/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.controllerblueprints.security;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

@SuppressWarnings("unused")
@EnableWebFluxSecurity
public class ApplicationSecurityConfigurerAdapter {

    @Value("${basic-auth.user-name}")
    private String userName;

    @Value("${basic-auth.hashed-pwd}")
    private String userHashedPassword;

    private static EELFLogger log = EELFManager.getInstance().getLogger(ApplicationSecurityConfigurerAdapter.class);

    @Bean
    public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) throws Exception {

        http.csrf().disable();
        http.authorizeExchange()
                .pathMatchers("/webjars/**", "/actuator/**").permitAll()
                .anyExchange().authenticated()
                .and().httpBasic();

        return http.build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        User.UserBuilder userBuilder = User.builder();
        UserDetails defaultUser = userBuilder
                .username(userName)
                .password(userHashedPassword).roles("USER").build();
        return new MapReactiveUserDetailsService(defaultUser);
    }
}