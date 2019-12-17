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

package org.onap.ccsdk.cds.blueprintsprocessor.atomix.service

import io.atomix.cluster.ClusterMembershipEvent
import io.atomix.core.Atomix
import kotlinx.coroutines.delay
import org.onap.ccsdk.cds.blueprintsprocessor.atomix.utils.AtomixLibUtils
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.BluePrintClusterService
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterInfo
import org.onap.ccsdk.cds.blueprintsprocessor.core.service.ClusterMember
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.CompletableFuture

@Service
open class AtomixBluePrintClusterService : BluePrintClusterService {

    private val log = logger(AtomixBluePrintClusterService::class)

    lateinit var atomix: Atomix

    private var joined = false

    override suspend fun startCluster(clusterInfo: ClusterInfo) {
        log.info(
            "Cluster(${clusterInfo.id}) node(${clusterInfo.nodeId}), node address(${clusterInfo.nodeAddress}) " +
                "starting with members(${clusterInfo.clusterMembers})"
        )

        /** Create Atomix cluster either from config file or default multi-cast cluster*/
        atomix = if (!clusterInfo.configFile.isNullOrEmpty()) {
            AtomixLibUtils.configAtomix(clusterInfo.configFile!!)
        } else {
            AtomixLibUtils.defaultMulticastAtomix(clusterInfo)
        }

        /** Listen for the member chaneg events */
        atomix.membershipService.addListener { membershipEvent ->
            when (membershipEvent.type()) {
                ClusterMembershipEvent.Type.MEMBER_ADDED -> log.info("***** New Member Added")
                ClusterMembershipEvent.Type.MEMBER_REMOVED -> log.info("***** Member Removed")
                ClusterMembershipEvent.Type.METADATA_CHANGED -> log.info("***** Metadata Changed Removed")
                else -> log.info("***** Member event unknown")
            }
        }
        atomix.start().join()
        log.info(
            "Cluster(${clusterInfo.id}) node(${clusterInfo.nodeId}), node address(${clusterInfo.nodeAddress}) " +
                "created successfully...."
        )

        /** Receive ping from network */
        val pingHandler = { message: String ->
            log.info("####### ping message received : $message")
            CompletableFuture.completedFuture(message)
        }
        atomix.communicationService.subscribe("ping", pingHandler)

        /** Ping the network */
        atomix.communicationService.broadcast(
            "ping",
            "ping from node(${clusterInfo.nodeId})"
        )
        joined = true
    }

    override fun clusterJoined(): Boolean {
        return joined
    }

    override suspend fun allMembers(): Set<ClusterMember> {
        check(::atomix.isInitialized) { "failed to start and join cluster" }
        check(atomix.isRunning) { "cluster is not running" }
        return atomix.membershipService.members.map {
            ClusterMember(
                id = it.id().id(),
                memberAddress = it.host()
            )
        }.toSet()
    }

    override suspend fun clusterMembersForPrefix(memberPrefix: String): Set<ClusterMember> {
        check(::atomix.isInitialized) { "failed to start and join cluster" }
        check(atomix.isRunning) { "cluster is not running" }

        return atomix.membershipService.members.filter {
            it.id().id().startsWith(memberPrefix, true)
        }.map { ClusterMember(id = it.id().id(), memberAddress = it.host()) }
            .toSet()
    }

    override suspend fun <T> clusterMapStore(name: String): MutableMap<String, T> {
        return AtomixLibUtils.distributedMapStore<T>(atomix, name)
    }

    override suspend fun shutDown(duration: Duration) {
        val shutDownMilli = duration.toMillis()
        log.info("Received cluster shutdown request, shutdown in ($shutDownMilli)ms")
        delay(shutDownMilli)
        atomix.stop()
    }
}
