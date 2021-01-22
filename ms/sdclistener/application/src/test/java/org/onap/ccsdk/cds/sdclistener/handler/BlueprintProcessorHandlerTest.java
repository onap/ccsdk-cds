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

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementOutput;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementServiceGrpc.BlueprintManagementServiceImplBase;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintUploadInput;
import org.onap.ccsdk.cds.controllerblueprints.management.api.FileChunk;
import org.onap.ccsdk.cds.controllerblueprints.management.api.UploadAction;
import org.onap.ccsdk.cds.sdclistener.client.SdcListenerAuthClientInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties({BlueprintProcesssorHandler.class, SdcListenerAuthClientInterceptor.class})
@SpringBootTest(classes = {BlueprintProcessorHandlerTest.class})
public class BlueprintProcessorHandlerTest {

    @Autowired
    private BlueprintProcesssorHandler bluePrintProcesssorHandler;

    @Autowired
    private SdcListenerAuthClientInterceptor sdcListenerAuthClientInterceptor;

    @Rule
    public GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private static final String CBA_ARCHIVE = "src/test/resources/testcba.zip";
    private static final String SUCCESS_MSG = "Successfully uploaded CBA";
    private static final int SUCCESS_CODE = 200;
    private ManagedChannel channel;

    @Before
    public void setUp() throws IOException {
        final BlueprintManagementServiceImplBase serviceImplBase = new BlueprintManagementServiceImplBase() {
            @Override
            public void uploadBlueprint(BlueprintUploadInput request,
                    StreamObserver<BlueprintManagementOutput> responseObserver) {
                responseObserver.onNext(getBlueprintManagementOutput());
                responseObserver.onCompleted();
            }
        };

        // Generate server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register.
        grpcCleanup.register(InProcessServerBuilder.forName(serverName).addService(serviceImplBase).directExecutor()
                .build().start());

        // Create a client channel.
        channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
    }

    @Test
    public void testApplicationEndPointSucess() throws IOException {
        // Arrange
        BlueprintUploadInput request = generateRequest();

        // Act
        Status output = bluePrintProcesssorHandler.sendRequest(request, channel);

        // Verify
        assertEquals(SUCCESS_CODE, output.getCode());
        assertTrue(output.getMessage().contains(SUCCESS_MSG));
    }

    private BlueprintUploadInput generateRequest() throws IOException {
        File file = Paths.get(CBA_ARCHIVE).toFile();
        byte[] bytes = FileUtils.readFileToByteArray(file);
        FileChunk fileChunk = FileChunk.newBuilder().setChunk(ByteString.copyFrom(bytes)).build();

        return BlueprintUploadInput.newBuilder()
                .setCommonHeader(CommonHeader.newBuilder().setRequestId(UUID.randomUUID().toString())
                        .setSubRequestId(UUID.randomUUID().toString()).setOriginatorId("SDC-LISTENER").build())
                .setActionIdentifiers(
                        ActionIdentifiers.newBuilder().setActionName(UploadAction.PUBLISH.toString()).build())
                .setFileChunk(fileChunk).build();
    }

    private BlueprintManagementOutput getBlueprintManagementOutput() {
        return BlueprintManagementOutput.newBuilder()
                .setStatus(Status.newBuilder().setMessage(SUCCESS_MSG).setCode(200).build()).build();
    }

}
