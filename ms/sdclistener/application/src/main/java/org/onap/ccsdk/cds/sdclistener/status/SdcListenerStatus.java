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

package org.onap.ccsdk.cds.sdclistener.status;

import org.onap.ccsdk.cds.sdclistener.dto.SdcListenerDto;
import org.onap.ccsdk.cds.sdclistener.util.BuilderUtil;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.consumer.IComponentDoneStatusMessage;
import org.onap.sdc.api.consumer.IDistributionStatusMessage;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.onap.sdc.utils.DistributionActionResultEnum.SUCCESS;

@Component
@ConfigurationProperties("listenerservice")
@ComponentScan("org.onap.ccsdk.cds.cdssdclistener.dto")
public class SdcListenerStatus {

    private static final Logger LOGGER = LoggerFactory.getLogger(SdcListenerStatus.class);
    private static final String COMPONENT_NAME = "cds";

    @Value("${listenerservice.config.consumerId}")
    private String consumerId;

    @Autowired
    private SdcListenerDto sdcListenerDto;


    public enum NotificationType {
        DOWNLOAD, SDC_LISTENER_COMPONENT;
    }

    /**
     * Send the component status back to SDC.
     *
     * @param distributionID SDC Distribution ID
     * @param status Distribution status
     * @param errorReason Reason of failure if present
     * @param url Artifact URL
     * @param type - NotificationType(Download or Component)
     */
    public void sendResponseBackToSdc(String distributionID, DistributionStatusEnum status, String errorReason,
            String url, NotificationType type) {
        final IDistributionClient distributionClient = sdcListenerDto.getDistributionClient();

        switch (type) {
            case SDC_LISTENER_COMPONENT:
                IComponentDoneStatusMessage componentStatusMessage =
                        buildStatusMessage(distributionID, status, url, COMPONENT_NAME);

                if (errorReason == null) {
                    checkResponseStatusFromSdc(distributionClient.sendComponentDoneStatus(componentStatusMessage));
                } else {
                    checkResponseStatusFromSdc(
                            distributionClient.sendComponentDoneStatus(componentStatusMessage, errorReason));
                }
                break;

            case DOWNLOAD:
                IDistributionStatusMessage downloadStatusMessage =
                        buildStatusMessage(distributionID, status, url, null);

                if (errorReason == null) {
                    checkResponseStatusFromSdc(distributionClient.sendDownloadStatus(downloadStatusMessage));
                } else {
                    checkResponseStatusFromSdc(
                            distributionClient.sendDownloadStatus(downloadStatusMessage, errorReason));
                }
            default:
                break;
        }
    }

    private ComponentStatusMessage buildStatusMessage(String distributionId, DistributionStatusEnum status, String url,
            String componentName) {
        return new BuilderUtil<>(new ComponentStatusMessage()).build(builder -> {
            builder.setDistributionID(distributionId);
            builder.setStatus(status);
            builder.setConsumerID(consumerId);
            builder.setComponentName(componentName);
            builder.setTimeStamp(System.currentTimeMillis());
            builder.setArtifactUrl(url);
        }).create();
    }

    private void checkResponseStatusFromSdc(IDistributionClientResult result) {
        if (!Objects.equals(result.getDistributionActionResult(), SUCCESS)) {
            LOGGER.error("SDC failed to receive the response from cds-sdc listener due to {}",
                    result.getDistributionMessageResult());
        } else {
            LOGGER.info("SDC successfully received the response");
        }
    }

}
