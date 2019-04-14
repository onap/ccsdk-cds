/*
 * Copyright © 2019 Bell Canada.
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
package org.onap.ccsdk.cds.blueprintsprocessor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.stereotype.Component;

// When GRPC enable, we need to manually create the netty server
@ConditionalOnProperty(name = "blueprintsprocessor.grpcEnable", havingValue = "true")
@Component
public class HttpServerWhenGrpcEnable {

    @Value("${blueprintsprocessor.httpPort}")
    private Integer httpPort;

    private final HttpHandler httpHandler;
    private WebServer http;

    @Autowired
    public HttpServerWhenGrpcEnable(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    @PostConstruct
    public void start() {
        ReactiveWebServerFactory factory = new NettyReactiveWebServerFactory(httpPort);
        this.http = factory.getWebServer(this.httpHandler);
        this.http.start();
    }

    @PreDestroy
    public void stop() {
        this.http.stop();
    }
}