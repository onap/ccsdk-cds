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
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterLock
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.AbstractMessagePrioritizeProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.PrioritizationConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.message.prioritization.db.MessagePrioritization
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService

object MessageProcessorUtils {

    /** Utility to create the cluster lock for message [messagePrioritization] */
    suspend fun prioritizationGrouplock(
        clusterService: BluePrintClusterService?,
        messagePrioritization: MessagePrioritization
    ): ClusterLock? {
        return if (clusterService != null && clusterService.clusterJoined()) {
            val lockName = "prioritization-${messagePrioritization.group}"
            val clusterLock = clusterService.clusterLock(lockName)
            clusterLock.lock()
            if (!clusterLock.isLocked()) throw BluePrintProcessorException("failed to lock($lockName)")
            clusterLock
        } else null
    }

    /** Utility used to cluster unlock for message [messagePrioritization] */
    suspend fun prioritizationGroupUnLock(clusterService: BluePrintClusterService?, clusterLock: ClusterLock?) {
        if (clusterService != null && clusterService.clusterJoined() && clusterLock != null) {
            clusterLock.unLock()
            clusterLock.close()
        }
    }

    fun <K, V> bluePrintProcessorSupplier(name: String, prioritizationConfiguration: PrioritizationConfiguration):
        ProcessorSupplier<K, V> {
        return ProcessorSupplier<K, V> {
            // Dynamically resolve the Prioritization Processor
            val processorInstance = BluePrintDependencyService.instance<AbstractMessagePrioritizeProcessor<K, V>>(name)
            processorInstance.prioritizationConfiguration = prioritizationConfiguration
            processorInstance
        }
    }
}
