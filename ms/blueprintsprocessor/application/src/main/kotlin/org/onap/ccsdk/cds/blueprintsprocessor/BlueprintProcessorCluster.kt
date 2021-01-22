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

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BlueprintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterInfo
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.utils.ClusterUtils
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.Properties

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
 * 3. Each contained, should have environment properties CLUSTER_ID, CLUSTER_NODE_ID, CLUSTER_JOIN_AS_CLIENT,
 * CLUSTER_CONFIG_FILE
 *     Example values :
 *      CLUSTER_ID: cds-cluster
 *      CLUSTER_NODE_ID: cds-controller-2
 *      CLUSTER_JOIN_AS_CLIENT: "true" or "false"
 *      CLUSTER_CONFIG_FILE:  <Config location>
 * 4. Cluster will be enabled only all the above properties present in the environments.
 * if CLUSTER_ENABLED is present, then it will try to create cluster.
 */
@Component
open class BlueprintProcessorCluster(private val bluePrintClusterService: BlueprintClusterService) {

    private val log = logger(BlueprintProcessorCluster::class)

    @EventListener(ApplicationReadyEvent::class)
    fun startAndJoinCluster() = GlobalScope.launch {

        if (BlueprintConstants.CLUSTER_ENABLED) {

            val clusterId = ClusterUtils.clusterId()
            val nodeId = ClusterUtils.clusterNodeId()

            val joinAsClient =
                (System.getenv(BlueprintConstants.PROPERTY_CLUSTER_JOIN_AS_CLIENT) ?: "false").toBoolean()

            val clusterConfigFile = System.getenv(BlueprintConstants.PROPERTY_CLUSTER_CONFIG_FILE)

            val properties = Properties()
            properties["hazelcast.logging.type"] = "slf4j"

            val clusterInfo = ClusterInfo(
                id = clusterId, nodeId = nodeId,
                joinAsClient = joinAsClient,
                configFile = clusterConfigFile,
                properties = properties
            )
            bluePrintClusterService.startCluster(clusterInfo)
        } else {
            log.info("Cluster is disabled, to enable cluster set the environment CLUSTER_* properties.")
        }
    }
}
