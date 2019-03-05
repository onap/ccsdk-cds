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

package org.onap.ccsdk.apps.blueprintsprocessor;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.onap.ccsdk.apps.blueprintsprocessor.security.BasicAuthServerInterceptor;
import org.onap.ccsdk.apps.blueprintsprocessor.selfservice.api.BluePrintManagementGRPCHandler;
import org.onap.ccsdk.apps.blueprintsprocessor.selfservice.api.BluePrintProcessingGRPCHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "blueprintsprocessor.grpcEnable", havingValue = "true")
@Component
public class BlueprintGRPCServer implements ApplicationListener<ContextRefreshedEvent> {

    private static Logger log = LoggerFactory.getLogger(BlueprintGRPCServer.class);

    @Autowired
    private BluePrintProcessingGRPCHandler bluePrintProcessingGRPCHandler;
    @Autowired
    private BluePrintManagementGRPCHandler bluePrintManagementGRPCHandler;
    @Autowired
    private BasicAuthServerInterceptor authInterceptor;

    @Value("${blueprintsprocessor.grpcPort}")
    private Integer grpcPort;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            log.info("Starting Blueprint Processor GRPC Starting..");
            Server server = ServerBuilder
                .forPort(grpcPort)
                .intercept(authInterceptor)
                .addService(bluePrintProcessingGRPCHandler)
                .addService(bluePrintManagementGRPCHandler)
                .build();

            server.start();
            log.info("Blueprint Processor GRPC server started and ready to serve on port({})...", server.getPort());
            server.awaitTermination();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
