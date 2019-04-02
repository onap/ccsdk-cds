/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package org.onap.ccsdk.cds.cdssdclistener.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.ccsdk.cds.cdssdclistener.client.CdsSdcListenerAuthClientInterceptor;
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintManagementOutput;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintManagementServiceGrpc.BluePrintManagementServiceImplBase;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintUploadInput;
import org.onap.ccsdk.cds.controllerblueprints.management.api.FileChunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@EnableConfigurationProperties({BluePrintProcesssorHandler.class, CdsSdcListenerAuthClientInterceptor.class})
@SpringBootTest(classes = {BluePrintProcessorHandlerTest.class})
public class BluePrintProcessorHandlerTest {

    @Autowired
    private BluePrintProcesssorHandler bluePrintProcesssorHandler;

    @Autowired
    private CdsSdcListenerAuthClientInterceptor cdsSdcListenerAuthClientInterceptor;

    @Rule
    public GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private static final String CBA_ARCHIVE = "src/test/resources/testcba.zip";
    private static final String SUCCESS_MSG = "Successfully uploaded CBA";
    private static final int SUCCESS_CODE = 200;
    private ManagedChannel channel;

    @Before
    public void setUp() throws IOException {
        final BluePrintManagementServiceImplBase serviceImplBase = new BluePrintManagementServiceImplBase() {
            @Override
            public void uploadBlueprint(BluePrintUploadInput request,
                StreamObserver<BluePrintManagementOutput> responseObserver) {
                responseObserver.onNext(getBluePrintManagementOutput());
                responseObserver.onCompleted();
            }
        };

        // Generate server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register.
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).addService(serviceImplBase).directExecutor().build().start());

        // Create a client channel.
        channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
    }

    @Test
    public void testApplicationEndPointSucess() throws IOException {
        // Arrange
        BluePrintUploadInput request = generateRequest();

        // Act
        Status output = bluePrintProcesssorHandler.sendRequest(request, channel);

        // Verify
        assertEquals(SUCCESS_CODE, output.getCode());
        assertTrue(output.getMessage().contains(SUCCESS_MSG));
    }

    private BluePrintUploadInput generateRequest() throws IOException {
        File file = Paths.get(CBA_ARCHIVE).toFile();
        byte[] bytes = FileUtils.readFileToByteArray(file);
        FileChunk fileChunk = FileChunk.newBuilder().setChunk(ByteString.copyFrom(bytes)).build();

        return BluePrintUploadInput.newBuilder().setFileChunk(fileChunk).build();
    }

    private BluePrintManagementOutput getBluePrintManagementOutput() {
        return BluePrintManagementOutput.newBuilder()
                .setStatus(Status.newBuilder().setMessage(SUCCESS_MSG).setCode(200).build())
                .build();
    }
}
