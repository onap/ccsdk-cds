/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor

import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterInfo
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.splitCommaAsList
import org.onap.ccsdk.cds.controllerblueprints.core.utils.ClusterUtils
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.Duration
import javax.annotation.PreDestroy

/**
 * To Start the cluster, minimum 2 Instances/ Replicas od CDS needed.
 * All instance such as Blueprintprocessor, ResourceResolution, MessagePrioritization should be in
 * same cluster and should have same cluster name.
 *
 * Data can be shared only between the clusters, outside the cluster data can't be shared.
 * If cds-controller-x instance wants to share data with resource-resolution-x instance, then it should be in the
 * same cluster.(cds-cluster) and same network (cds-network)
 *
 * Assumptions:
 * 1. Container, Pod and Host names are same.
 * 2. Container names should end with sequence number.
 *      Blueprintprocessor example be : cds-controller-1, cds-controller-2, cds-controller-3
 *      ResourceResolution example be : resource-resolution-1, resource-resolution-2,  resource-resolution-3
 * 3. Each contained, should have environment properties CLUSTER_ID, CLUSTER_NODE_ID, CLUSTER_NODE_ADDRESS,
 * CLUSTER_MEMBERS, CLUSTER_STORAGE_PATH
 *     Example values :
 *      CLUSTER_ID: cds-cluster
 *      CLUSTER_NODE_ID: cds-controller-2
 *      CLUSTER_NODE_ADDRESS: cds-controller-2
 *      CLUSTER_MEMBERS: cds-controller-1,cds-controller-2,cds-controller-3,resource-resolution-1,resource-resolution-2,resource-resolution-3
 *      CLUSTER_STORAGE_PATH: /opt/app/onap/config/cluster
 *      CLUSTER_CONFIG_FILE:  /opt/app/onap/config/atomix/atomix-multicast.conf
 * 4. Cluster will be enabled only all the above properties present in the environments.
 * if CLUSTER_ENABLED is present, then it will try to create cluster.
 */
@Component
open class BluePrintProcessorCluster(private val bluePrintClusterService: BluePrintClusterService) {

    private val log = logger(BluePrintProcessorCluster::class)

    @EventListener(ApplicationReadyEvent::class)
    fun startAndJoinCluster() = runBlocking {

        if (BluePrintConstants.CLUSTER_ENABLED) {

            val clusterId = ClusterUtils.clusterId()
            val nodeId = ClusterUtils.clusterNodeId()
            val nodeAddress = ClusterUtils.clusterNodeAddress()

            val clusterMembers = System.getenv(BluePrintConstants.PROPERTY_CLUSTER_MEMBERS)
                ?: throw BluePrintProcessorException("couldn't get environment variable ${BluePrintConstants.PROPERTY_CLUSTER_MEMBERS}")

            val clusterMemberList = clusterMembers.splitCommaAsList()

            val clusterStorage = System.getenv(BluePrintConstants.PROPERTY_CLUSTER_STORAGE_PATH)
                ?: throw BluePrintProcessorException("couldn't get environment variable ${BluePrintConstants.PROPERTY_CLUSTER_STORAGE_PATH}")

            val clusterConfigFile = System.getenv(BluePrintConstants.PROPERTY_CLUSTER_CONFIG_FILE)

            val clusterInfo = ClusterInfo(
                id = clusterId, nodeId = nodeId,
                clusterMembers = clusterMemberList, nodeAddress = nodeAddress,
                storagePath = clusterStorage,
                configFile = clusterConfigFile
            )
            bluePrintClusterService.startCluster(clusterInfo)
        } else {
            log.info("Cluster is disabled, to enable cluster set the environment CLUSTER_* properties.")
        }
    }

    @PreDestroy
    fun shutDown() = runBlocking {
        bluePrintClusterService.shutDown(Duration.ofSeconds(1))
    }
}
