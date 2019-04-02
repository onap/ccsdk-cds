/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package org.onap.ccsdk.cds.cdssdclistener.client;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * This job of this class is to provide authentication with GRPC server.
 */
@ConfigurationProperties("listenerservice")
@Component
public class CdsSdcListenerAuthClientInterceptor implements ClientInterceptor {

    @Value("${listenerservice.config.authHeader}")
    private String basicAuth;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
                                                               CallOptions callOptions, Channel channel) {
        Key<String> authHeader = Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(authHeader, basicAuth);
                super.start(responseListener, headers);
            }
        };
    }
}
