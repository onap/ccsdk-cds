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

package org.onap.ccsdk.cds.sdclistener.client;

import org.onap.ccsdk.cds.sdclistener.SdcListenerConfiguration;
import org.onap.ccsdk.cds.sdclistener.SdcListenerNotificationCallback;
import org.onap.ccsdk.cds.sdclistener.dto.SdcListenerDto;
import org.onap.ccsdk.cds.sdclistener.exceptions.SdcListenerException;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientFactory;
import org.onap.sdc.utils.DistributionActionResultEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ComponentScan("org.onap.ccsdk.cds.cdssdclistener.dto")
public class SdcListenerClient {

    private static Logger LOG = LoggerFactory.getLogger(SdcListenerClient.class);

    @Autowired
    private SdcListenerConfiguration configuration;

    @Autowired
    private SdcListenerNotificationCallback notification;

    @Autowired
    private SdcListenerDto listenerDto;

    private IDistributionClient distributionClient;

    /**
     * This method initializes the SDC Distribution client.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initSdcClient() throws SdcListenerException {
        LOG.info("Initialize the SDC distribution client");

        distributionClient = Optional.of(DistributionClientFactory.createDistributionClient())
                .orElseThrow(() -> new SdcListenerException("Could not able to create SDC Distribution client"));

        listenerDto.setManagedChannelForGrpc();

        listenerDto.setDistributionClient(distributionClient);

        IDistributionClientResult result = distributionClient.init(configuration, notification);
        startSdcClientBasedOnTheResult(result);
    }

    private void startSdcClientBasedOnTheResult(IDistributionClientResult result) throws SdcListenerException {
        if (!result.getDistributionActionResult().equals(DistributionActionResultEnum.SUCCESS)) {
            throw new SdcListenerException(
                    "SDC distribution client init failed with reason:" + result.getDistributionMessageResult());
        }

        LOG.info("Initialization of the SDC distribution client is complete");

        // Start the client.
        result = this.distributionClient.start();

        if (!result.getDistributionActionResult().equals(DistributionActionResultEnum.SUCCESS)) {
            throw new SdcListenerException("Startup of the SDC distribution client failed with reason: "
                    + result.getDistributionMessageResult());
        }
    }

    private void closeSdcDistributionclient() throws SdcListenerException {
        LOG.info("Closing SDC distribution client");
        IDistributionClientResult status = this.distributionClient.stop();
        if (status.getDistributionActionResult().equals(DistributionActionResultEnum.SUCCESS)) {
            throw new SdcListenerException(
                    "Failed to close the SDC distribution client due to : " + status.getDistributionMessageResult());
        }
    }

}
