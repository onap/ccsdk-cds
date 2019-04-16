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
package org.onap.ccsdk.cds.cdssdclistener.status;

import static org.onap.sdc.utils.DistributionActionResultEnum.SUCCESS;
import java.util.Objects;
import org.onap.ccsdk.cds.cdssdclistener.dto.CdsSdcListenerDto;
import org.onap.ccsdk.cds.cdssdclistener.util.BuilderUtil;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.consumer.IFinalDistrStatusMessage;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientImpl;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("listenerservice")
@ComponentScan("org.onap.ccsdk.cds.cdssdclistener.dto")
public class CdsSdcListenerStatus {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdsSdcListenerStatus.class);
    private static final String COMPONENT_NAME = "cds";

    @Value("${listenerservice.config.consumerId}")
    private String consumerId;

    @Autowired
    private CdsSdcListenerDto cdsSdcListenerDto;

    /**
     * Send the response back to SDC.
     *
     * @param distributionID SDC Distribution ID
     * @param status Distribution status
     * @param errorReason Reason of failure
     */
    public void sendResponseStatusBackToSDC(String distributionID, DistributionStatusEnum status, String errorReason) {

        final IDistributionClient distributionClient = cdsSdcListenerDto.getDistributionClient();

        IFinalDistrStatusMessage finalDistribution = new BuilderUtil<>(new DistributionStatusMessage())
            .build(builder -> {
                builder.distributionID = distributionID;
                builder.status = status;
                builder.consumerID = consumerId;
                builder.componentName = COMPONENT_NAME;
            }).create();

        if (errorReason == null) {
            checkResponseStatusFromSdc(distributionClient.sendFinalDistrStatus(finalDistribution));
        } else {
            checkResponseStatusFromSdc(distributionClient.sendFinalDistrStatus(finalDistribution, errorReason));
        }
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
