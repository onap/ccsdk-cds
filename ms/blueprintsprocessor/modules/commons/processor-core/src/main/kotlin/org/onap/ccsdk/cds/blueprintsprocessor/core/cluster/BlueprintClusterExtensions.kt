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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BlueprintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterLock
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterMember
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.MDCContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintDependencyService

/**
 * Exposed Dependency Service by this Hazelcast Lib Module
 */
fun BlueprintDependencyService.clusterService(): BlueprintClusterService =
    instance(HazelcastClusterService::class)

/** Optional Cluster Service, returns only if Cluster is enabled */
fun BlueprintDependencyService.optionalClusterService(): BlueprintClusterService? {
    return if (BlueprintConstants.CLUSTER_ENABLED) {
        BlueprintDependencyService.clusterService()
    } else null
}

/** Extension to convert Hazelcast Member to Blueprints Cluster Member */
fun Member.toClusterMember(): ClusterMember {
    val memberName: String = this.getAttribute(BlueprintConstants.PROPERTY_CLUSTER_NODE_ID) ?: this.uuid.toString()
    return ClusterMember(
        id = this.uuid.toString(),
        name = memberName,
        memberAddress = this.address.toString()
    )
}

/**
 * This function will try to acquire the lock and then execute the provided block.
 * If the lock cannot be acquired within timeout, a BlueprintException will be thrown.
 *
 * Since a lock can only be unlocked by the the thread which acquired the lock,
 * this function will confine coroutines within the block to a dedicated thread.
 */
suspend fun <R> ClusterLock.executeWithLock(acquireLockTimeout: Long, block: suspend () -> R): R {
    val lock = this
    return newSingleThreadContext(lock.name()).use {
        withContext(GlobalScope.coroutineContext[MDCContext]?.plus(it) ?: it) {
            if (lock.tryLock(acquireLockTimeout)) {
                try {
                    block()
                } finally {
                    lock.unLock()
                }
            } else
                throw BlueprintException("Failed to acquire lock within timeout")
        }
    }
}
