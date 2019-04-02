/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package org.onap.ccsdk.cds.cdssdclistener.handler;

import io.grpc.ManagedChannel;
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintManagementOutput;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintManagementServiceGrpc;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintManagementServiceGrpc.BluePrintManagementServiceBlockingStub;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintUploadInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("listenerservice")
@Component
public class BluePrintProcesssorHandler implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BluePrintProcesssorHandler.class);

    private ManagedChannel channel;

    /**
     *  Sending CBA archive to CDS backend to store into its Database.
     * @param request BluePrintManagementInput object holds CBA archive, its version and blueprints.
     * @param managedChannel - ManagedChannel object helps to access the server or application end point.
     * @return A response object
     */
    public Status sendRequest(BluePrintUploadInput request, ManagedChannel managedChannel) {
        LOGGER.info("Sending request to blueprint processor");

        this.channel = managedChannel;

        final BluePrintManagementServiceBlockingStub syncStub = BluePrintManagementServiceGrpc.newBlockingStub(channel);

        // Send the request to CDS backend.
        final BluePrintManagementOutput response = syncStub.uploadBlueprint(request);

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
