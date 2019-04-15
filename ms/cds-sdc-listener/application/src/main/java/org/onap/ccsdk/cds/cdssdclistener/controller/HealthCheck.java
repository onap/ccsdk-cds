package org.onap.ccsdk.cds.cdssdclistener.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/sdclistener")
public class HealthCheck {

    @RequestMapping(path = "/ping", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Mono<String> ping() {
        return Mono.just("{\"status\":\"UP\"}");
    }
}
