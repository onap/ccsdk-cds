/*
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 *
 * NOTICE:  All the intellectual and technical concepts contained herein are
 * proprietary to Bell Canada and are protected by trade secret or copyright law.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */

package org.onap.ccsdk.cds.cdssdclistener;

import java.util.List;
import org.onap.sdc.api.consumer.IConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * In order to initiate a SDC distribution client we need to supply some pre-configuration values that
 * distribution client needs.
 */
@ConfigurationProperties("listenerservice")
public class CdsSdcListenerConfiguration implements IConfiguration {

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

