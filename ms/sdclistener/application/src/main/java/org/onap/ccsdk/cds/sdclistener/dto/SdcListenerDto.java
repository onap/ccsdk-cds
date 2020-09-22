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

package org.onap.ccsdk.cds.sdclistener.dto;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.onap.ccsdk.cds.sdclistener.client.SdcListenerAuthClientInterceptor;
import org.onap.sdc.api.IDistributionClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("listenerservice")
public class SdcListenerDto {

    @Value("${listenerservice.config.grpcAddress}")
    private String grpcAddress;

    @Value("${listenerservice.config.grpcPort}")
    private int grpcPort;

    private String artifactUrl;

    @Autowired
    private SdcListenerAuthClientInterceptor sdcListenerAuthClientInterceptor;

    private IDistributionClient distributionClient;
    private ManagedChannel managedChannel;
    private String distributionId;

    public IDistributionClient getDistributionClient() {
        return distributionClient;
    }

    public void setDistributionClient(IDistributionClient distributionClient) {
        this.distributionClient = distributionClient;
    }

    public void setDistributionId(String id) {
        this.distributionId = id;
    }

    public String getDistributionId() {
        return distributionId;
    }

    public void setManagedChannelForGrpc() {
        managedChannel = ManagedChannelBuilder.forAddress(grpcAddress, grpcPort).usePlaintext()
                .intercept(sdcListenerAuthClientInterceptor).build();
    }

    public ManagedChannel getManagedChannelForGrpc() {
        return managedChannel;
    }

    public String getArtifactUrl() {
        return artifactUrl;
    }

    public void setArtifactUrl(String artifactUrl) {
        this.artifactUrl = artifactUrl;
    }

}
