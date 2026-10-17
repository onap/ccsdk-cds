/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.cds.sdclistener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.ccsdk.cds.sdclistener.status.SdcListenerStatus.NotificationType.DOWNLOAD;
import static org.onap.ccsdk.cds.sdclistener.status.SdcListenerStatus.NotificationType.SDC_LISTENER_COMPONENT;
import static org.onap.sdc.api.notification.DistributionStatusEnum.COMPONENT_DONE_ERROR;
import static org.onap.sdc.api.notification.DistributionStatusEnum.DOWNLOAD_ERROR;

import java.util.Collections;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.onap.ccsdk.cds.sdclistener.dto.SdcListenerDto;
import org.onap.ccsdk.cds.sdclistener.service.ListenerService;
import org.onap.ccsdk.cds.sdclistener.status.SdcListenerStatus;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.results.DistributionActionResultEnum;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;

public class SdcListenerNotificationCallbackTest {

    private static final String DISTRIBUTION_ID = "dist-1";
    private static final String URL = "/sdc/v1/catalog/services/svc/1.0/artifacts/service-Svc-csar.csar";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private SdcListenerDto sdcListenerDto;

    @Mock
    private ListenerService listenerService;

    @Mock
    private SdcListenerStatus listenerStatus;

    @InjectMocks
    private SdcListenerNotificationCallback callback;

    private INotificationData notification;
    private IDistributionClient distributionClient;

    @Before
    public void setUp() {
        IArtifactInfo csar = mock(IArtifactInfo.class);
        when(csar.getArtifactType()).thenReturn(SdcListenerConfiguration.TOSCA_CSAR);
        when(csar.getArtifactURL()).thenReturn(URL);

        notification = mock(INotificationData.class);
        when(notification.getDistributionID()).thenReturn(DISTRIBUTION_ID);
        when(notification.getServiceArtifacts()).thenReturn(Collections.singletonList(csar));

        IDistributionClientDownloadResult result = mock(IDistributionClientDownloadResult.class);
        when(result.getDistributionActionResult()).thenReturn(DistributionActionResultEnum.FAIL);

        distributionClient = mock(IDistributionClient.class);
        when(distributionClient.download(csar)).thenReturn(result);
        when(sdcListenerDto.getDistributionClient()).thenReturn(distributionClient);
        when(sdcListenerDto.getDistributionId()).thenReturn(DISTRIBUTION_ID);
    }

    @Test
    public void failedDownloadIsFollowedByComponentDoneError() {
        callback.activateCallback(notification);

        String errorMessage = "Failed to download the artifact from : " + URL + " due to FAIL ";
        InOrder inOrder = inOrder(listenerStatus);
        inOrder.verify(listenerStatus).sendResponseBackToSdc(DISTRIBUTION_ID, DOWNLOAD_ERROR, errorMessage, URL,
                DOWNLOAD);
        inOrder.verify(listenerStatus).sendResponseBackToSdc(DISTRIBUTION_ID, COMPONENT_DONE_ERROR, errorMessage, URL,
                SDC_LISTENER_COMPONENT);
        verify(listenerService, never()).extractCsarAndStore(any(), any());
    }
}
