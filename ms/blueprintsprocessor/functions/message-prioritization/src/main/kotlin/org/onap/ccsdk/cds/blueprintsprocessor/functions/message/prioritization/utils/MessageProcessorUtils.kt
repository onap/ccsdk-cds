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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.utils

import org.apache.kafka.streams.processor.ProcessorSupplier
import org.onap.ccsdk.cds.blueprintsprocessor.core.cluster.optionalClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterLock
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.kafka.AbstractMessagePrioritizeProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.toFormatedCorrelation
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintDependencyService

object MessageProcessorUtils {

    /** Utility to create the cluster lock for message [messagePrioritization] prioritization procssing.*/
    suspend fun prioritizationGrouplock(messagePrioritization: MessagePrioritization): ClusterLock? {
        val clusterService = BlueprintDependencyService.optionalClusterService()

        return if (clusterService != null && clusterService.clusterJoined() &&
            !messagePrioritization.correlationId.isNullOrBlank()
        ) {
            // Get the correlation key in ascending order, even it it is misplaced
            val correlationId = messagePrioritization.toFormatedCorrelation()
            val lockName = "prioritize::${messagePrioritization.group}::$correlationId"
            val clusterLock = clusterService.clusterLock(lockName)
            clusterLock.lock()
            if (!clusterLock.isLocked()) throw BlueprintProcessorException("failed to lock($lockName)")
            clusterLock
        } else null
    }

    /** Utility to create the cluster lock for expiry scheduler*/
    suspend fun prioritizationExpiryLock(): ClusterLock? {
        val clusterService = BlueprintDependencyService.optionalClusterService()
        return if (clusterService != null && clusterService.clusterJoined()) {
            val lockName = "prioritize-expiry"
            val clusterLock = clusterService.clusterLock(lockName)
            clusterLock.lock()
            if (!clusterLock.isLocked()) throw BlueprintProcessorException("failed to lock($lockName)")
            clusterLock
        } else null
    }

    /** Utility to create the cluster lock for expiry scheduler*/
    suspend fun prioritizationCleanLock(): ClusterLock? {
        val clusterService = BlueprintDependencyService.optionalClusterService()
        return if (clusterService != null && clusterService.clusterJoined()) {
            val lockName = "prioritize-clean"
            val clusterLock = clusterService.clusterLock(lockName)
            clusterLock.lock()
            if (!clusterLock.isLocked()) throw BlueprintProcessorException("failed to lock($lockName)")
            clusterLock
        } else null
    }

    /** Utility used to cluster unlock for message [clusterLock] */
    suspend fun prioritizationUnLock(clusterLock: ClusterLock?) {
        if (clusterLock != null) {
            clusterLock.unLock()
            clusterLock.close()
        }
    }

    /** Get the Kafka Supplier for processor lookup [name] **/
    fun <K, V> bluePrintProcessorSupplier(name: String): ProcessorSupplier<K, V> {
        return ProcessorSupplier<K, V> {
            // Dynamically resolve the Prioritization Processor
            BlueprintDependencyService.instance<AbstractMessagePrioritizeProcessor<K, V>>(name)
        }
    }
}
