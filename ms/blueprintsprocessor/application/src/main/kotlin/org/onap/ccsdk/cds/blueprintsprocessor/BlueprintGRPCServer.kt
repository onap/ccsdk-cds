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

package org.onap.ccsdk.cds.blueprintsprocessor

import io.grpc.ServerBuilder
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.BlueprintManagementGRPCHandler
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.interceptor.GrpcServerLoggingInterceptor
import org.onap.ccsdk.cds.blueprintsprocessor.security.BasicAuthServerInterceptor
import org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.BlueprintProcessingGRPCHandler
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@ConditionalOnProperty(name = ["blueprintsprocessor.grpcEnable"], havingValue = "true")
@Component
open class BlueprintGRPCServer(
    private val bluePrintProcessingGRPCHandler: BlueprintProcessingGRPCHandler,
    private val bluePrintManagementGRPCHandler: BlueprintManagementGRPCHandler,
    private val authInterceptor: BasicAuthServerInterceptor
) :
    ApplicationListener<ContextRefreshedEvent> {

    private val log = logger(BlueprintGRPCServer::class)

    @Value("\${blueprintsprocessor.grpcPort}")
    private val grpcPort: Int? = null

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        try {
            log.info("Starting Blueprint Processor GRPC Starting..")
            val server = ServerBuilder
                .forPort(grpcPort!!)
                .intercept(GrpcServerLoggingInterceptor())
                .intercept(authInterceptor)
                .addService(bluePrintProcessingGRPCHandler)
                .addService(bluePrintManagementGRPCHandler)
                .build()

            server.start()
            log.info("Blueprint Processor GRPC server started and ready to serve on port({})...", server.port)
        } catch (e: Exception) {
            log.error("*** Error ***", e)
        }
    }
}
