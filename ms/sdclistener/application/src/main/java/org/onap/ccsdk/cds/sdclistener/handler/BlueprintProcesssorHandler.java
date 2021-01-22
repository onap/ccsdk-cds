/*
 * Copyright © 2019 Bell Canada
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

package org.onap.ccsdk.cds.sdclistener.handler;

import io.grpc.ManagedChannel;
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementOutput;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementServiceGrpc;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementServiceGrpc.BlueprintManagementServiceBlockingStub;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintUploadInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("listenerservice")
@Component
public class BlueprintProcesssorHandler implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlueprintProcesssorHandler.class);

    private ManagedChannel channel;

    /**
     * Sending CBA archive to CDS backend to store into its Database.
     *
     * @param request BlueprintManagementInput object holds CBA archive, its version and blueprints.
     * @param managedChannel - ManagedChannel object helps to access the server or application end point.
     *
     * @return A response object
     */
    public Status sendRequest(BlueprintUploadInput request, ManagedChannel managedChannel) {
        LOGGER.info("Sending request to blueprint processor");

        this.channel = managedChannel;

        final BlueprintManagementServiceBlockingStub syncStub = BlueprintManagementServiceGrpc.newBlockingStub(channel);

        // Send the request to CDS backend.
        final BlueprintManagementOutput response = syncStub.uploadBlueprint(request);

        return response.getStatus();
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.shutdown();
        }
        LOGGER.info("Stopping GRPC connection to CDS backend");
    }

}
