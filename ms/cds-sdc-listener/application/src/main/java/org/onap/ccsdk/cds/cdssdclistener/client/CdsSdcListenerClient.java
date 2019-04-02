/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package org.onap.ccsdk.cds.cdssdclistener.client;

import java.util.Optional;
import org.onap.ccsdk.cds.cdssdclistener.CdsSdcListenerConfiguration;
import org.onap.ccsdk.cds.cdssdclistener.CdsSdcListenerDto;
import org.onap.ccsdk.cds.cdssdclistener.CdsSdcListenerNotificationCallback;
import org.onap.ccsdk.cds.cdssdclistener.exceptions.CdsSdcListenerException;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientFactory;
import org.onap.sdc.utils.DistributionActionResultEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CdsSdcListenerClient {

    private static Logger LOG = LoggerFactory.getLogger(CdsSdcListenerClient.class);

    @Autowired
    private CdsSdcListenerConfiguration configuration;

    @Autowired
    private CdsSdcListenerNotificationCallback notification;

    @Autowired
    private CdsSdcListenerDto listenerDto;

    private IDistributionClient distributionClient;

    /**
     * This method initializes the SDC Distribution client.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initSdcClient() throws CdsSdcListenerException {
        LOG.info("Initialize the SDC distribution client");

        distributionClient = Optional.of(DistributionClientFactory.createDistributionClient())
            .orElseThrow(() -> new CdsSdcListenerException("Could not able to create SDC Distribution client"));

        listenerDto.setDistributionClient(distributionClient);

        IDistributionClientResult result = distributionClient.init(configuration, notification);

        startSdcClientBasedOnTheResult(result);
    }

    private void startSdcClientBasedOnTheResult(IDistributionClientResult result) throws CdsSdcListenerException {
        if (!result.getDistributionActionResult().equals(DistributionActionResultEnum.SUCCESS)) {
            throw new CdsSdcListenerException(
                "SDC distribution client init failed with reason:" + result.getDistributionMessageResult());
        }

        // Start the client.
        result = this.distributionClient.start();

        if (!result.getDistributionActionResult().equals(DistributionActionResultEnum.SUCCESS)) {
            throw new CdsSdcListenerException(
                "Startup of the SDC distribution client failed with reason: " + result.getDistributionMessageResult());
        }
    }

    private void closeSdcDistributionclient() throws CdsSdcListenerException {

        IDistributionClientResult status = this.distributionClient.stop();

        LOG.info("Closing SDC distribution client");
        if (status.getDistributionActionResult().equals(DistributionActionResultEnum.SUCCESS)) {
            throw new CdsSdcListenerException(
                "Failed to close the SDC distribution client due to : " + status.getDistributionMessageResult());
        }
    }
}
