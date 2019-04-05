/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package org.onap.ccsdk.cds.cdssdclistener.service;

import static junit.framework.TestCase.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.onap.ccsdk.cds.cdssdclistener.client.CdsSdcListenerAuthClientInterceptor;
import org.onap.ccsdk.cds.cdssdclistener.handler.BluePrintProcesssorHandler;
import org.onap.sdc.impl.mock.DistributionClientDownloadResultStubImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties({ListenerServiceImpl.class, CdsSdcListenerAuthClientInterceptor.class,
    BluePrintProcesssorHandler.class})
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
}
