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

package org.onap.ccsdk.cds.sdclistener.service;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintUploadInput;
import org.onap.ccsdk.cds.sdclistener.SdcListenerConfiguration;
import org.onap.ccsdk.cds.sdclistener.client.SdcListenerAuthClientInterceptor;
import org.onap.ccsdk.cds.sdclistener.dto.SdcListenerDto;
import org.onap.ccsdk.cds.sdclistener.handler.BluePrintProcesssorHandler;
import org.onap.ccsdk.cds.sdclistener.status.SdcListenerStatus;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.sdc.api.results.DistributionActionResultEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertTrue;
import static org.onap.ccsdk.cds.sdclistener.status.SdcListenerStatus.NotificationType.DEPLOY;
import static org.onap.ccsdk.cds.sdclistener.status.SdcListenerStatus.NotificationType.SDC_LISTENER_COMPONENT;
import static org.onap.sdc.api.notification.DistributionStatusEnum.COMPONENT_DONE_ERROR;
import static org.onap.sdc.api.notification.DistributionStatusEnum.COMPONENT_DONE_OK;
import static org.onap.sdc.api.notification.DistributionStatusEnum.DEPLOY_ERROR;
import static org.onap.sdc.api.notification.DistributionStatusEnum.DEPLOY_OK;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties({SdcListenerAuthClientInterceptor.class, BluePrintProcesssorHandler.class,
        SdcListenerDto.class, ListenerServiceImpl.class, SdcListenerStatus.class, SdcListenerConfiguration.class})
@SpringBootTest(classes = {ListenerServiceImplTest.class})
public class ListenerServiceImplTest {

    private static final String CSAR_SAMPLE = "src/test/resources/service-ServicePnfTest-csar.csar";
    private static final String WRONG_CSAR_SAMPLE = "src/test/resources/wrong_csar_pattern.csar";
    private static final String CBA_ZIP_PATH =
            "Artifacts/[a-zA-Z0-9-_.]+/Deployment/CONTROLLER_BLUEPRINT_ARCHIVE/[a-zA-Z0-9-_.()]+[.]zip";
    private static final String ZIP_FILE = ".zip";
    private static final String CSAR_FILE = ".csar";
    private static final String DISTRIBUTION_ID = "1";
    private static final String URL = "/sdc/v1/artifact";

    private String csarArchivePath;
    private Path tempDirectoryPath;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Autowired
    private ListenerServiceImpl listenerService;

    @MockBean
    SdcListenerStatus status;

    @MockBean
    SdcListenerDto listenerDto;

    @MockBean
    BluePrintProcesssorHandler bluePrintProcesssorHandler;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        csarArchivePath = folder.getRoot().toString();
        tempDirectoryPath = Paths.get(csarArchivePath, "cds-sdc-listener-test");
    }

    @Test
    public void extractBluePrintSuccessfully() throws IOException {
        // Act
        listenerService.extractBluePrint(CSAR_SAMPLE, tempDirectoryPath.toString());

        // Verify.
        String result = checkFileExists(tempDirectoryPath);
        assertTrue(result.contains(ZIP_FILE));
    }

    @Test
    public void extractBluePrintFailure() {
        // Arrange
        Mockito.when(listenerDto.getDistributionId()).thenReturn(DISTRIBUTION_ID);
        Mockito.when(listenerDto.getArtifactUrl()).thenReturn(URL);
        Mockito.doCallRealMethod().when(status).sendResponseBackToSdc(DISTRIBUTION_ID, COMPONENT_DONE_OK, null, URL,
                SDC_LISTENER_COMPONENT);

        // Act
        listenerService.extractBluePrint(WRONG_CSAR_SAMPLE, tempDirectoryPath.toString());

        // Verify
        Mockito.verify(status).sendResponseBackToSdc(DISTRIBUTION_ID, COMPONENT_DONE_OK, null, URL,
                SDC_LISTENER_COMPONENT);
    }

    @Test
    public void storeCsarArtifactToFileSuccessfully() throws IOException {
        // Arrange
        DistributionClientDownloadResultStubImpl resultStub = new DistributionClientDownloadResultStubImpl();

        // Act
        listenerService.extractCsarAndStore(resultStub, tempDirectoryPath);

        // Verify
        String result = checkFileExists(tempDirectoryPath);
        assertTrue(result.contains(CSAR_FILE));
    }

    @Test
    public void storeCsarArtifactFailureIsReportedAsComponentDoneError() throws IOException {
        // Arrange
        Mockito.when(listenerDto.getDistributionId()).thenReturn(DISTRIBUTION_ID);
        Mockito.when(listenerDto.getArtifactUrl()).thenReturn(URL);
        Path notADirectory = folder.newFile("not-a-directory").toPath();

        // Act
        listenerService.extractCsarAndStore(new DistributionClientDownloadResultStubImpl(), notADirectory);

        // Verify
        Mockito.verify(status).sendResponseBackToSdc(Mockito.eq(DISTRIBUTION_ID), Mockito.eq(COMPONENT_DONE_ERROR),
                ArgumentMatchers.startsWith("Failed to store the CSAR"), Mockito.eq(URL),
                Mockito.eq(SDC_LISTENER_COMPONENT));
    }

    @Test
    public void storedBluePrintIsReportedAsDeployOkBeforeComponentDone() throws IOException {
        // Arrange
        Mockito.when(listenerDto.getDistributionId()).thenReturn(DISTRIBUTION_ID);
        Mockito.when(listenerDto.getArtifactUrl()).thenReturn(URL);
        Mockito.when(bluePrintProcesssorHandler.sendRequest(Mockito.any(BluePrintUploadInput.class), Mockito.any()))
                .thenReturn(Status.newBuilder().setCode(200).build());

        // Act
        listenerService.saveBluePrintToCdsDatabase(cbaArchiveWithOneBluePrint(), null);

        // Verify
        InOrder inOrder = Mockito.inOrder(status);
        inOrder.verify(status).sendResponseBackToSdc(DISTRIBUTION_ID, DEPLOY_OK, null, URL, DEPLOY);
        inOrder.verify(status).sendResponseBackToSdc(DISTRIBUTION_ID, COMPONENT_DONE_OK, null, URL,
                SDC_LISTENER_COMPONENT);
    }

    @Test
    public void rejectedBluePrintIsReportedAsDeployErrorBeforeComponentDone() throws IOException {
        // Arrange
        final String errorMessage = "Failed to store the CBA archive into CDS DB due to invalid blueprint";
        Mockito.when(listenerDto.getDistributionId()).thenReturn(DISTRIBUTION_ID);
        Mockito.when(listenerDto.getArtifactUrl()).thenReturn(URL);
        Mockito.when(bluePrintProcesssorHandler.sendRequest(Mockito.any(BluePrintUploadInput.class), Mockito.any()))
                .thenReturn(Status.newBuilder().setCode(500).setErrorMessage("invalid blueprint").build());

        // Act
        listenerService.saveBluePrintToCdsDatabase(cbaArchiveWithOneBluePrint(), null);

        // Verify
        InOrder inOrder = Mockito.inOrder(status);
        inOrder.verify(status).sendResponseBackToSdc(DISTRIBUTION_ID, DEPLOY_ERROR, errorMessage, URL, DEPLOY);
        inOrder.verify(status).sendResponseBackToSdc(DISTRIBUTION_ID, COMPONENT_DONE_ERROR, errorMessage, URL,
                SDC_LISTENER_COMPONENT);
    }

    private Path cbaArchiveWithOneBluePrint() throws IOException {
        Path cbaArchive = folder.newFolder("cba-archive").toPath();
        Files.copy(Paths.get("src/test/resources/testcba.zip"), cbaArchive.resolve("testcba.zip"));
        return cbaArchive;
    }

    private String checkFileExists(Path path) throws IOException {
        return Files.walk(path).filter(Files::isRegularFile).map(Path::toFile).findAny().get().getName();
    }

    public byte[] convertFileToByteArray(File file) {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public class DistributionClientDownloadResultStubImpl implements IDistributionClientDownloadResult {

        @Override
        public DistributionActionResultEnum getDistributionActionResult() {
            return DistributionActionResultEnum.SUCCESS;
        }

        @Override
        public String getDistributionMessageResult() {
            return "Stub Result, method not implemented!";
        }

        public DistributionClientDownloadResultStubImpl() {}

        public byte[] getArtifactPayload() {
            File file = Paths.get(CSAR_SAMPLE).toFile();
            return convertFileToByteArray(file);
        }

        public String getArtifactName() {
            return "MackArtifactName";
        }

        public String getArtifactFilename() {
            return "MackArtifactName.csar";
        }

    }

}
