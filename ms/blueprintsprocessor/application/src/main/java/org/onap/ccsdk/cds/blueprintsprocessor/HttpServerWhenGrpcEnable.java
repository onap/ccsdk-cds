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