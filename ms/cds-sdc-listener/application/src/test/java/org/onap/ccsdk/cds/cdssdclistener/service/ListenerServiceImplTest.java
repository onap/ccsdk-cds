/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package org.onap.ccsdk.cds.cdssdclistener.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(ListenerServiceImpl.class)
@SpringBootTest(classes = {ListenerServiceImplTest.class})
public class ListenerServiceImplTest {

    private static final String CSAR_SAMPLE = "src/test/resources/service-Testsvc140.csar";
    private Path tempDirectoryPath;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Autowired
    private ListenerServiceImpl listenerService;

    @Test
    public void extractBluePrintSuccessfully() {
        // Arrange
        tempDirectoryPath = Paths.get(folder.getRoot().toString(), "cds-sdc-listener-test");

        // Act
        listenerService.extractBluePrint(CSAR_SAMPLE, tempDirectoryPath.toString());
    }
}
