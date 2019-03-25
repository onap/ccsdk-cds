/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package org.onap.ccsdk.cds.cdssdclistener;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Tested;
import mockit.VerificationsInOrder;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.ccsdk.cds.cdssdclistener.exceptions.CdsSdcListenerException;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientFactory;
import org.onap.sdc.impl.DistributionClientResultImpl;
import org.onap.sdc.utils.DistributionActionResultEnum;

@RunWith(JMockit.class)
public class CdsSdcListenerClientTest {

    @Tested
    private CdsSdcListenerClient cdsSdcListenerClient;

    @Test
    public void testInitCdsClientSuccesfully(@Injectable IDistributionClient distributionClient,
        @Injectable CdsSdcListenerConfiguration configuration,
        @Injectable CdsSdcListenerNotificationCallback notification,
        @Injectable CdsSdcListenerDto cdsSdcListenerDto) throws CdsSdcListenerException {

        // Arrange
        new MockUp<DistributionClientFactory>() {
            @Mock
            public IDistributionClient createDistributionClient() {
                return distributionClient;
            }
        };

        new Expectations() {{
            distributionClient.init(configuration, notification);
            result = getResult();
        }};

        new Expectations() {{
            distributionClient.start();
            result = getResult();
        }};

        // Act
        cdsSdcListenerClient.initSdcClient();

        // Verify
        new VerificationsInOrder() {{
            distributionClient.init(configuration, notification);
            distributionClient.start();
        }};
    }

    public IDistributionClientResult getResult() {
        return new DistributionClientResultImpl(DistributionActionResultEnum.SUCCESS,
            DistributionActionResultEnum.SUCCESS.name());
    }
}
