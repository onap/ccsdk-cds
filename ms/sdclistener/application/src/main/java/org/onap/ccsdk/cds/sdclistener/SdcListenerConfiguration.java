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

import org.onap.sdc.api.consumer.IConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * In order to initiate a SDC distribution client we need to supply some pre-configuration values that distribution
 * client needs.
 */
@ConfigurationProperties("listenerservice")
public class SdcListenerConfiguration implements IConfiguration {

    public static final String TOSCA_CSAR = "TOSCA_CSAR";

    @Value("${listenerservice.config.asdcAddress}")
    private String asdcAddress;

    @Value("${listenerservice.config.messageBusAddress}")
    private List<String> messageBusAddress;

    @Value("${listenerservice.config.user}")
    private String user;

    @Value("${listenerservice.config.password}")
    private String password;

    @Value("${listenerservice.config.pollingTimeout}")
    private int pollingTimeout;

    @Value("${listenerservice.config.pollingInterval}")
    private int pollingInterval;

    @Value("${listenerservice.config.relevantArtifactTypes}")
    private List<String> relevantArtifactTypes;

    @Value("${listenerservice.config.consumerGroup}")
    private String consumerGroup;

    @Value("${listenerservice.config.environmentName}")
    private String envName;

    @Value("${listenerservice.config.consumerId}")
    private String consumerId;

    @Value("${listenerservice.config.activateServerTLSAuth}")
    private boolean activateServerTLSAuth;

    @Value("${listenerservice.config.isUseHttpsWithDmaap}")
    private boolean isUseHttpsWithDmaap;

    @Override
    public String getAsdcAddress() {
        return asdcAddress;
    }

    @Override
    public List<String> getMsgBusAddress() {
        return messageBusAddress;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public int getPollingInterval() {
        return pollingInterval;
    }

    @Override
    public int getPollingTimeout() {
        return pollingTimeout;
    }

    @Override
    public List<String> getRelevantArtifactTypes() {
        return relevantArtifactTypes;
    }

    @Override
    public String getConsumerGroup() {
        return consumerGroup;
    }

    @Override
    public String getEnvironmentName() {
        return envName;
    }

    @Override
    public String getConsumerID() {
        return consumerId;
    }

    @Override
    public String getKeyStorePath() {
        return null;
    }

    @Override
    public String getKeyStorePassword() {
        return null;
    }

    @Override
    public boolean activateServerTLSAuth() {
        return activateServerTLSAuth;
    }

    @Override
    public boolean isFilterInEmptyResources() {
        return false;
    }

    @Override
    public Boolean isUseHttpsWithDmaap() {
        return isUseHttpsWithDmaap;
    }

}

