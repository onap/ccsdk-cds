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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.onap.ccsdk.cds.sdclistener.SdcListenerConfiguration;
import org.onap.ccsdk.cds.sdclistener.client.SdcListenerAuthClientInterceptor;
import org.onap.ccsdk.cds.sdclistener.dto.SdcListenerDto;
import org.onap.ccsdk.cds.sdclistener.handler.BlueprintProcesssorHandler;
import org.onap.ccsdk.cds.sdclistener.status.SdcListenerStatus;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.sdc.impl.mock.DistributionClientResultStubImpl;
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
import static org.onap.ccsdk.cds.sdclistener.status.SdcListenerStatus.NotificationType.SDC_LISTENER_COMPONENT;
import static org.onap.sdc.utils.DistributionStatusEnum.COMPONENT_DONE_OK;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties({SdcListenerAuthClientInterceptor.class, BlueprintProcesssorHandler.class,
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

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        csarArchivePath = folder.getRoot().toString();
        tempDirectoryPath = Paths.get(csarArchivePath, "cds-sdc-listener-test");
    }

    @Test
    public void extractBlueprintSuccessfully() throws IOException {
        // Act
        listenerService.extractBlueprint(CSAR_SAMPLE, tempDirectoryPath.toString());

        // Verify.
        String result = checkFileExists(tempDirectoryPath);
        assertTrue(result.contains(ZIP_FILE));
    }

    @Test
    public void extractBlueprintFailure() {
        // Arrange
        Mockito.when(listenerDto.getDistributionId()).thenReturn(DISTRIBUTION_ID);
        Mockito.when(listenerDto.getArtifactUrl()).thenReturn(URL);
        Mockito.doCallRealMethod().when(status).sendResponseBackToSdc(DISTRIBUTION_ID, COMPONENT_DONE_OK, null, URL,
                SDC_LISTENER_COMPONENT);

        // Act
        listenerService.extractBlueprint(WRONG_CSAR_SAMPLE, tempDirectoryPath.toString());

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

    public class DistributionClientDownloadResultStubImpl extends DistributionClientResultStubImpl
            implements IDistributionClientDownloadResult {

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
