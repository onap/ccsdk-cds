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

package org.onap.ccsdk.cds.blueprintsprocessor.core.cluster

import com.hazelcast.cluster.Member
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterMember
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService

/**
 * Exposed Dependency Service by this Hazelcast Lib Module
 */
fun BluePrintDependencyService.clusterService(): BluePrintClusterService =
    instance(HazelcastClusterService::class)

/** Optional Cluster Service, returns only if Cluster is enabled */
fun BluePrintDependencyService.optionalClusterService(): BluePrintClusterService? {
    return if (BluePrintConstants.CLUSTER_ENABLED) {
        BluePrintDependencyService.clusterService()
    } else null
}

/** Extension to convert Hazelcast Member to Blueprints Cluster Member */
fun Member.toClusterMember(): ClusterMember {
    val memberName: String = this.getAttribute(BluePrintConstants.PROPERTY_CLUSTER_NODE_ID) ?: this.uuid.toString()
    return ClusterMember(
        id = this.uuid.toString(),
        name = memberName,
        memberAddress = this.address.toString()
    )
}
