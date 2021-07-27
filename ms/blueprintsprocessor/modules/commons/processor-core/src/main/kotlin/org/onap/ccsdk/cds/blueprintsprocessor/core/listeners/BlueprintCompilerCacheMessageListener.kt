/*
 * Copyright © 2020 Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.core.listeners

import org.onap.ccsdk.cds.blueprintsprocessor.core.cluster.BlueprintClusterTopic
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterMessage
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BlueprintClusterMessageListener
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterJoinedEvent
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.scripts.BluePrintCompileCache
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("CLUSTER_ENABLED", havingValue = "true")
open class BlueprintCompilerCacheMessageListener(private val clusterService: BluePrintClusterService) : BlueprintClusterMessageListener<String> {
    private val log = logger(BlueprintCompilerCacheMessageListener::class)

    @EventListener(ClusterJoinedEvent::class)
    fun register() {
        log.info("Registering BlueprintCompilerCacheMessageListener")
        clusterService.addBlueprintClusterMessageListener(BlueprintClusterTopic.BLUEPRINT_CLEAN_COMPILER_CACHE, this)
    }

    override fun onMessage(message: BluePrintClusterMessage<String>?) {
        message?.let {
            log.info("Received ClusterMessage - Cleaning compile cache for blueprint (${it.payload})")
            BluePrintCompileCache.cleanClassLoader(it.payload)
        }
    }
}
