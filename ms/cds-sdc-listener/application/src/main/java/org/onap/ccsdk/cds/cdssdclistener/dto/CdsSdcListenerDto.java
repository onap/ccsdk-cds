/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package org.onap.ccsdk.cds.cdssdclistener.dto;

import org.onap.sdc.api.IDistributionClient;
import org.springframework.stereotype.Component;

@Component
public class CdsSdcListenerDto {

    private IDistributionClient distributionClient;

    public IDistributionClient getDistributionClient() {
        return distributionClient;
    }

    public void setDistributionClient(IDistributionClient distributionClient) {
        this.distributionClient = distributionClient;
    }
}
