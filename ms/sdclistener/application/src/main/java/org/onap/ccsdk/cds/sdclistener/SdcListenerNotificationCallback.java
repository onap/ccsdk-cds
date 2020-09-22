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

package org.onap.ccsdk.cds.sdclistener;

import org.onap.ccsdk.cds.sdclistener.dto.SdcListenerDto;
import org.onap.ccsdk.cds.sdclistener.service.ListenerService;
import org.onap.ccsdk.cds.sdclistener.status.SdcListenerStatus;
import org.onap.ccsdk.cds.sdclistener.status.SdcListenerStatus.NotificationType;
import org.onap.ccsdk.cds.sdclistener.util.FileUtil;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.consumer.INotificationCallback;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.onap.sdc.utils.DistributionActionResultEnum.SUCCESS;

@ConfigurationProperties("listenerservice")
@Component
@ComponentScan("org.onap.ccsdk.cds.cdssdclistener.dto")
public class SdcListenerNotificationCallback implements INotificationCallback {

    @Autowired
    private SdcListenerDto sdcListenerDto;

    @Autowired
    private ListenerService listenerService;

    @Value("${listenerservice.config.archivePath}")
    private String pathToStoreArchives;

    @Autowired
    private SdcListenerStatus listenerStatus;

    private static final Logger LOGGER = LoggerFactory.getLogger(SdcListenerNotificationCallback.class);

    @Override
    public void activateCallback(INotificationData notificationData) {
        final String distributionId = notificationData.getDistributionID();
        sdcListenerDto.setDistributionId(distributionId);
        LOGGER.info("Received service distribution from SDC with the id {}", distributionId);
        processNotification(notificationData);
    }

    private void processNotification(INotificationData notificationData) {
        final IDistributionClient distributionClient = sdcListenerDto.getDistributionClient();
        notificationData.getServiceArtifacts()
                .forEach(artifactInfo -> downloadCsarArtifacts(artifactInfo, distributionClient));
    }

    /**
     * Download the TOSCA CSAR artifact and process it.
     *
     * @param info - Artifact information
     * @param distributionClient - SDC distribution client
     */
    private void downloadCsarArtifacts(IArtifactInfo info, IDistributionClient distributionClient) {
        final String url = info.getArtifactURL();
        sdcListenerDto.setArtifactUrl(url);
        final String id = info.getArtifactUUID();
        final String distributionId = sdcListenerDto.getDistributionId();

        if (Objects.equals(info.getArtifactType(), SdcListenerConfiguration.TOSCA_CSAR)) {
            LOGGER.info("Trying to download the artifact from : {} and UUID is {} ", url, id);

            // Download the artifact
            IDistributionClientDownloadResult result = distributionClient.download(info);

            if (!Objects.equals(result.getDistributionActionResult(), SUCCESS)) {
                final String errorMessage = String.format("Failed to download the artifact from : %s due to %s ", url,
                        result.getDistributionActionResult());
                listenerStatus.sendResponseBackToSdc(distributionId, DistributionStatusEnum.DOWNLOAD_ERROR,
                        errorMessage, url, NotificationType.DOWNLOAD);
                LOGGER.error(errorMessage);
            } else {
                listenerStatus.sendResponseBackToSdc(distributionId, DistributionStatusEnum.DOWNLOAD_OK, null, url,
                        NotificationType.DOWNLOAD);
                LOGGER.info("Trying to write CSAR artifact to file  with URL {} and UUID {}", url, id);
                processCsarArtifact(result);
            }
        }
    }

    private void processCsarArtifact(IDistributionClientDownloadResult result) {
        Path cbaArchivePath = Paths.get(pathToStoreArchives, "cba-archive");
        Path csarArchivePath = Paths.get(pathToStoreArchives, "csar-archive");

        // Extract and store the CSAR archive into local disk.
        listenerService.extractCsarAndStore(result, csarArchivePath);

        List<File> csarFiles = FileUtil.getFilesFromDisk(csarArchivePath);

        if (!csarFiles.isEmpty()) {
            final String archivePath = cbaArchivePath.toString();

            // Extract CBA archive from CSAR package and store it into local disk
            csarFiles.forEach(file -> listenerService.extractBluePrint(file.getAbsolutePath(), archivePath));
            csarFiles.forEach(file -> FileUtil.deleteFile(file, csarArchivePath.toString()));
        } else {
            LOGGER.error("Could not able to read CSAR files from this location {}", csarArchivePath);
        }

        listenerService.saveBluePrintToCdsDatabase(cbaArchivePath, sdcListenerDto.getManagedChannelForGrpc());
    }

}
