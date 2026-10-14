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

import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.onap.ccsdk.cds.sdclistener.client.SdcListenerClient;
import org.onap.ccsdk.cds.sdclistener.dto.SdcListenerDto;
import org.onap.ccsdk.cds.sdclistener.exceptions.SdcListenerException;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientFactory;
import org.onap.sdc.impl.DistributionClientResultImpl;
import org.onap.sdc.api.results.DistributionActionResultEnum;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class SdcListenerClientTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private IDistributionClient distributionClient;

    @Mock
    private SdcListenerConfiguration configuration;

    @Mock
    private SdcListenerNotificationCallback notification;

    @Mock
    private SdcListenerDto sdcListenerDto;

    @InjectMocks
    private SdcListenerClient sdcListenerClient;

    @Test
    public void testInitCdsClientSuccesfully() throws SdcListenerException {
        when(distributionClient.init(configuration, notification)).thenReturn(getResult());
        when(distributionClient.start()).thenReturn(getResult());

        try (MockedStatic<DistributionClientFactory> factory = mockStatic(DistributionClientFactory.class)) {
            factory.when(DistributionClientFactory::createDistributionClient).thenReturn(distributionClient);

            sdcListenerClient.initSdcClient();
        }

        InOrder order = inOrder(distributionClient);
        order.verify(distributionClient).init(configuration, notification);
        order.verify(distributionClient).start();
    }

    public IDistributionClientResult getResult() {
        return new DistributionClientResultImpl(DistributionActionResultEnum.SUCCESS,
                DistributionActionResultEnum.SUCCESS.name());
    }

}
