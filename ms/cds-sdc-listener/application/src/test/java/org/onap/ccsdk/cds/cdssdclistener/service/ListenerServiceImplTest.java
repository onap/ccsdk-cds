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
package org.onap.ccsdk.cds.cdssdclistener.service;

import static junit.framework.TestCase.assertTrue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.onap.ccsdk.cds.cdssdclistener.CdsSdcListenerConfiguration;
import org.onap.ccsdk.cds.cdssdclistener.client.CdsSdcListenerAuthClientInterceptor;
import org.onap.ccsdk.cds.cdssdclistener.dto.CdsSdcListenerDto;
import org.onap.ccsdk.cds.cdssdclistener.handler.BluePrintProcesssorHandler;
import org.onap.ccsdk.cds.cdssdclistener.status.CdsSdcListenerStatus;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.sdc.impl.mock.DistributionClientResultStubImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties({CdsSdcListenerAuthClientInterceptor.class,
    BluePrintProcesssorHandler.class, CdsSdcListenerDto.class, ListenerServiceImpl.class, CdsSdcListenerStatus.class,
    CdsSdcListenerConfiguration.class})
@SpringBootTest(classes = {ListenerServiceImplTest.class})
public class ListenerServiceImplTest {

    private static final String CSAR_SAMPLE = "src/test/resources/service-Testsvc140.csar";
    private static final String ZIP_FILE = ".zip";
    private static final String CSAR_FILE = ".csar";
    private String csarArchivePath;
    private Path tempDirectoryPath;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Autowired
    private ListenerServiceImpl listenerService;

    @Before
    public void setup() {
        csarArchivePath = folder.getRoot().toString();
        tempDirectoryPath = Paths.get(csarArchivePath, "cds-sdc-listener-test");
    }
    @Test
    public void extractBluePrintSuccessfully() throws IOException {
        // Act
        listenerService.extractBluePrint(CSAR_SAMPLE, tempDirectoryPath.toString());

        // Verify
        String result = checkFileExists(tempDirectoryPath);
        assertTrue(result.contains(ZIP_FILE));
    }

    @Test
    public void storeCsarArtifactToFileSuccessfully() throws  IOException {
        // Arrange
        DistributionClientDownloadResultStubImpl resultStub = new DistributionClientDownloadResultStubImpl();

        // Act
        listenerService.extractCsarAndStore(resultStub, tempDirectoryPath.toString());

        // Verify
        String result = checkFileExists(tempDirectoryPath);
        assertTrue(result.contains(CSAR_FILE));
    }

    private String checkFileExists(Path path) throws IOException {
        return Files.walk(path)
            .filter(Files::isRegularFile)
            .map(Path::toFile)
            .findAny()
            .get()
            .getName();
    }

    public byte[] convertFileToByteArray(File file) {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public class DistributionClientDownloadResultStubImpl extends DistributionClientResultStubImpl implements
        IDistributionClientDownloadResult {

        public DistributionClientDownloadResultStubImpl() {
        }

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
