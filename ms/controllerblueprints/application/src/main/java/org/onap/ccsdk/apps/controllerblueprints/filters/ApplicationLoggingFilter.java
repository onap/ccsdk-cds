/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *  Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.apps.controllerblueprints.filters;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * ApplicationLoggingFilter
 *
 * @author Brinda Santh 8/14/2018
 */
@Configuration
@SuppressWarnings("unused")
public class ApplicationLoggingFilter implements WebFilter {
    private static Logger log = LoggerFactory.getLogger(ApplicationLoggingFilter.class);

    @SuppressWarnings("unused")
    @Value("${appVersion}")
    private String appVersion;

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        try {

            ServerHttpRequest request = serverWebExchange.getRequest();
            ServerHttpResponse response = serverWebExchange.getResponse();

            String[] tokens = StringUtils.split(appVersion, '.');
            Preconditions.checkNotNull(tokens, "failed to split application versions");
            Preconditions.checkArgument(tokens.length == 3, "failed to tokenize application versions");
            HttpHeaders header = response.getHeaders();

            String requestID = defaultToUUID(request.getHeaders().getFirst("X-ONAP-RequestID"));
            String invocationID = defaultToUUID(request.getHeaders().getFirst("X-ONAP-InvocationID"));
            String partnerName = defaultToEmpty(request.getHeaders().getFirst("X-ONAP-PartnerName"));
            MDC.put("InvokeTimestamp", ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
            MDC.put("RequestID", requestID);
            MDC.put("InvocationID", invocationID);
            MDC.put("PartnerName", partnerName);
            MDC.put("ClientIPAddress", defaultToEmpty(request.getRemoteAddress().getAddress()));
            MDC.put("ServerFQDN", defaultToEmpty(request.getRemoteAddress().getHostString()));

            header.add(BluePrintConstants.RESPONSE_HEADER_TRANSACTION_ID, requestID);
            header.add(BluePrintConstants.RESPONSE_HEADER_MINOR_VERSION, tokens[1]);
            header.add(BluePrintConstants.RESPONSE_HEADER_PATCH_VERSION, tokens[2]);
            header.add(BluePrintConstants.RESPONSE_HEADER_LATEST_VERSION, appVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return webFilterChain.filter(serverWebExchange);

    }

    private static String defaultToUUID(String in) {
        return in == null ? UUID.randomUUID().toString() : in;
    }

    private static String defaultToEmpty(Object in) {
        return in == null ? "" : in.toString();
    }


}